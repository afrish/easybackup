package com.nucleo.easybackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamPrinter extends Thread {
    
    private InputStream is;
    private Logger log;

    public StreamPrinter(InputStream is, Logger log) {
        this.is = is;
        this.log = log;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                log.println(line);
            }
            is.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}