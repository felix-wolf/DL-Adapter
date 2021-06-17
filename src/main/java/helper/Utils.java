package helper;

import com.google.gson.Gson;
import models.Information;
import models.Timestamp;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static long getLastReadTime() {
        Information information = loadInformationFromFile();
        return information != null ? information.getTimestamp().getTime() : 1274434900;
    }

    public static void updateLastRead(long time) {
        Information information;
        information = loadInformationFromFile();
        if (information != null) {
            information.setTimestamp(time);
        } else {
            information = new Information(new Timestamp(time));
        }
        writeInformationToFile(information);
    }

    private static Information loadInformationFromFile() {
        Path path = Paths.get("information.txt");
        String fileContents;
        try {
            fileContents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("File information.txt does not exist");
            return null;
        }
        return new Gson().fromJson(fileContents, Information.class);
    }

    private static void writeInformationToFile(Information information) {
        try {
            FileWriter fileWriter = new FileWriter("information.txt");
            String jsonString = new Gson().toJson(information);
            fileWriter.write(jsonString);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
