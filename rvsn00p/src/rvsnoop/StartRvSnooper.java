//:File:    StartRvSnooper.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rvsn00p.viewer.RvSnooperGUI;
import rvsnoop.ui.MultiLineToolTipUI;

/**
 * Starts an instance of RvSnoop.
 * <p>
 * This is just a collection of bootstrap methods to ensure that the environment
 * is suitable and parse the command line arguments.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor
 * 5</a>.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class StartRvSnooper {
    
    private static final Logger logger = Logger.getLogger(StartRvSnooper.class);

    /**
     * Checks whether the JVM version is suitable.
     * 
     * @return <code>true</code> if the JVM is OK, <code>false</code> otherwise.
     */
    private static boolean isCorrectJavaVersion() {
        final String ver = System.getProperty("java.version");
        try {
            final StringTokenizer st = new StringTokenizer(ver, "._-");
            final int a = Integer.parseInt(st.nextToken());
            final int b = Integer.parseInt(st.nextToken());
            if (a > 1) return true;
            if (b > 4) return true;
            return st.hasMoreTokens() && Integer.parseInt(st.nextToken()) > 1;
        } catch(Exception e) {
            if (logger.isWarnEnabled()) logger.warn("Unable to determine Java version.", e);
            return true;
        }
    }

    /**
     * The application entry point.
     * 
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        final String toolTipUI = MultiLineToolTipUI.class.getName();
        UIManager.put("ToolTipUI", toolTipUI);
        UIManager.put(toolTipUI, MultiLineToolTipUI.class);
        if (!isCorrectJavaVersion() && logger.isWarnEnabled())
            logger.warn("Java version 1.4.2 or higher is required, rvSnoop may fail unexpectedly with earlier versions.");
        final ArgParser parser = new ArgParser(Version.getAsStringWithName());
        parser.addArgument('h', "help", true, "Display a short help message.");
        parser.addArgument('p', "project", false, "Load a project file on startup.");
        parser.parseArgs(args);
        if (parser.getBooleanArg("help"))
            parser.printUsage(System.out);
        final String filename = parser.getStringArg("project");
        if (filename != null && logger.isDebugEnabled()) logger.debug("Loading project file: " + filename);
        final File file = filename == null ? null : new File(filename);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RvSnooperGUI().show();
                PreferencesManager.INSTANCE.load();
                if (file != null && file.exists() && file.canRead()) {
                    try {
                        final Project project = new Project(file.getCanonicalFile());
                        project.load();
                        RecentProjects.INSTANCE.add(project);
                    } catch(IOException e) {
                        logger.error("Could not load project from " + filename, e);
                    }
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    PreferencesManager.INSTANCE.store();
                    RecentConnections.INSTANCE.store();
                    RecentProjects.INSTANCE.store();
                    logger.info(Version.getAsStringWithName() + " stopped at " + StringUtils.format(new Date()) + ".");
                    Logger.flush();
                } catch (IOException ignored) {
                    // Oh well, we may lose the preferences then.
                }
            }
        }, "shutdownHook"));
    }
    
    /**
     * Do not instantiate.
     */
    private StartRvSnooper() {
        throw new UnsupportedOperationException();
    }

}
