package helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static Date getDateFromLogString(String dateAsString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d HH:mm:ss.S");
        try {
            return format.parse(dateAsString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
