//:File:    StartRvSnooper.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import rvsn00p.util.rv.RvParameters;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Starts an instance of the RvSn00p console for off-line viewing.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class StartRvSnooper {

    /**
     * Main - starts a an instance of the RvSnoop console and configures
     * the console settings.
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
        if (checkJavaVersion() == false)
            System.err.println("Warning: Java JRE Version 1.4.1 or higher is required");

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

    protected static int getDefaultMonitorWidth() {
        return (3 * getScreenWidth()) / 4;
    }

    protected static int getDefaultMonitorHeight() {
        return (3 * getScreenHeight()) / 4;
    }

    /**
     * @return the screen width from Toolkit.getScreenSize()
     * if possible, otherwise returns 800
     * @see java.awt.Toolkit
     */
    protected static int getScreenWidth() {
        try {
            return Toolkit.getDefaultToolkit().getScreenSize().width;
        } catch (Throwable t) {
            return 800;
        }
    }

    /**
     * @return the screen height from Toolkit.getScreenSize()
     * if possible, otherwise returns 600
     * @see java.awt.Toolkit
     */
    protected static int getScreenHeight() {
        try {
            return Toolkit.getDefaultToolkit().getScreenSize().height;
        } catch (Throwable t) {
            return 600;
        }
    }

    /**
     * @return boolean is java version ok
     */
    protected static boolean checkJavaVersion() {

        boolean retVal;
        String ver;
        ver = System.getProperty("java.version");
        retVal = false;
        try {
            StringTokenizer st = new StringTokenizer(ver, "._-");
            int a,b;
            a = b = -1;

            a = Integer.parseInt(st.nextToken());
            b = Integer.parseInt(st.nextToken());

            retVal = (a >= 1 && b >= 4);
            if (a == 1 && b == 4) {
                if (st.hasMoreTokens() == true) {
                    Integer.parseInt(st.nextToken());
                    if (Integer.parseInt(st.nextToken()) >= 1) {
                        retVal = true;
                    } else {
                        retVal = false;
                    }
                } else {
                    retVal = false;
                }

            }
        }catch(Exception ex){
            retVal = true;
        }

        return retVal;

    }

}
