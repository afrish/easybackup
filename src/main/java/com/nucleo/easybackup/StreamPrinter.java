package com.nucleo.easybackup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class StreamPrinter extends Thread {
    
    private InputStream is;
    private Logger log;

    public StreamPrinter(InputStream is, Logger log) {
        this.is = is;
        this.log = log;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            int c;
            while ((c = br.read()) != -1) {
                log.print((char) c);
            }
            is.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}