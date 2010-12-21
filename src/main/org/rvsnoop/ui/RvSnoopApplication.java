// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.EventObject;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
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
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.OSXAdapter;
import org.jdesktop.application.utils.PlatformType;
import org.rvsnoop.Application;
import org.rvsnoop.Connections;
import org.rvsnoop.Logger;
import org.rvsnoop.ProjectFileFilter;
import org.rvsnoop.ProjectService;
import org.rvsnoop.UserPreferences;

import rvsnoop.BrowserLauncher;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;
import rvsnoop.ui.MultiLineToolTipUI;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;

/**
 * The application antry point.
 */
public final class RvSnoopApplication extends SingleFrameApplication {

    static {
        // Set up the log directory so that it may be used in log4j.properties.
        final String home = System.getProperty("user.home");
        final String fileSep = System.getProperty("file.separator");
        if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
            System.setProperty("rvsnoop.logDir",
                    home + "/Application Data/Logs/RvSnoop".replace("/", fileSep));
        } else if (AppHelper.getPlatform() == PlatformType.OS_X) {
            System.setProperty("rvsnoop.logDir",
                    home + "/Library/Logs/RvSnoop".replace("/", fileSep));
        } else {
            System.setProperty("rvsnoop.logDir",
                    home + "/.rvsnoop".replace("/", fileSep));
        }
    }

    private static final Logger logger = Logger.getLogger();

    public static void main(String[] args) throws Exception {
        org.jdesktop.application.Application.launch(RvSnoopApplication.class, args);
    }

    private Injector injector;

    private File initialProjectFile;

    private String getString(String key) {
        return getContext().getResourceMap().getString(key);
    }

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
        JOptionPane.showMessageDialog(getMainFrame(),
                getString("displayAbout.info.message"),
                getString("displayAbout.info.title"),
                JOptionPane.PLAIN_MESSAGE);
    }

    @Action
    public void displayBugsPage() {
        try {
            BrowserLauncher.openURL("https://github.com/ianp/rvsnoop/issues");
        } catch (Exception e) {
            logger.error(e, getString("displayBugsPage.error.browser"));
        }
    }

    private void displayDocPage(String filename) {
        try {
            final File home = new File(System.getProperty("rvsnoop.home"));
            final File docs = new File(home, "doc");
            final File help = new File(docs, filename);
            final String prefix = System.getProperty("os.name").startsWith("Win") ? "" : "file://";
            BrowserLauncher.openURL(prefix + help.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e, getString("displayHelp.error.browser"));
        }
    }

    @Action
    public void displayHelp() {
        displayDocPage("index.html");
    }

    @Action
    public void displayHomePage() {
        try {
            BrowserLauncher.openURL("http://rvsnoop.org");
        } catch (Exception e) {
            logger.error(e, getString("displayHomePage.error.browser"));
        }
    }

    @Action
    public void displayLicense() {
        displayDocPage("license.html");
    }

    private void ensureJavaVersionIsValid() {
        try {
            String[] parts = Pattern.compile("^(\\d+)\\.(\\d+)").split(System.getProperty("java.version"));
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            if (major < 1 || major == 1 && minor < 6) {
                String[] message = { getString("CLI.error.javaVersion[0]"), getString("CLI.error.javaVersion[1]") };
                String title = getString("CLI.error.javaVersion.title");
                JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        } catch (Exception e) {
            // be optimistic
        }
    }

    private void configureLookAndFeel() {
        try {
            if (AppHelper.getPlatform() == PlatformType.OS_X) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
    }

    public void configureMacOsEventHandlers() {
        try {
            OSXAdapter.setAboutHandler(RvSnoopApplication.this, RvSnoopApplication.this.getClass().getMethod("displayAbout"));
            OSXAdapter.setQuitHandler(RvSnoopApplication.this, RvSnoopApplication.this.getClass().getMethod("exit"));
        } catch (NoSuchMethodException e) {
            logger.error(e, "Error installing Mac event handlers.");
            // should never happen.
        }
    }

    @Override
    protected void initialize(String[] args) {
        ensureJavaVersionIsValid();
        configureLookAndFeel();
        logger.info(getString("info.appStarted"));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHookTask(), "shutdownHook"));
        MultiLineToolTipUI.configure();

        Option helpOption = new DefaultOptionBuilder()
                .withShortName("h").withLongName("help")
                .withDescription(getString("CLI.helpDescription")).create();
        Option projectOption = new DefaultOptionBuilder()
                .withShortName("p").withLongName("project")
                .withDescription(getString("CLI.projectDescription")).create();
        CommandLine line = parseCommandLine(args, helpOption, projectOption);

        injector = Guice.createInjector(new GuiModule());
        injector.injectMembers(this);

        initialProjectFile = loadProjectIfValid(projectOption, line);
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

    private File loadProjectIfValid(Option project, CommandLine line) {
        if (!line.hasOption(project)) { return null; }
        String filename = line.getValue(project).toString();
        if (filename == null || filename.length() == 0) {
            logger.warn(getString("CLI.warn.noProject"));
            return null;
        }
        File file = new File(filename);
        if (!file.canRead()) {
            logger.error(getString("CLI.error.unreadableProject"), filename);
            return null;
        }
        return file;
    }

    @Override
    protected void startup() {
        setMainFrame(injector.getInstance(Application.class).getFrame());

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

        if (AppHelper.getPlatform() == PlatformType.OS_X) {
            configureMacOsEventHandlers();
        }

        if (initialProjectFile == null) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new ProjectFileFilter());
            final int option = chooser.showDialog(null, "Open Project");
            initialProjectFile = chooser.getSelectedFile();
            if (JFileChooser.APPROVE_OPTION != option || initialProjectFile == null) { exit(); }

        }
        show(frame);
        injector.getInstance(ProjectService.class).openProject(initialProjectFile);
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
            bind(Preferences.class).toInstance(Preferences.userRoot().node("org").node("rvsnoop"));
            bind(ApplicationContext.class).toInstance(getContext());
            bind(Connections.class).asEagerSingleton();
            bind(RecordTypes.class).asEagerSingleton();
            bind(ProjectService.class).asEagerSingleton();
            bind(Application.class).to(Application.Impl.class).in(Scopes.SINGLETON);
        }

    }

    private class MaybeExit implements ExitListener {
        public boolean canExit(EventObject event) {
            int option = JOptionPane.showConfirmDialog(getMainFrame(),
                    getString("quit.dialog.message"),
                    getString("quit.dialog.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    getContext().getResourceMap().getIcon("banners.exit"));
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
                logger.error(e, getString("error.shutdown"));
                System.exit(1);
            }
        }
    }

    private class ShutdownHookTask implements Runnable {
        ShutdownHookTask() {
            super();
        }
        public void run() {
            logger.info(getString("info.appStopped"));
        }
    }

}
