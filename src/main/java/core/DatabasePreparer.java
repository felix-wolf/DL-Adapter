package core;

import java.sql.Timestamp;

public class DatabasePreparer {

    static int x = 0;

    static void fillDatabase() {
        for (int i = x * 5000; i < (x + 1) * 5000; i++) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis() + 1000000);
            new DatabaseRequester().execAction("INSERT INTO BOOK(id, title, author, publisher, isavail, updated_at) VALUES('" + i + "', 'test', 'test', 'test', true, '"+ timestamp + "')");
        }
    }

}
