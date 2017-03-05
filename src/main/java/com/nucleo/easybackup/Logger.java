package com.nucleo.easybackup;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger implements Closeable {
    public static final String LOG_EXTENSION = ".log";
    private static final SimpleDateFormat LOG_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private PrintWriter writer;
    
    public Logger(Path dest, String name) throws FileNotFoundException, UnsupportedEncodingException {
        File logFile = dest.resolve(name + LOG_EXTENSION).toFile();
        writer = new PrintWriter(logFile, "UTF-8");
    }
    
    public void println(String line) {
        line = now() + "  " + line;
        System.out.println(line);
        writer.println(line);
    }
    
    public void println() {
        String line = now();
        System.out.println(line);
        writer.println(line);
    }

    public void print(char ch) {
        System.out.print(ch);
        writer.print(ch);
    }

    private String now() {
        return LOG_DATE_TIME_FORMAT.format(new Date());
    }
    
    public void close() {
        writer.close();
    }
    
    
}
