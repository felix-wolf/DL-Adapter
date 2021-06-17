package core;

import helper.Utils;
import models.ObjectType;
import models.Operation;
import models.OperationType;
import models.extended.*;
import timer.TimerUtil;

import javax.swing.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class DatabaseRequester {

    private Date lastRead = null;
    private static final String DB_URL_LIVE = "jdbc:derby://localhost:1527/database;create=true";
    private static final String DRIVER_LIVE = "org.apache.derby.jdbc.EmbeddedDriver";
    private static Connection conn = null;

    public DatabaseRequester() {
        createConnection();
        initialiseTimer();
    }

    /**
     * creates a connection to the database
     */
    private void createConnection() {
        try {
            Class.forName(DRIVER_LIVE).newInstance();
            conn = DriverManager.getConnection(DB_URL_LIVE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cant load database", "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * creates a timer that fires every 5 seconds
     * upon firing, the database is queried for new entries
     * entries are then published in form of events
     */
    private void initialiseTimer() {
        TimerUtil timer = new TimerUtil(5000);
        timer.startTimer(fireCounts -> {
            System.out.println("Fired, count: " + fireCounts);
            lastRead = new Date(Utils.getLastReadTime());
            ArrayList<Operation> convertedEvents = generateNewOperations();
            if (!convertedEvents.isEmpty()) {
                try {
                    EventProducer.produceEvents(convertedEvents);
                    Utils.updateLastRead(System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // stops timer
            if (fireCounts == -1) {
                timer.stopTimer();
            }
        });
    }

    /**
     * checks if the updated_at timestamp is newer than the last time the tool ran
     * which means the the entry belonging the to timestamp has not yet processed
     * @param updatedAt timestamp in String format
     * @param lastReadDate Time in Date form when the tool last ran
     * @return true if updatedAt is newer
     */
    private boolean updatedAtNewerThanLastRead(String updatedAt, Date lastReadDate) {
        if (updatedAt == null) return true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d HH:mm:ss.SSS");
        Date fileDate;
        try {
            fileDate = format.parse(updatedAt);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return fileDate.after(lastReadDate);
    }

    /**
     * generate new operations to publish
     * @return a list of operations
     */
    public ArrayList<Operation> generateNewOperations() {
        ArrayList<BookExtended> books = getNewBooks();
        ArrayList<MemberExtended> members = getNewMembers();
        ArrayList<IssueExtended> issues = getNewIssues();
        ArrayList<MailServerInfoExtended> mailServerInfos = getNewMailServerInfos();
        ArrayList<Operation> operations = new ArrayList<>();
        operations.addAll(processEntities(books, ObjectType.BOOK, "id"));
        operations.addAll(processEntities(members, ObjectType.MEMBER, "id"));
        operations.addAll(processEntities(issues, ObjectType.ISSUE, "bookid"));
        operations.addAll(processEntities(mailServerInfos, ObjectType.MAIL_SERVER_INFO, "server_name"));
        return operations;
    }

    /**
     * processes entities by converting objects to operations
     * The type of operations is inferred by the state of the entity, meaning:
     *
     * if an entity has set is_deleted to true, it is a DELETE operation.
     * after the entities is published to the kafka topic, this tool fully deletes the entity in the database
     *
     * if an entity has set updated_at to null, it is an INSERT operation.
     * to avoid that this entities is processed again the next time the timer fires,
     * this tool sets the updated_at column to CURRENT_TIMESTAMP
     *
     * if updated_at is a valid value that is newer than lastTimeRead and is_deleted is false,
     * it is an UPDATE operation.
     * UPDATE operations do not require any additional work other than processing the event
     *
     * @param extendeds list of entities to process
     * @param objectType type of object
     * @param nameOfPrimaryKey name of the primary key (used when updating or deleting the entry after processing)
     * @return list of operations
     */
    private ArrayList<Operation> processEntities(ArrayList<? extends Extended> extendeds, ObjectType objectType, String nameOfPrimaryKey) {
        ArrayList<Operation> operations = new ArrayList<>();
        for (Extended extended : extendeds) {
            if (extended.getIsDeleted()) {
                operations.add(new Operation(new Date().getTime(), OperationType.DELETE, objectType, extended.getBasicType()));
                deleteEntity(extended, objectType, nameOfPrimaryKey);
            } else if (!extended.getIsDeleted() && extended.getUpdatedAt() == null) {
                operations.add(new Operation(new Date().getTime(), OperationType.INSERT, objectType, extended.getBasicType()));
                markEntityAsChanged(extended, objectType, nameOfPrimaryKey);
            } else if (!extended.getIsDeleted() && extended.getUpdatedAt() != null) {
                operations.add(new Operation(new Date().getTime(), OperationType.UPDATE, objectType, extended.getBasicType()));
            }
        }
        return operations;
    }

    /**
     * marks an entity as changed by updating the updated_at column
     * @param object the object to update
     * @param objectType the type of the object
     * @param nameOfPrimaryKey name of the primary key
     */
    private void markEntityAsChanged(Extended object, ObjectType objectType, String nameOfPrimaryKey) {
        String queryString = "Update " + objectType.name() + " SET UPDATED_AT = CURRENT_TIMESTAMP WHERE "
                + nameOfPrimaryKey + " = '" + object.getId() + "'";
        execAction(queryString);
    }

    /**
     * deletes an entity
     * @param object the object to update
     * @param objectType the type of the object
     * @param nameOfPrimaryKey name of the primary key
     */
    private void deleteEntity(Extended object, ObjectType objectType, String nameOfPrimaryKey) {
        String queryString = "DELETE FROM " +  objectType.name() + " WHERE " + nameOfPrimaryKey + " = '" + object.getId() + "'";
        execAction(queryString);
    }

    /**
     * gets all the books in the database
     * filters them by updated_at column
     * @return returns a list of all books that have not yet been processed
     */
    private ArrayList<BookExtended> getNewBooks() {
        ResultSet rs = execQuery("SELECT * FROM BOOK");
        ArrayList<BookExtended> books = new ArrayList<>();
        try {
            while (Objects.requireNonNull(rs).next()) {
                String id = rs.getString("ID");
                String title = rs.getString("TITLE");
                String author = rs.getString("AUTHOR");
                String publisher = rs.getString("PUBLISHER");
                boolean isAvailable = rs.getBoolean("ISAVAIL");
                String updatedAt = rs.getString("UPDATED_AT");
                boolean isDeleted = rs.getBoolean("IS_DELETED");
                BookExtended book = new BookExtended(id, title, author, publisher, isAvailable, updatedAt, isDeleted);
                books.add(book);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        books.removeIf(book -> !updatedAtNewerThanLastRead(book.getUpdatedAt(), lastRead));
        return books;
    }

    /**
     * gets all the members in the database
     * filters them by updated_at column
     * @return returns a list of all members that have not yet been processed
     */
    private ArrayList<MemberExtended> getNewMembers() {
        ResultSet rs = execQuery("SELECT * FROM MEMBER");
        ArrayList<MemberExtended> memberExtendeds = new ArrayList<>();
        try {
            while (Objects.requireNonNull(rs).next()) {
                String id = rs.getString("ID");
                String name = rs.getString("NAME");
                String mobile = rs.getString("MOBILE");
                String email = rs.getString("EMAIL");
                String updatedAt = rs.getString("UPDATED_AT");
                boolean isDeleted = rs.getBoolean("IS_DELETED");
                MemberExtended memberExtended = new MemberExtended(id, name, mobile, email, updatedAt, isDeleted);
                memberExtendeds.add(memberExtended);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        memberExtendeds.removeIf(memberExtended -> !updatedAtNewerThanLastRead(memberExtended.getUpdatedAt(), lastRead));
        return memberExtendeds;
    }

    /**
     * gets all the issues in the database
     * filters them by updated_at column
     * @return returns a list of all issues that have not yet been processed
     */
    private ArrayList<IssueExtended> getNewIssues() {
        ResultSet rs = execQuery("SELECT * FROM ISSUE");
        ArrayList<IssueExtended> issueExtendeds = new ArrayList<>();
        try {
            while (Objects.requireNonNull(rs).next()) {
                String bookId = rs.getString("BOOKID");
                String memberId = rs.getString("MEMBERID");
                String issueTime = rs.getString("ISSUETIME");
                long renewCount = rs.getLong("RENEW_COUNT");
                String updatedAt = rs.getString("UPDATED_AT");
                boolean isDeleted = rs.getBoolean("IS_DELETED");
                IssueExtended issueExtended = new IssueExtended(memberId, bookId, issueTime, renewCount, updatedAt, isDeleted);
                issueExtendeds.add(issueExtended);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        issueExtendeds.removeIf(issueExtended -> !updatedAtNewerThanLastRead(issueExtended.getUpdatedAt(), lastRead));
        return issueExtendeds;
    }

    /**
     * gets all the mailServerInfos in the database
     * filters them by updated_at column
     * @return returns a list of all mailServerInfos that have not yet been processed
     */
    private ArrayList<MailServerInfoExtended> getNewMailServerInfos() {
        ResultSet rs = execQuery("SELECT * FROM MAIL_SERVER_INFO");
        ArrayList<MailServerInfoExtended> mailServerInfoExtendeds = new ArrayList<>();
        try {
            while (Objects.requireNonNull(rs).next()) {
                String id = rs.getString("SERVER_NAME");
                int serverPort = rs.getInt("SERVER_PORT");
                String userEmail = rs.getString("USER_EMAIL");
                String userPassword = rs.getString("USER_PASSWORD");
                boolean sslEnabled = rs.getBoolean("SSL_ENABLED");
                String updatedAt = rs.getString("UPDATED_AT");
                boolean isDeleted = rs.getBoolean("IS_DELETED");
                MailServerInfoExtended mailServerInfoExtended = new MailServerInfoExtended(id, serverPort, userEmail, userPassword, sslEnabled, updatedAt, isDeleted);
                mailServerInfoExtendeds.add(mailServerInfoExtended);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        mailServerInfoExtendeds.removeIf(mailServerInfoExtended -> !updatedAtNewerThanLastRead(mailServerInfoExtended.getUpdatedAt(), lastRead));
        return mailServerInfoExtendeds;
    }

    /**
     * executes an non selecting query
     * @param query query to execute
     * @return true if successful, false if an exceptions was thrown
     */
    public boolean execAction(String query) {
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(query);
            return true;
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Error Occured", JOptionPane.ERROR_MESSAGE);
            System.err.println("Exception at execQuery:dataHandler " + ex.getLocalizedMessage());
            return false;
        }
    }

    /**
     * executes a select query
     * @param query query to execute
     * @return the resultSet containing the entities, null if exceptions was thrown
     */
    private ResultSet execQuery(String query) {
        ResultSet result;
        try {
            Statement stmt = conn.createStatement();
            result = stmt.executeQuery(query);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Exception at execQuery:dataHandler " + ex.getLocalizedMessage());
            return null;
        }
        return result;
    }

}