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

/**
 * utility class
 */
public class Utils {

    /**
     * Somewhat mimics a very basic database by storing a timestamp in a text file in JSON format
     * update the last read time
     * @param time time of last read
     */
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

    /**
     * returns the last time the tool ran
     * @return either the timestamp saved in the text file
     * or a static date that is certain to be in the past
     */
    public static long getLastReadTime() {
        Information information = loadInformationFromFile();
        return information != null ? information.getTimestamp().getTime() : 1274434900;
    }

    /**
     * loads the information object stored in a text file
     * @return the information object loaded
     */
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

    /**
     * replaces the stored information by a new instance
     * @param information the new information object to store
     */
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