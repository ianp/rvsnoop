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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;

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
        private final Application application;

        public CreateAndShowTask(Application application) {
            this.application = application;
        }

        public void run() {
            // FIXME this is a hack, there should be no static field.
            UIManager.INSTANCE = application.getFrame();
            // FIXME all of this should go, use event listeners instead.
            PreferencesManager.INSTANCE.setRecordLedgerTable(UIManager.INSTANCE.getMessageLedger());
            PreferencesManager.INSTANCE.load();
            UIManager.INSTANCE.setVisible(true);
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
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error saving session state.", e);
                }
            }
            log.info(Version.getAsStringWithName() + " stopped at " + DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()) + '.');
        }
    }

    private static final Log log = LogFactory.getLog(StartRvSnooper.class);

    private static void handleHelpOption(final Options options) {
        new HelpFormatter().printHelp("rvsnoop", options);
        System.exit(0);
    }

    private static void handleProjectOption(String project, Application application) {
        final File file = new File(project);
        if (file.canRead()) {
            if (log.isInfoEnabled()) {
                log.info("Loading project from " + project);
            }
            try {
                application.setProject(new Project(file));
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Error reading project from " + project, e);
                }
            }
        } else if (log.isErrorEnabled()) {
            if (file.exists()) {
                log.error("The project could not be read from " + project);
            } else {
                log.error("No project could be found at " + project);
            }
        }
    }

    /**
     * The application entry point.
     *
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        MultiLineToolTipUI.configure();
        if (!SystemUtils.isJavaVersionAtLeast(142)) {
            Object message = new String[] {
                "Java 1.4.2 or later is required to run " + Version.getAsStringWithName(),
                "Please rerun using a supported Java version."
            };
            JOptionPane.showMessageDialog(null, message, "Error!",
                    JOptionPane.ERROR_MESSAGE);
        }
        final Application application = new Application();
        final Options options = new Options();
        options.addOption("h", "help", false, "Display a short help message then exit.");
        options.addOption("p", "project", true, "Load a project file on startup.");
        try {
            final CommandLine commands = new PosixParser().parse(options, args);
            if (commands.hasOption("h")) { handleHelpOption(options); }
            if (commands.hasOption("p")) {
                handleProjectOption(commands.getOptionValue("p"), application);
            }
        } catch (ParseException e) {
            new HelpFormatter().printHelp("rvsnoop", options);
            System.exit(-1);
        }
        SwingUtilities.invokeLater(new CreateAndShowTask(application));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookTask(), "shutdownHook"));
    }

    /** Do not instantiate. */
    private StartRvSnooper() {
        throw new UnsupportedOperationException();
    }

}
