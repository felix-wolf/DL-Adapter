package core;

import models.extended.BookExtended;
import timer.TimerUtil;

import javax.swing.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class DatabaseRequester {

    private static final String DB_URL_LIVE = "jdbc:derby://localhost:1527/database;create=true";
    private static final String DRIVER_LIVE = "org.apache.derby.jdbc.EmbeddedDriver";
    private static Connection conn = null;

    public DatabaseRequester() {
        createConnection();
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
    void initialiseTimer() {
        TimerUtil timer = new TimerUtil(1000);
        timer.startTimer(fireCounts -> {
            System.out.println("Fired, count: " + fireCounts);
            long startTime = System.currentTimeMillis();
            System.out.println("TimeStart: " + startTime);
            ArrayList<BookExtended> convertedEvents = getNewBooks(new Timestamp(System.currentTimeMillis()), startTime);
            System.out.println(convertedEvents.size());
            long endTime = System.currentTimeMillis();
            System.out.println("TimeEnd: " + endTime);
            long duration = endTime - startTime;
            System.out.println("duration: " + duration);
            System.out.println("---------------------------------------------------------------------");
            // stops timer
            if (fireCounts == 1) {
                timer.stopTimer();
            }
        });
    }

    /**
     * gets all the books in the database
     * filters them by updated_at column
     * @return returns a list of all books that have not yet been processed
     */
    private ArrayList<BookExtended> getNewBooks(Timestamp lastRead, long startTime) {
        //String query = "SELECT * FROM BOOK WHERE UPDATED_AT IS NULL OR UPDATED_AT > '" + lastRead + "'";
        String query = "SELECT * FROM BOOK";
        ResultSet rs = execQuery(query);
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
        long queryTime = System.currentTimeMillis();
        System.out.println("TimeQuery: " + queryTime);
        long duration = queryTime - startTime;
        System.out.println("QueryDuration: " + duration);
        books.removeIf(book -> !updatedAtNewerThanLastRead(book.getUpdatedAt()));
        return books;
    }



    /**
     * checks if the updated_at timestamp is newer than the last time the tool ran
     * which means the the entry belonging the to timestamp has not yet processed
     * @param updatedAt timestamp in String format
     * @return true if updatedAt is newer
     */
    private boolean updatedAtNewerThanLastRead(String updatedAt) {
        if (updatedAt == null) return true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d HH:mm:ss.SSS");
        Date fileDate;
        try {
            fileDate = format.parse(updatedAt);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return fileDate.after(new Date());
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