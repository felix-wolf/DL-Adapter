package core;

import timer.TimerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class LogFileReader {

    Date lastRead = null;

    public LogFileReader() {
        initialiseTimer();
    }

    private void initialiseTimer() {
        TimerUtil timer = new TimerUtil(5000);
        timer.startTimer(fireCounts -> {
            System.out.println("Fired, count: " + fireCounts);
            // System.out.println(getNewDatabaseEntries() != null && !getNewDatabaseEntries().isEmpty());
            ArrayList<String> newEntries = getNewDatabaseEntries();
            if (newEntries != null && newEntries.isEmpty()) {
                lastRead = new Date(System.currentTimeMillis());

            }
            // process books
            if (fireCounts == 3) {
                timer.stopTimer();
            }
        });
    }

    private ArrayList<String> getNewDatabaseEntries() {
        File file = getLogFileOfToday();
        if (file.exists()) {
            ArrayList<String> logs = extractModifyingSQLStatements(file);
            logs.removeIf(log -> {
                String[] parts = log.split(Pattern.quote(" ["));
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d HH:mm:ss.S");
                try {
                    Date data = format.parse(parts[0]);
                    return lastRead != null && data.before(lastRead);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            });
            return logs;
        } else {
            return null;
        }
    }

    private File getLogFileOfToday() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("d-MM-yyyy");
        String formattedDate = format.format(date);
        return new File("../library-assistant/logs/logs_" + formattedDate + ".log");
    }

    private ArrayList<String> extractModifyingSQLStatements(File file) {
        ArrayList<String> logs = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                if (!line.equals("")) {
                    if (line.contains("jdbc.sqlonly")) logs.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!logs.isEmpty()) {
            logs.removeIf(line -> line.contains("SELECT"));
        }
        return logs;
    }

}
