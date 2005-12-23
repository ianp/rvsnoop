//:File:    StartRvSnooper.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import rvsn00p.util.rv.RvParameters;
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

    private static void displayUsageAndExit() {
        System.out.print(RvSnooperGUI.VERSION);
        System.out.println("Usage: java rvsn00p.StartRvSnooper [-title <t>] [-{textonly|treeonly}] [<daemon>|<service>|<network>|<subjects>] ...");
        System.out.println("");
        System.out.println("  -title    : set the window title to <title>.");
        System.out.println("  -treeonly : show only the tree view of message details (new-style).");
        System.out.println("  -textonly : show only the text view of message details (old-style).");
        System.out.println("  <service> : the Rendezvous service parameter.");
        System.out.println("  <network> : the Rendezvous network parameter.");
        System.out.println("  <daemon>  : the Rendezvous daemon parameter.");
        System.out.println("  <subjects>: a comma-separated list of Rendezvous subject patterns.");
        System.out.println("");
        System.out.println("Example: java rvsn00p.StartRvSnooper \"tcp:7500|7500||a.>,c.x\" \"tcp:7500|7501||b.>,q.b\"");
        System.exit(-1);
    }

    /**
     * Checks whether the JVM version is suitable.
     * 
     * @return <code>true</code> if the JVM is OK, <code>false</code> otherwise.
     */
    private static boolean isCorrectJavaVersion() {
        String ver = System.getProperty("java.version");
        try {
            StringTokenizer st = new StringTokenizer(ver, "._-");
            int a = Integer.parseInt(st.nextToken());
            int b = Integer.parseInt(st.nextToken());
            if (a > 1) return true;
            if (b > 4) return true;
            return st.hasMoreTokens() && Integer.parseInt(st.nextToken()) > 1;
        } catch(Exception e) {
            System.err.println("Warning: unable to determine Java version.");
            return true;
        }
    }

    /**
     * The application entry point.
     * 
     * @param args The command line arguments.
     */
    public final static void main(String[] args) {
        args = args != null ? args : new String[0];
        int startAt = 0;
        String title = null;
        boolean text = true, tree = true;

        while (startAt < args.length && args[startAt].charAt(0) == '-') {
            String arg = args[startAt++].substring(1);
            if ("h".equals(arg)) {
                displayUsageAndExit();
            } else if ("title".equals(arg)) {
                title = args[startAt++];
            } else if ("textonly".equals(arg)) {
                tree = false;
            } else if ("treeonly".equals(arg)) {
                text = false;
            }
        }

        if (!text && !tree)
            displayUsageAndExit();
        if (!isCorrectJavaVersion())
            System.err.println("Warning: Java version 1.4.2 or higher is required");

        Set setRvListenersParam = new HashSet();
        if (args.length > 0) {
            for (int iarg = startAt; args.length > iarg; ++iarg)
                setRvListenersParam.add(RvParameters.parseConfigurationString(args[iarg]));
            System.out.print(RvSnooperGUI.VERSION);
            System.out.print(" on " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version"));
            System.out.print(" " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
            System.out.println(" " + System.getProperty("os.version"));
        }
        RvSnooperGUI gui = new RvSnooperGUI(
                MsgType.getAllDefaultLevels(), setRvListenersParam, title, tree, text);
        gui.show();

    }

}
