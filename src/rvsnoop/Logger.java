//:File:    Logger.java
//:Created: Jan 5, 2006
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import rvsn00p.viewer.RvSnooperGUI;
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

    private static class NullWriter extends Writer {
        NullWriter() {
            super();
        }
        public void close() throws IOException {
            // Do nothing.
        }
        public void flush() throws IOException {
            // Do nothing.
        }
        public void write(char[] cbuf, int off, int len) throws IOException {
            // Do nothing.
        }
    }

    private static final int DEBUG = 5;
    private static final int INFO  = 4;
    private static final int WARN  = 3;
    private static final int ERROR = 2;
    private static final int FATAL = 1;
    private static final int OFF   = 0;
    
    private static final String DEBUG_LABEL = "DEBUG";
    private static final String INFO_LABEL  = "INFO";
    private static final String WARN_LABEL  = "WARN";
    private static final String ERROR_LABEL = "ERROR";
    private static final String FATAL_LABEL = "FATAL";

    private static final String CONFIG_DIRECTORY = ".rvsnoop";

    private static final StringBuffer buffer = new StringBuffer();

    private static final Date date = new Date();

    private static final int level;
    
    private static final Writer writer;
    
    static {
        final String s = System.getProperty("rvsnoop.logging");
        if ("debug".equals(s))
            level = DEBUG;
        else if ("info".equals(s))
            level = INFO;
        else if ("warn".equals(s))
            level = WARN;
        else if ("error".equals(s))
            level = ERROR;
        else if ("fatal".equals(s))
            level = FATAL;
        else if ("off".equals(s))
            level = OFF;
        else
            level = INFO;
        if (level == OFF) {
            writer = new NullWriter();
        } else {
            final String home = System.getProperty("user.home");
            final String fs = System.getProperty("file.separator");
            final String filename = "log-" + new SimpleDateFormat("yyyyMMddHH").format(new Date());
            try {
                final File file = new File(home + fs + CONFIG_DIRECTORY + fs + filename);
                final FileWriter fw = new FileWriter(file, true);
                if (Boolean.getBoolean("rvsnoop.logging.buffered"))
                    writer = new BufferedWriter(fw);
                else
                    writer = fw;
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        log("Logger", INFO_LABEL, Version.getAsStringWithName() + " started at " + StringUtils.format(new Date()) + ".", null);
        log("Logger", INFO_LABEL, "Log level set to " + (s != null ? s : "info") + ".", null);
    }

    public static void flush() throws IOException {
        writer.flush();
    }
    
    public static Logger getLogger(Class clazz) {
        return new Logger(clazz);
    }
    
    private static synchronized void log(String simpleName, String level, String message, Throwable throwable) {
        date.setTime(System.currentTimeMillis());
        buffer.append(StringUtils.format(date)).append(" ").append(level);
        buffer.append(level.length() == 4 ? "  " : " ");
        buffer.append(simpleName).append(" - ").append(message);
        if (throwable != null) buffer.append(" [").append(throwable.getMessage()).append("]");
        try {
            writer.write(buffer.append("\n").toString());
        } catch (IOException e) {
            // @PMD:REVIEWED:SystemPrintln: by ianp on 1/5/06 8:58 PM
            System.err.println("Could not log to file because: " + e.getMessage());
        }
        if (throwable != null) throwable.printStackTrace(new PrintWriter(writer));
        buffer.setLength(0);
    }
    
    private final String simpleName;

    private Logger(Class clazz) {
        super();
        final String clazzName = clazz.getName();
        final int index = clazzName.lastIndexOf('.');
        simpleName = index > 0 ? clazzName.substring(index + 1) : clazzName;
    }

    public synchronized void debug(String message) {
        log(simpleName, DEBUG_LABEL, message, null);
    }

    public synchronized void debug(String message, Throwable throwable) {
        log(simpleName, DEBUG_LABEL, message, throwable);
    }

    public synchronized void error(String message) {
        log(simpleName, ERROR_LABEL, message, null);
        RvSnooperGUI.setStatusBarWarning(message);
        UIUtils.showError(message, null);
    }

    public synchronized void error(String message, Throwable throwable) {
        log(simpleName, ERROR_LABEL, message, throwable);
        RvSnooperGUI.setStatusBarWarning(message);
        UIUtils.showError(message, throwable);
    }

    public synchronized void fatal(String message) {
        log(simpleName, FATAL_LABEL, message, null);
    }
    
    public synchronized void fatal(String message, Throwable throwable) {
        log(simpleName, FATAL_LABEL, message, throwable);
    }
    
    public synchronized void info(String message) {
        RvSnooperGUI.setStatusBarMessage(message);
        log(simpleName, INFO_LABEL, message, null);
    }
    
    public synchronized void info(String message, Throwable throwable) {
        RvSnooperGUI.setStatusBarMessage(message);
        log(simpleName, INFO_LABEL, message, throwable);
    }
    
    public boolean isDebugEnabled() {
        return level >= DEBUG;
    }
    
    public boolean isErrorEnabled() {
        return level >= ERROR;
    }
    
    public boolean isFatalEnabled() {
        return level >= FATAL;
    }
    
    public boolean isInfoEnabled() {
        return level >= INFO;
    }
    
    public boolean isWarnEnabled() {
        return level >= WARN;
    }
    
    public synchronized void warn(String message) {
        RvSnooperGUI.setStatusBarWarning(message);
        log(simpleName, WARN_LABEL, message, null);
    }
    
    public synchronized void warn(String message, Throwable throwable) {
        RvSnooperGUI.setStatusBarWarning(message);
        log(simpleName, WARN_LABEL, message, throwable);
    }
}
