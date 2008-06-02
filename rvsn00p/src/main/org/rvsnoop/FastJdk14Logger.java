/*
 * Class:     FastJdk14Logger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

/**
 * A fast implementation of the {@link Log} interface.
 * <p>
 * Unlike the standard Commons Logging version, this does not generate a stack
 * trace for every log entry. It also assumes that the logger name is a class
 * name (i.e. it has been created with a call to
 * {@link org.apache.commons.logging.LogFactory#getLog(Class)}.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class FastJdk14Logger implements Log, Serializable {

    private static final long serialVersionUID = -1567993728374458533L;

    static {
        // Configure the logging framework from our properties file.
        try {
            LogManager.getLogManager().readConfiguration(
                    ClassLoader.getSystemClassLoader()
                    .getResourceAsStream("commons-logging.properties"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /** The JDK logger being wrapped. */
    private transient Logger logger;

    /** The name of the logger being wrapping. */
    private final String name;

    /**
     * Create a new named Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public FastJdk14Logger(String name) {
        this.name = name != null ? name : "";
    }

    /** Create a new anonymous logger. */
    public FastJdk14Logger() {
        this.name = "";
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINE</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINE</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message));
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message));
    }

    /**
     * Logs a message with <code>java.util.logging.Level.SEVERE</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Lazily load the logger that is being wrapped.
     *
     * @return The wrapped logger.
     */
    private synchronized Logger getLogger() {
        if (logger == null) {
            logger = name.length() > 0
                ? Logger.getLogger(name)
                : Logger.getAnonymousLogger();
        }
        return logger;
    }

    /**
     * Logs a message with <code>java.util.logging.Level.INFO</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message));
    }

    /**
     * Logs a message with <code>java.util.logging.Level.INFO</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {
        return (getLogger().isLoggable(Level.FINE));
    }

    /**
     * Is error logging currently enabled?
     */
    public boolean isErrorEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }

    /**
     * Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }

    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {
        return (getLogger().isLoggable(Level.INFO));
    }

    /**
     * Is trace logging currently enabled?
     */
    public boolean isTraceEnabled() {
        return (getLogger().isLoggable(Level.FINEST));
    }

    /**
     * Is warn logging currently enabled?
     */
    public boolean isWarnEnabled() {
        return (getLogger().isLoggable(Level.WARNING));
    }

    private void log(Level level, String message, Throwable thrown) {
        final Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            if(thrown == null) {
                logger.log(level, message);
            } else {
                logger.log(level, message, thrown);
            }
        }
    }

    private void log(Level level, String message) {
        final Logger logger = getLogger();
        if (logger.isLoggable(level)) { logger.log(level, message); }
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINEST</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message));
    }

    /**
     * Logs a message with <code>java.util.logging.Level.FINEST</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }

    /**
     * Logs a message with <code>java.util.logging.Level.WARNING</code>.
     *
     * @param message The message to log.
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message));
    }

    /**
     * Logs a message with <code>java.util.logging.Level.WARNING</code>.
     *
     * @param message The message to log.
     * @param exception The exception to log.
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }

}
