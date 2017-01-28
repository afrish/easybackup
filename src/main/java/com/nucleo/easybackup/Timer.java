package com.nucleo.easybackup;

import java.util.concurrent.TimeUnit;

public class Timer {
    
    private long start;
    private long finish;
    
    public Timer() { 
    }
    
    public void start() {
        start = System.currentTimeMillis();
    }
    
    public void stop() {
        finish = System.currentTimeMillis();
    }
    
    public long getDuration() {
        return finish - start;
    }
    
    public String getDurationAsString() {
        return durationToString(getDuration());
    }
    
    private static String durationToString(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours);
            sb.append(" h ");
        }
        if (minutes > 0) {
            sb.append(minutes);
            sb.append(" min ");
        }
        if (seconds > 0) {
            sb.append(seconds);
            sb.append(" sec ");
        }
        sb.append(millis);
        sb.append(" ms");

        return sb.toString();
    }

}
