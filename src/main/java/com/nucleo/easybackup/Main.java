package com.nucleo.easybackup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ml.options.OptionData;
import ml.options.OptionSet;
import ml.options.Options;
import ml.options.Options.Multiplicity;
import ml.options.Options.Separator;

public class Main {

    private static final String SQUASH_EXTENSION = ".sqsh";
    private static final String TAR_EXTENSION = ".tar.gz";
    private static final SimpleDateFormat NAME_DATE_TIME_FORMAT = new SimpleDateFormat("__yyyy_MM_dd__HH_mm_ss");

    public static void main(String[] args) throws IOException, InterruptedException {
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

        String name = set.getOption("n").getResultValue(0) + NAME_DATE_TIME_FORMAT.format(new Date());
        Type type = Type.valueOf(set.getOption("t").getResultValue(0).toUpperCase());
        int retain = Integer.parseInt(set.getOption("r").getResultValue(0));

        List<String> excludes = new ArrayList<>();
        OptionData excludeOption = set.getOption("e");
        for (int i = 0; i < excludeOption.getResultCount(); i++) {
            excludes.add(excludeOption.getResultValue(i));
        }

        Path src = Paths.get(set.getData().get(0));
        Path dest = Paths.get(set.getData().get(1));

        dest.toFile().mkdirs();

        try (Logger log = new Logger(dest, name)) {
            String command = buildBackupCommand(type, name, src, dest, excludes);
            executeBackup(command, log);
            cleanOldBackups(retain, dest, log);
        }
    }

    private static void executeBackup(String command, Logger log) throws IOException, InterruptedException {
        log.println("Starting backup command: " + command);
        log.println();

        Timer timer = new Timer();
        timer.start();

        Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });

        StreamPrinter errPrinter = new StreamPrinter(p.getErrorStream(), log);
        StreamPrinter stdPrinter = new StreamPrinter(p.getInputStream(), log);
        errPrinter.start();
        stdPrinter.start();

        int exitVal = p.waitFor();
        errPrinter.join();
        stdPrinter.join();

        timer.stop();
        log.println();
        log.println("====================================================");
        log.println("Backup operation took " + timer.getDurationAsString());

        boolean success = exitVal == 0;
        log.println("Exit value: " + exitVal + " (" + (success ? "success" : "failure") + ")");
        log.println("====================================================");
        log.println();
    }

    private static String buildBackupCommand(Type type, String name, Path src, Path dest, List<String> excludes) {
        String command = null;
        String backupFilePath = null;
        switch (type) {
        case TAR:
            backupFilePath = dest.resolve(name + TAR_EXTENSION).toString();
            command = "tar cvzf \"" + backupFilePath + "\" \"" + src + "\"";
            for (String exclude : excludes) {
                command += " --exclude=\"" + exclude + "\"";
            }
            break;
        case SQSH:
            backupFilePath = dest.resolve(name + SQUASH_EXTENSION).toString();
            command = "mksquashfs \"" + src + "\" \"" + backupFilePath + "\"";
            if (!excludes.isEmpty()) {
                command += " -e";
                for (String exclude : excludes) {
                    command += " \"" + exclude + "\"";
                }
            }
            break;
        }
        return command;
    }

    private static void cleanOldBackups(int retain, Path dest, Logger log) {
        cleanOldFiles(retain, dest, (dir, name) -> name.endsWith(TAR_EXTENSION) || name.endsWith(SQUASH_EXTENSION), log,
                "Removing old backup: ");
        cleanOldFiles(retain, dest, (dir, name) -> name.endsWith(Logger.LOG_EXTENSION), log, "Removing old log file: ");
    }

    private static void cleanOldFiles(int retain, Path dest, FilenameFilter filter, Logger log, String message) {
        List<File> files = new LinkedList<File>(Arrays.asList(dest.toFile().listFiles(filter)));
        Collections.sort(files);
        while (files.size() > retain) {
            log.println(message + files.get(0));
            files.get(0).delete();
            files.remove(0);
        }
    }

    enum Type {
        TAR, SQSH
    }

}
