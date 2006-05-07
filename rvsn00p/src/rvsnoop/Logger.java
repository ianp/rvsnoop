//:File:    Logger.java
//:Created: Jan 5, 2006
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import rvsnoop.ui.UIManager;
import rvsnoop.ui.UIUtils;

/**
 * A trivial logging facility.
 * <p>
 * I know that the app needs a logger but can't bring myself to commit to either
 * {@link java.util.logging} (because it's just not very good) or Log4J (because
 * it has a few warts as well, and is a big extra dependency). This class is
 * basically here to let me defer the decision for a while whilst writing
 * logging into the application.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class Logger {

    private static final int DEBUG = 5;
    private static final int INFO  = 4;
    private static final int WARN  = 3;
    private static final int ERROR = 2;
    private static final int FATAL = 1;
    private static final int OFF   = 0;

    private static final String[] LABELS = { "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG" };

    private static final String CONFIG_DIRECTORY = ".rvsnoop";

    private static final StringBuffer buffer = new StringBuffer();

    private static final Date date = new Date();

    private static boolean isRunningHeadless = true;

    // Be moderately verbose by default.
    private static int level = INFO;

    private static Writer writer;

    static {
        final String home = System.getProperty("user.home");
        final String fs = System.getProperty("file.separator");
        final String filename = "log-" + new SimpleDateFormat("yyyyMMddHH").format(new Date());
        try {
            final File file = new File(home + fs + CONFIG_DIRECTORY + fs + filename);
            file.getParentFile().mkdirs();
            writer = new FileWriter(file, true);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
        log("Logger", LABELS[INFO], Version.getAsStringWithName() + " started at " + StringUtils.format(new Date()) + ".", null);
        final String logLevel = System.getProperty("rvsnoop.logging");
        if (logLevel != null) setLevel(logLevel);
    }

    public static int getLevel() {
        return level;
    }

    public static Logger getLogger(Class clazz) {
        return new Logger(clazz);
    }

    private static synchronized void log(String simpleName, String level, String message, Throwable throwable) {
        if (Logger.level == OFF) return;
        date.setTime(System.currentTimeMillis());
        buffer.append(StringUtils.format(date)).append(" ").append(level);
        buffer.append(level.length() == 4 ? "  " : " ");
        buffer.append(simpleName).append(" - ").append(message);
        if (throwable != null) buffer.append(" [").append(throwable.getMessage()).append("]");
        try {
            writer.write(buffer.append("\n").toString());
            writer.flush();
        } catch (IOException e) {
            System.err.println("Could not log to file because: " + e.getMessage()); //NOPMD
        }
        if (throwable != null) throwable.printStackTrace(new PrintWriter(writer));
        buffer.setLength(0);
    }

    public static void setRunningHeadless(boolean isRunningHeadless) {
        Logger.isRunningHeadless = isRunningHeadless;
    }

    private final String simpleName;

    private Logger(Class clazz) {
        super();
        final String clazzName = clazz.getName();
        final int index = clazzName.lastIndexOf('.');
        simpleName = index > 0 ? clazzName.substring(index + 1) : clazzName;
    }

    public synchronized void debug(String message) {
        if (isDebugEnabled())
            log(simpleName, LABELS[DEBUG], message, null);
    }

    public synchronized void debug(String message, Throwable throwable) {
        if (isDebugEnabled())
            log(simpleName, LABELS[DEBUG], message, throwable);
    }

    public synchronized void error(String message) {
        if (isErrorEnabled())
            log(simpleName, LABELS[ERROR], message, null);
        if (!isRunningHeadless) {
            UIManager.INSTANCE.setStatusBarWarning(message);
            UIUtils.showError(message, null);
        }
    }

    public synchronized void error(String message, Throwable throwable) {
        if (isErrorEnabled())
            log(simpleName, LABELS[ERROR], message, throwable);
        if (!isRunningHeadless) {
            UIManager.INSTANCE.setStatusBarWarning(message);
            UIUtils.showError(message, throwable);
        }
    }

    public synchronized void fatal(String message) {
        if (isFatalEnabled())
            log(simpleName, LABELS[FATAL], message, null);
    }

    public synchronized void fatal(String message, Throwable throwable) {
        if (isFatalEnabled())
            log(simpleName, LABELS[FATAL], message, throwable);
    }

    public synchronized void info(String message) {
        if (isInfoEnabled())
            log(simpleName, LABELS[INFO], message, null);
        if (!isRunningHeadless)
            UIManager.INSTANCE.setStatusBarMessage(message);
    }

    public synchronized void info(String message, Throwable throwable) {
        if (isInfoEnabled())
            log(simpleName, LABELS[INFO], message, throwable);
        if (!isRunningHeadless)
            UIManager.INSTANCE.setStatusBarMessage(message);
    }

    public static boolean isDebugEnabled() {
        return level >= DEBUG;
    }

    public static boolean isErrorEnabled() {
        return level >= ERROR;
    }

    private static boolean isFatalEnabled() {
        return level >= FATAL;
    }

    public static boolean isInfoEnabled() {
        return level >= INFO;
    }

    public static boolean isWarnEnabled() {
        return level >= WARN;
    }

    public static void setLevel(int level) {
        if (level < 0 || level >= LABELS.length) throw new IndexOutOfBoundsException();
        log("Logger", LABELS[INFO], "Setting log level to " + LABELS[level] + ".", null);
        Logger.level = level;
    }

    public static void setLevel(String level) {
        for (int i = 0, imax = LABELS.length; i < imax; ++i) {
            if (LABELS[i].equalsIgnoreCase(level)) {
                setLevel(i);
                return;
            }
        }
        if (isErrorEnabled())
            log("Logger", LABELS[ERROR], "Not a valid log level: " + level + ".", null);
    }

    public synchronized void warn(String message) {
        log(simpleName, LABELS[WARN], message, null);
        if (!isRunningHeadless)
            UIManager.INSTANCE.setStatusBarWarning(message);
    }

    public synchronized void warn(String message, Throwable throwable) {
        log(simpleName, LABELS[WARN], message, throwable);
        if (!isRunningHeadless)
            UIManager.INSTANCE.setStatusBarWarning(message);
    }
}
