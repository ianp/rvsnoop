/*
 * Class:     StartRvSnooper
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rvsnoop.ui.MultiLineToolTipUI;
import rvsnoop.ui.UIManager;

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

    private static class CreateAndShowTask implements Runnable {
        private final String filename;
        private final File file;

        public CreateAndShowTask(String filename, File file) {
            this.filename = filename;
            this.file = file;
        }

        public void run() {
            PreferencesManager.INSTANCE.setRecordLedgerTable(UIManager.INSTANCE.getMessageLedger());
            PreferencesManager.INSTANCE.load();
            UIManager.INSTANCE.setVisible(true);
            if (file != null && file.exists() && file.canRead()) {
                try {
                    final Project project = new Project(file.getCanonicalFile());
                    project.load();
                    RecentProjects.INSTANCE.add(project);
                } catch(IOException e) {
                    log.error("Could not load project from " + filename, e);
                }
            }
        }
    }

    private static class ShutdownHookTask implements Runnable {
        ShutdownHookTask() {
            super();
        }
        public void run() {
            try {
                PreferencesManager.INSTANCE.store();
                RecentConnections.getInstance().store();
                RecentProjects.INSTANCE.store();
                log.info(Version.getAsStringWithName() + " stopped at " + StringUtils.format(new Date()) + '.');
            } catch (IOException ignored) {
                // Oh well, we may lose the preferences then.
            }
        }
    }

    private static final Log log = LogFactory.getLog(StartRvSnooper.class);

    /**
     * The application entry point.
     *
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        MultiLineToolTipUI.configure();
        if (!SystemUtils.isJavaVersionAtLeast(142) && log.isWarnEnabled())
            log.warn("Java version 1.4.2 or higher is required, RvSnoop may fail unexpectedly with earlier versions.");
        final ArgParser parser = new ArgParser(Version.getAsStringWithName());
        parser.addArgument('h', "help", true, "Display a short help message and exit.");
        parser.addArgument('p', "project", false, "Load a project file on startup.");
        parser.addArgument('i', "interface", false, "Use the specified look and feel (class name).");
        parser.parseArgs(args);
        if (parser.getBooleanArg("help")) {
            parser.printUsage(System.out);
            System.exit(0);
        }
        setLookAndFeel(parser.getStringArg("interface"));
        final String filename = parser.getStringArg("project");
        if (filename != null && log.isDebugEnabled()) log.debug("Loading project file: " + filename);
        final File file = filename == null ? null : new File(filename);
        SwingUtilities.invokeLater(new CreateAndShowTask(filename, file));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookTask(), "shutdownHook"));
    }

    private static void setLookAndFeel(String className) {
        if (className == null) {
            className = SystemUtils.IS_OS_WINDOWS
                      ? "com.jgoodies.looks.windows.WindowsLookAndFeel"
                      : "com.jgoodies.looks.plastic.PlasticXPLookAndFeel";
        }
        try {
            javax.swing.UIManager.setLookAndFeel(className);
        } catch(Exception e) {
            if (log.isWarnEnabled())
                log.warn("Could not set look and feel to " + className);
        }
    }

    /**
     * Do not instantiate.
     */
    private StartRvSnooper() {
        throw new UnsupportedOperationException();
    }

}
