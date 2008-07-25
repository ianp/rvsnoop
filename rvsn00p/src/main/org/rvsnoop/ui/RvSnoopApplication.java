/*
 * Class:     RvSnoopApplication
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2008 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */

package org.rvsnoop.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.EventObject;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task.BlockingScope;
import org.rvsnoop.Application;

import rvsnoop.BrowserLauncher;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The main UI class for RvSnoop.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnoopApplication extends SingleFrameApplication {
    
    private static final Log logger = LogFactory.getLog(RvSnoopApplication.class);

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
            if (logger.isErrorEnabled()) {
                logger.error(getContext().getResourceMap().getString("displayBugsPage.error.browser"), e);
            }
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
            if (logger.isErrorEnabled()) {
                logger.error(getContext().getResourceMap().getString("displayHelp.error.browser"), e);
            }
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
            if (logger.isErrorEnabled()) {
                logger.error(getContext().getResourceMap().getString("displayHomePage.error.browser"), e);
            }
        }
    }

    @Action
    public void displayLicense() {
        displayDocPage("license.html");
    }

    @Override
    protected void initialize(String[] args) {
        injector = Guice.createInjector(new GuiModule());
        injector.injectMembers(this);
    }

    /* (non-Javadoc)
     * @see org.jdesktop.application.Application#startup()
     */
    @Override
    protected void startup() {
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
            bind(Application.class).toInstance(new Application());
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
            return option == JOptionPane.YES_OPTION;
        }

        public void willExit(EventObject event) {
            injector.getInstance(Application.class).shutdown();
        }
    }

}
