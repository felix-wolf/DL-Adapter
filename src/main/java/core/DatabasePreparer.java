package core;

import java.sql.Timestamp;

public class DatabasePreparer {

    static void fillDatabase() {
        for (int i = 0; i < 5000; i++) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 1000000);
            new DatabaseRequester().execAction("INSERT INTO BOOK(id, title, author, publisher, isavail, updated_at) VALUES('" + i + "', 'test', 'test', 'test', true, '"+ timestamp + "')");
        }
    }

}
