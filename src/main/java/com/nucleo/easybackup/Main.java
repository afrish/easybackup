package com.nucleo.easybackup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        String name = args[0];
        Path src = Paths.get(args[1]);
        Path dest = Paths.get(args[2]);
        long retain = Long.parseLong(args[3]);

        File destAsFile = dest.toFile();
        destAsFile.mkdirs();
                
        String command = buildBackupCommand(args, name, src, dest);
        executeBackup(command);
        cleanOldBackups(retain, dest);

    }

    private static boolean executeBackup(String command) {
        System.out.println("Making backup\n" + command);

        try {
            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
            new StreamPrinter(p.getErrorStream()).start();
            new StreamPrinter(p.getInputStream()).start();

            int exitVal = p.waitFor();
            boolean success = exitVal == 0;
            System.out.println("Exit value: " + exitVal + " (" + (success ? "success" : "failure") + ")");
            
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
    }

    private static String buildBackupCommand(String[] args, String name, Path src, Path dest) {
        Type type = Type.valueOf(args[4].toUpperCase());
        String command = null;
        String newBackupPath = null;
        switch (type) {
        case TAR: 
            newBackupPath = dest.resolve(name + "_`date '+%Y_%m_%d__%H_%M_%S'`.tar.gz").toString();
            command = "tar cvzf " + newBackupPath + " " + src;
            break;
        case SQSH:
            newBackupPath = dest.resolve(name + "_`date '+%Y_%m_%d__%H_%M_%S'`.sqsh").toString();
            command = "mksquashfs " + src + " " + newBackupPath;
            break;
        }
        return command;
    }

    private static void cleanOldBackups(long retain, Path dest) {
        List<File> backups = new LinkedList<File>(Arrays.asList(dest.toFile().listFiles()));
        Collections.sort(backups);
        while (backups.size() > retain) {
            System.out.println("Removing old backup\n" + backups.get(0));
            backups.get(0).delete();
            backups.remove(0);
        }
    }
    
    enum Type { TAR, SQSH }

}
