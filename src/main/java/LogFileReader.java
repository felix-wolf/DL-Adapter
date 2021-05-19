import java.sql.*;

public class LogFileReader {

    private static final String PATH_TO_DB = "jdbc:derby:../library-assistant/database";
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet resultSet = null;

    public LogFileReader() {
        createConnection();
    }

    private void createConnection() {
        try {
            // Class.forName("org.apache.derby.jdbc.ClientDriver").getDeclaredConstructor().newInstance();
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getDeclaredConstructor().newInstance();
            conn = DriverManager.getConnection(PATH_TO_DB);
            runTestStatement();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private boolean runTestStatement() {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * from BOOK");
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("title");
                System.out.println("Book found with title: " + name);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
