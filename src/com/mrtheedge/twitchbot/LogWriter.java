package com.mrtheedge.twitchbot;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by E.J. Schroeder on 2/2/2016.
 *
 * Creates a new log file with a timestamp, writing the data that is passed to it.
 */
public class LogWriter {

    // Formatter for filenames with dates
    private static DateTimeFormatter fileDateFormat = DateTimeFormat.forPattern("YYYY-MM-dd_HH-mm-ss");

    public static void writeLogs(String text){
        DateTime time = DateTime.now();
        File logDir = new File("logs"); // Create directory for log files
        logDir.mkdir();
        File logFile = new File("logs/" + fileDateFormat.print(time) + ".log"); // Create dated log file

        try {
            FileOutputStream out = new FileOutputStream(logFile);
            out.write(text.getBytes()); // Write log text to the file
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException");
            e.printStackTrace();
            return;
        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
            return;
        }


    }

}
