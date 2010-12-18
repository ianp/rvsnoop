/*
 * Class:     RvSnoopApplication
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2008 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */

package org.rvsnoop.ui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task.BlockingScope;
import org.rvsnoop.Application;
import org.rvsnoop.Connections;
import org.rvsnoop.Logger;
import org.rvsnoop.Project;
import org.rvsnoop.SystemUtils;
import org.rvsnoop.UserPreferences;

import rvsnoop.BrowserLauncher;
import rvsnoop.RvConnection;
import rvsnoop.ui.MultiLineToolTipUI;

import com.apple.eawt.ApplicationEvent;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * The main UI class for RvSnoop.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RvSnoopApplication extends SingleFrameApplication {

    static {
        // Set up the log directory so that it may be used in log4j.properties.
        final String home = System.getProperty("user.home");
        final String fileSep = System.getProperty("file.separator");
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty("rvsnoop.logDir",
                    home + "/Application Data/Logs/RvSnoop".replace("/", fileSep));
        } else if (SystemUtils.IS_OS_MAC) {
            System.setProperty("rvsnoop.logDir",
                    home + "/Library/Logs/RvSnoop".replace("/", fileSep));
        } else {
            System.setProperty("rvsnoop.logDir",
                    home + "/.rvsnoop".replace("/", fileSep));
        }
    }

    private static final Logger logger = new Logger();

    /**
     * The application entry point.
     *
     * @param args The command line arguments.
     */
    public static void main(final String[] args) throws Exception {
        org.jdesktop.application.Application.launch(RvSnoopApplication.class, args);
    }

    private Injector injector;

    @Action(block=BlockingScope.ACTION)
    public CheckForUpdatesTask checkForUpdates() {
        return new CheckForUpdatesTask(this);
    }

    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu();
        helpMenu.setName("helpMenu");
        getContext().getResourceMap().injectComponent(helpMenu);
        ApplicationActionMap actionMap = getContext().getActionMap();
        helpMenu.add(actionMap.get("displayHelp"));
        helpMenu.add(actionMap.get("displayHomePage"));
        helpMenu.add(actionMap.get("displayBugsPage"));
        helpMenu.addSeparator();
        helpMenu.add(actionMap.get("checkForUpdates"));
        helpMenu.addSeparator();
        helpMenu.add(actionMap.get("displayLicense"));
        helpMenu.add(actionMap.get("displayAbout"));
        return helpMenu;
    }

    @Action
    public void displayAbout() {
        ResourceMap resourceMap = getContext().getResourceMap();
        JOptionPane.showMessageDialog(
                getMainFrame(),
                resourceMap.getString("displayAbout.info.message"),
                resourceMap.getString("displayAbout.info.title"),
                JOptionPane.PLAIN_MESSAGE);
    }

    @Action
    public void displayBugsPage() {
        try {
            BrowserLauncher.openURL("http://sourceforge.net/tracker/?group_id=63447");
        } catch (Exception e) {
            logger.error(getContext().getResourceMap(), "displayBugsPage.error.browser", e);
        }
    }

    private void displayDocPage(String filename) {
        try {
            final File home = new File(System.getProperty("rvsnoop.home"));
            final File docs = new File(home, "doc");
            final File help = new File(docs, filename);
            final String prefix = System.getProperty("os.name").startsWith("Win")
                ? "" : "file://";
            BrowserLauncher.openURL(prefix + help.getAbsolutePath());
        } catch (Exception e) {
            logger.error(getContext().getResourceMap(), "displayHelp.error.browser", e);
        }
    }

    @Action
    public void displayHelp() {
        displayDocPage("index.html");
    }

    @Action
    public void displayHomePage() {
        try {
            BrowserLauncher.openURL("http://rvsn00p.sourceforge.net");
        } catch (Exception e) {
            logger.error(getContext().getResourceMap(), "displayHomePage.error.browser", e);
        }
    }

    @Action
    public void displayLicense() {
        displayDocPage("license.html");
    }

    private void ensureJavaVersionIsValid() {
        if (!SystemUtils.isJavaVersionAtLeast(1,5)) {
            ResourceMap resourceMap = getContext().getResourceMap();
            Object message = new String[] {
                    resourceMap.getString("CLI.error.javaVersion[0]"),
                    resourceMap.getString("CLI.error.javaVersion[1]")
            };
            String title = resourceMap.getString("CLI.error.javaVersion.title");
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    @Override
    protected void initialize(String[] args) {
        ensureJavaVersionIsValid();
        ResourceMap resourceMap = getContext().getResourceMap();
        logger.info(resourceMap, "info.appStarted");
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookTask(), "shutdownHook"));
        MultiLineToolTipUI.configure();

        Option helpOption = new DefaultOptionBuilder()
                .withShortName("h").withLongName("help")
                .withDescription(resourceMap.getString("CLI.helpDescription")).create();
        Option projectOption = new DefaultOptionBuilder()
                .withShortName("p").withLongName("project")
                .withDescription(resourceMap.getString("CLI.projectDescription")).create();
        CommandLine line = parseCommandLine(args, helpOption, projectOption);

        injector = Guice.createInjector(new GuiModule());
        injector.injectMembers(this);

        Project project = loadProjectIfValid(projectOption, line);
        if (project != null) {
            try {
                injector.getInstance(Application.class).setProject(project);
            } catch (IOException e) {
                logger.error(getContext().getResourceMap(), "CLI.error.readingProject");
            }
        }
    }

    private CommandLine parseCommandLine(String[] args, Option helpOption, Option projectOption) {
        Group group = new GroupBuilder().withOption(helpOption).withOption(projectOption).create();
        Parser parser = new Parser();
        parser.setGroup(group);
        parser.setHelpOption(helpOption);
        parser.setHelpFormatter(new HelpFormatter());
        CommandLine line = parser.parseAndHelp(args);
        if (line.hasOption(helpOption)) { System.exit(0); }
        return line;
    }

    private Project loadProjectIfValid(Option project, CommandLine line) {
        if (!line.hasOption(project)) { return null; }
        String filename = line.getValue(project).toString();
        if (filename == null || filename.length() == 0) {
            logger.warn(getContext().getResourceMap(), "CLI.warn.noProject");
            return null;
        }
        File file = new File(filename);
        if (!file.canRead()) {
            logger.error(getContext().getResourceMap(), "CLI.error.unreadableProject", filename);
            return null;
        }
        return new Project(file);
    }

    /* (non-Javadoc)
     * @see org.jdesktop.application.Application#startup()
     */
    @Override
    protected void startup() {
        MainFrame.INSTANCE = injector.getInstance(Application.class).getFrame();
        setMainFrame(MainFrame.INSTANCE);

        final JFrame frame = getMainFrame();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new FrameClosingListener());
        addExitListener(new MaybeExit());

        ApplicationActionMap actionMap = getContext().getActionMap();
        JMenu fileMenu = frame.getJMenuBar().getMenu(0);
        fileMenu.addSeparator();
        fileMenu.add(actionMap.get("quit"));

        frame.getJMenuBar().add(createHelpMenu());
        getContext().getResourceMap().injectComponents(frame);

        if (SystemUtils.IS_OS_MAC) {
            new MacHandlersInstaller().installHandlers();
        }

        show(frame);
    }

    private class FrameClosingListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent event) {
            exit(event);
        }
    }

    private class GuiModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Connections.class).toInstance(new Connections());
            bind(Application.class).to(Application.Impl.class).in(Scopes.SINGLETON);
            bind(ApplicationContext.class).toInstance(getContext());
        }

    }

    private class MaybeExit implements ExitListener {
        public boolean canExit(EventObject event) {
            ResourceMap resourceMap = getContext().getResourceMap();
            int option = JOptionPane.showConfirmDialog(getMainFrame(),
                    resourceMap.getString("quit.dialog.message"),
                    resourceMap.getString("quit.dialog.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    resourceMap.getIcon("banners.exit"));
            if (option == JOptionPane.YES_OPTION) {
                // FIXME: this shouldn't be necessary:
                // change this class to ExitHandler and make sure all reosurces are
                // saved as necessary. Inject this class.
                UserPreferences.getInstance().store();
                return true;
            } else {
                return false;
            }
        }

        public void willExit(EventObject event) {
            try {
                final RvConnection[] conns = injector.getInstance(Connections.class).toArray();
                for (int i = 0, imax = conns.length; i < imax; ++i) {
                    conns[i].stop();
                }
                RvConnection.shutdown();
                System.exit(0);
            } catch (Exception e) {
                logger.error(getContext().getResourceMap(), "error.shutdown", e);
                System.exit(1);
            }
        }
    }

    private final class MacHandlersInstaller {
        public void installHandlers() {
            com.apple.eawt.Application.getApplication().addApplicationListener(new com.apple.eawt.ApplicationAdapter() {

                @Override
                public void handleAbout(ApplicationEvent event) {
                    getContext().getActionMap().get("displayAbout").actionPerformed(
                            new ActionEvent(event.getSource(), ActionEvent.ACTION_PERFORMED, "displayAbout"));
                }

                @Override
                public void handlePreferences(ApplicationEvent event) {
                    // TODO display the preferences dialog once we have one.
                }

                @Override
                public void handleQuit(ApplicationEvent event) {
                    event.setHandled(false);
                    exit();
                }
            });
        }
    }

    private class ShutdownHookTask implements Runnable {
        ShutdownHookTask() {
            super();
        }
        public void run() {
            logger.info(getContext().getResourceMap(), "info.appStopped");
        }
    }

}
