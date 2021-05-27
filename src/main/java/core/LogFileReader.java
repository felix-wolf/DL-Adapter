package core;

import helper.Utils;
import io.reactivex.rxjava3.core.Observable;
import models.Operation;
import org.apache.commons.lang3.StringUtils;
import timer.TimerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class LogFileReader {

    private Date lastRead = null;

    public LogFileReader() {
        initialiseTimer();
    }

    private void initialiseTimer() {
        TimerUtil timer = new TimerUtil(5000);
        timer.startTimer(fireCounts -> {
            System.out.println("Fired, count: " + fireCounts);
            lastRead = new Date(Utils.getLastReadTime());
            ArrayList<String> newEntries = getNewDatabaseEntries();
            if (!newEntries.isEmpty()) {
                ArrayList<Operation> convertedEvents = new LogConverter().convertLogs(newEntries);
                try {
                    EventProducer.produceEvents(convertedEvents);
                    Utils.updateLastRead(System.currentTimeMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // stop timer
            if (fireCounts == -1) {
                timer.stopTimer();
            }
        });
    }

    private ArrayList<String> getNewDatabaseEntries() {
        ArrayList<String> logs = new ArrayList<>();
        for (File file : getValidLogFiles()) {
            if (file.exists()) {
                ArrayList<String> logsOfFile = extractModifyingSQLStatements(file);
                logsOfFile.removeIf(log -> {
                    String[] parts = log.split(Pattern.quote(" ["));
                    Date date = Utils.getDateFromLogString(parts[0]);
                    return lastRead != null && date != null && date.before(lastRead);
                });
                logs.addAll(logsOfFile);
            }
        }
        return logs;
    }

    private List<File> getValidLogFiles() {
        long lastRead = Utils.getLastReadTime();
        File folder = new File("../library-assistant/logs");

        return Observable.fromArray(folder.listFiles())
                .sorted()
                .filter(localFile -> {
                            String dateString = StringUtils.substringBetween(localFile.getName(), "logs", ".log");
                            if (dateString != null) {
                                dateString = dateString.split(Pattern.quote("_"))[1];
                                SimpleDateFormat format = new SimpleDateFormat("d-MM-yyyy");
                                Date fileDate = format.parse(dateString);
                                Date lastReadDate = new Date(lastRead);
                                return fileDate.after(lastReadDate);
                            }
                            return false;
                        }
                ).toList().blockingGet();
    }

    private ArrayList<String> extractModifyingSQLStatements(File file) {
        ArrayList<String> logs = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.equals("") && line.contains("jdbc.sqlonly")) logs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logs.removeIf(line -> line.contains("SELECT"));
        return logs;
    }

}
