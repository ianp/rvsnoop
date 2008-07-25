/*
 * Class:     Logger
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2008 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */

package org.rvsnoop;

import org.apache.log4j.Level;
import org.jdesktop.application.ResourceMap;

/**
 * A logger that integrates JSR-296 resource maps and Log4J.
 * <p>
 * The opportunity has also been taken to enforce some best practices.
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public class Logger {

    private final org.apache.log4j.Logger delegate;
    
    public Logger() {
        String name = new Exception().getStackTrace()[1].getClassName();
        delegate = org.apache.log4j.Logger.getLogger(name);
    }

    public void debug(ResourceMap resourceMap, String key, Object... args) {
        if (!delegate.isEnabledFor(Level.DEBUG)) { return; }
        delegate.debug(resourceMap.getString(key, args));
    }

    public void debug(ResourceMap resourceMap, String key, Throwable t, Object... args) {
        if (!delegate.isEnabledFor(Level.DEBUG)) { return; }
        delegate.debug(resourceMap.getString(key, args), t);
    }

    public void error(ResourceMap resourceMap, String key, Object... args) {
        if (!delegate.isEnabledFor(Level.ERROR)) { return; }
        delegate.error(resourceMap.getString(key, args));
    }

    public void error(ResourceMap resourceMap, String key, Throwable t, Object... args) {
        if (!delegate.isEnabledFor(Level.ERROR)) { return; }
        delegate.error(resourceMap.getString(key, args), t);
    }

    public void fatal(ResourceMap resourceMap, String key, Object... args) {
        if (!delegate.isEnabledFor(Level.FATAL)) { return; }
        delegate.fatal(resourceMap.getString(key, args));
    }

    public void fatal(ResourceMap resourceMap, String key, Throwable t, Object... args) {
        if (!delegate.isEnabledFor(Level.FATAL)) { return; }
        delegate.fatal(resourceMap.getString(key, args), t);
    }

    public void info(ResourceMap resourceMap, String key, Object... args) {
        if (!delegate.isEnabledFor(Level.INFO)) { return; }
        delegate.info(resourceMap.getString(key, args));
    }

    public void info(ResourceMap resourceMap, String key, Throwable t, Object... args) {
        if (!delegate.isEnabledFor(Level.INFO)) { return; }
        delegate.info(resourceMap.getString(key, args), t);
    }

    public void warn(ResourceMap resourceMap, String key, Object... args) {
        if (!delegate.isEnabledFor(Level.WARN)) { return; }
        delegate.warn(resourceMap.getString(key, args));
    }

    public void warn(ResourceMap resourceMap, String key, Throwable t, Object... args) {
        if (!delegate.isEnabledFor(Level.WARN)) { return; }
        delegate.warn(resourceMap.getString(key, args), t);
    }

}
