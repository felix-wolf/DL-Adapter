package core;

import com.google.gson.Gson;
import models.*;
import timer.TimerUtil;

import javax.swing.*;
import java.sql.Timestamp;
import java.sql.*;
import java.util.HashMap;
import java.util.Objects;

public class DatabaseRequester {

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
            HashMap<Integer, Operation> convertedEventsMap = getNewOperationsFromOutboxTable();
            if (!convertedEventsMap.isEmpty()) {
                try {
                    EventProducer.produceEvents(convertedEventsMap);

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
     * gets all the books in the database
     * filters them by updated_at column
     * @return returns a list of all books that have not yet been processed
     */
    private HashMap<Integer, Operation> getNewOperationsFromOutboxTable() {
        ResultSet rs = execQuery("SELECT * FROM OUTBOX");
        HashMap<Integer, Operation> operationsMap = new HashMap<>();
        try {
            while (Objects.requireNonNull(rs).next()) {
                int id = rs.getInt("id");
                String operationTypeString = rs.getString("operationType");
                String objectTypeString = rs.getString("objectType");
                String objectString = rs.getString("object");
                Timestamp created_at = rs.getTimestamp("created_at");
                ObjectType objectType = ObjectType.valueOf(objectTypeString);
                Operation operation = new Operation(
                        created_at.getTime(),
                        OperationType.valueOf(operationTypeString),
                        objectType,
                        processObjectJson(objectString, objectType)
                );
                operationsMap.put(id, operation);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return operationsMap;
    }
    /**
     * deletes an entity
     * @param id the id of the row to delete
     */
    public static void deleteProcessedOperation(Integer id) {
        String queryString = "DELETE FROM OUTBOX WHERE id = " + id;
        execAction(queryString);
    }

    /**
     * converts a json string to an object
     * @param json the json as a string
     * @param objectType the type of object to convert the json to
     * @return the converted object
     */
    private Object processObjectJson(String json, ObjectType objectType) {
        Gson gson = new Gson();
        Object object = null;
        switch (objectType) {
            case BOOK:
                object = gson.fromJson(json, Book.class);
                break;
            case MEMBER:
                object = gson.fromJson(json, Member.class);
                break;
            case ISSUE:
                object = gson.fromJson(json, Issue.class);
                break;
            case MAIL_SERVER_INFO:
                object = gson.fromJson(json, MailServerInfo.class);
                break;
        }
        return object;
    }

    /**
     * executes an non selecting query
     * @param query query to execute
     * @return true if successful, false if an exceptions was thrown
     */
    public static boolean execAction(String query) {
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