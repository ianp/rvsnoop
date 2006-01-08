//:File:    StartRvSnooper.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import rvsn00p.viewer.RvSnooperGUI;

/**
 * Starts an instance of RvSn00p.
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

//    private static void displayUsageAndExit() {
//        System.out.print(Version.getAsStringWithName());
//        System.out.println("Usage: java rvsnoop.StartRvSnooper [-title <t>] [-{textonly|treeonly}]");
//        System.out.println("");
//        System.out.println("  -title    : set the window title to <title>.");
//        System.out.println("  -treeonly : show only the tree view of message details (new-style).");
//        System.out.println("  -textonly : show only the text view of message details (old-style).");
//        System.exit(-1);
//    }

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
    public static void main(String[] args) {
//        args = args != null ? args : new String[0];
//        int startAt = 0;
//        String title = null;
//        boolean text = true, tree = true;
//
//        while (startAt < args.length && args[startAt].charAt(0) == '-') {
//            final String arg = args[startAt++].substring(1);
//            if ("h".equals(arg)) {
//                displayUsageAndExit();
//            } else if ("title".equals(arg)) {
//                title = args[startAt++];
//            } else if ("textonly".equals(arg)) {
//                tree = false;
//            } else if ("treeonly".equals(arg)) {
//                text = false;
//            }
//        }
//
//        if (!text && !tree)
//            displayUsageAndExit();
        if (!isCorrectJavaVersion() && logger.isWarnEnabled())
            logger.warn("Java version 1.4.2 or higher is required, RvSnoop may fail unexpectedly with earlier versions.");

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new RvSnooperGUI().show();
                PreferencesManager.getInstance().load();
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    PreferencesManager.getInstance().store();
                    RecentConnections.getInstance().store();
                    RecentProjects.getInstance().store();
                    logger.info(Version.getAsStringWithName() + " stopped at " + StringUtils.format(new Date()) + ".");
                    Logger.flush();
                } catch (IOException ignored) {
                    // Oh well, lose the preferences then.
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
