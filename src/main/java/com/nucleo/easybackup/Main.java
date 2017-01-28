package com.nucleo.easybackup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ml.options.OptionData;
import ml.options.OptionSet;
import ml.options.Options;
import ml.options.Options.Multiplicity;
import ml.options.Options.Separator;

public class Main {

    private static final String DATE_TIME_FORMAT = "`date '+%Y_%m_%d__%H_%M_%S'`";

    public static void main(String[] args) throws IOException {
        Options options = new Options(args, 2);
        OptionSet set = options.getSet();
        set.addOption("n", Separator.EQUALS, Multiplicity.ONCE);
        set.addOption("t", Separator.EQUALS, Multiplicity.ONCE);
        set.addOption("r", Separator.EQUALS, Multiplicity.ONCE);
        set.addOption("e", Separator.EQUALS, Multiplicity.ZERO_OR_MORE);
        
        if (!options.check()) {
            System.out.println(options.getCheckErrors());
            System.exit(1);
        }
        
        String name = set.getOption("n").getResultValue(0);
        Type type = Type.valueOf(set.getOption("t").getResultValue(0).toUpperCase());
        int retain = Integer.parseInt(set.getOption("r").getResultValue(0));
        
        List<String> excludes = new ArrayList<>();
        OptionData excludeOption = set.getOption("e");
        for (int i = 0; i < excludeOption.getResultCount(); i++) {
            excludes.add(excludeOption.getResultValue(i));
        }
        
        Path src = Paths.get(set.getData().get(0));
        Path dest = Paths.get(set.getData().get(1));

        File destAsFile = dest.toFile();
        destAsFile.mkdirs();
                
        String command = buildBackupCommand(type, name, src, dest, excludes);
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

    private static String buildBackupCommand(Type type, String name, Path src, Path dest, List<String> excludes) {
        String command = null;
        String newBackupPath = null;
        switch (type) {
        case TAR: 
            newBackupPath = dest.resolve(name + "_" + DATE_TIME_FORMAT + ".tar.gz").toString();
            command = "tar cvzf " + newBackupPath + " " + src;
            for (String exclude : excludes) {
                command += " --exclude=" + exclude;
            }
            break;
        case SQSH:
            newBackupPath = dest.resolve(name + "_" + DATE_TIME_FORMAT + ".sqsh").toString();
            command = "mksquashfs " + src + " " + newBackupPath;
            if (!excludes.isEmpty()) {
                command += " -e";
                for (String exclude : excludes) {
                    command += " " + exclude;
                }    
            }
            break;
        }
        return command;
    }

    private static void cleanOldBackups(int retain, Path dest) {
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
