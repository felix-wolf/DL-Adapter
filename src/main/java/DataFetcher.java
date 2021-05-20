import io.reactivex.rxjava3.core.Observable;
import models.Book;

import java.sql.*;
import java.util.ArrayList;

public class DataFetcher {

    private static final String PATH_TO_DB = "jdbc:derby://localhost:1527/database;create=true";
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet resultSet = null;
    private String bookTimeStamp = "2021-04-19 13:27:49.16";

    public DataFetcher() {
        createConnection();
        // runTestStatement();
        setInitialBookTimeStamp();
    }

    private void createConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").getDeclaredConstructor().newInstance();
            conn = DriverManager.getConnection(PATH_TO_DB);
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

    Observable<Book> loadNewBooks() {
        ArrayList<Book> books = new ArrayList();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT * from BOOK WHERE updated_at > ?");
            statement.setString(1, bookTimeStamp);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String publisher = resultSet.getString("publisher");
                boolean isAvailable = resultSet.getBoolean("isAvail");
                String updated_at = resultSet.getString("updated_at");
                Book book = new Book(id, title, author, publisher, isAvailable, updated_at);
                System.out.println(book.getTitle());
                books.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Observable.fromIterable(books);
    }

    private void setInitialBookTimeStamp() {
        //TODO: SET BOOK TIME STAMP TO GOOD VALUE
    }

    void updateBookTimeStamp(String bookTimeStamp) {
        this.bookTimeStamp = bookTimeStamp;
    }


}
