/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p;

import rvsn00p.util.rv.RvParameters;
import rvsn00p.viewer.RvSnooperGUI;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Starts an instance of the RvSn00p console for off-line viewing.
 *
 * @author Örjan Lundberg
 * Based on work done on Logfactor5 Contributed by ThoughtWorks Inc. by
 * @author Brad Marlborough
 * @author Richard Hurst
 */


public class StartRvSnooper {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    /**
     * Main - starts a an instance of the RvSnoop console and configures
     * the console settings.
     */
    public final static void main(String[] args) {
        int startAt = 0;
        String title;
        title = null;

        if (args.length != 0){
            if (args[0].compareToIgnoreCase("-h") == 0) {
                System.err.println(RvSnooperGUI.VERSION);
                System.err.println("Usage: rvsnoop.StartRvSnooper [-title t] [Daemon|Service|Network|Subject] ...  ");
                System.err.println("Example: rvsnoop.StartRvSnooper \"tcp:7500|7500||a.>,c.x\" \"tcp:7500|7501||b.>,q.b\"  ");
                System.exit(-1);
            }  else if( args[0].compareToIgnoreCase("-title") == 0) {
                title = args[1];
                startAt = 2;
            }
        }



        Set setRvListenersParam = new HashSet();
        if (args.length > 0) {
            for ( int iarg = startAt; args.length > iarg;++iarg) {
                RvParameters p = new RvParameters();
                p.configureByLineString(args[iarg]);
                setRvListenersParam.add(p);
            }

            System.out.println(RvSnooperGUI.VERSION);
        }

        RvSnooperGUI gui = new RvSnooperGUI(
                MsgType.getAllDefaultLevels(), setRvListenersParam, title);

        gui.setFrameSize(getDefaultMonitorWidth(),
                             getDefaultMonitorHeight());

        gui.show();

    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

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
        }
        catch (Throwable t) {
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
        }
        catch (Throwable t) {
            return 600;
        }
    }

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces
    //--------------------------------------------------------------------------

}


