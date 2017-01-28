package com.nucleo.easybackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StreamPrinter extends Thread {
    private static final SimpleDateFormat LOG_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private InputStream is;

    public StreamPrinter(InputStream is) {
        this.is = is;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(LOG_DATE_TIME_FORMAT.format(new Date()) + "  " + line);
            }
            is.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}