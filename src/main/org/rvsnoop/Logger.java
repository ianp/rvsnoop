// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import org.apache.log4j.Level;
import org.jdesktop.application.ResourceMap;

/**
 * A simple Log4J wrapper that provides a nicer API.
 */
public class Logger {

    public static Logger getLogger() {
        String name = new Exception().getStackTrace()[1].getClassName();
        return new Logger(name);
    }

    private final org.apache.log4j.Logger delegate;
    
    private Logger(String name) {
        delegate = org.apache.log4j.Logger.getLogger(name);
    }

    public void debug(String key, Object... args) {
        log(Level.DEBUG, null, key, args);
    }

    public void debug(Throwable t, String key, Object... args) {
        log(Level.DEBUG, t, key, args);
    }

    public void error(String key, Object... args) {
        log(Level.ERROR, null, key, args);
    }

    public void error(Throwable t, String key, Object... args) {
        log(Level.ERROR, t, key, args);
    }

    public void info(String key, Object... args) {
        log(Level.INFO, null, key, args);
    }

    public void info(Throwable t, String key, Object... args) {
        log(Level.INFO, t, key, args);
    }

    public void warn(String key, Object... args) {
        log(Level.WARN, null, key, args);
    }

    public void warn(Throwable t, String key, Object... args) {
        log(Level.WARN, t, key, args);
    }

    private void log(Level level, Throwable t, String formatString, Object... args) {
        if (!delegate.isEnabledFor(level)) { return; }
        String message = args != null && args.length != 0 ? String.format(formatString, args) : formatString;
        delegate.log(level, message, t);
    }

}
