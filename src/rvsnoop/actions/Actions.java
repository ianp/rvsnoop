//:File:    Actions.java
//:Created: Sep 14, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.KeyStroke;

import rvsnoop.Logger;
import rvsnoop.StringUtils;

/**
 * Singleton action instances.
 * <p>
 * All of the actions are package private and should be accessed via this class.
 * This is done to allow the use of actions to enforce modularity in the code
 * whilst preventing application state from being scattered throughout too many
 * classes.
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Actions {

    // KeyStroke -> Action
    static final Map acceleratorKeyMap = new HashMap();
    
    private static final KeyListener acceleratorKeyListener = new KeyAdapter() {
        public void keyTyped(KeyEvent e) {
            final Action a = (Action) acceleratorKeyMap.get(KeyStroke.getKeyStrokeForEvent(e));
            if (a == null) return;
            a.actionPerformed(new ActionEvent(e.getSource(), e.getID(),
                (String) a.getValue(Action.ACTION_COMMAND_KEY)));
            e.consume();
        }
    };
    
    // String -> Action
    private static final Map actionCommandMap = new HashMap();

    private static final Logger logger = Logger.getLogger(Actions.class);
    
    public static final Action ADD_CONNECTION = add(new AddConnection());

    public static final Action CHANGE_TABLE_FONT = add(new ChangeTableFont());

    public static final Action CHECK_FOR_UPDATES = add(new CheckForUpdates());

    public static final Action CLEAR_LEDGER = add(new ClearLedger());

    public static final Action COPY = add(new Copy());

    public static final Action CUT = add(new Cut());

    public static final Action DELETE = add(new Delete());
    
    public static final Action DISPLAY_ABOUT = add(new DisplayAbout());
    
    public static final Action DISPLAY_HOME_PAGE = add(new DisplayHomePage());
    
    public static final Action DISPLAY_LICENSE = add(new DisplayLicense());

    public static final Action DISPLAY_WL_IAN = add(new DisplayWishList("Ian’s Wish List", "http://www.amazon.co.uk/gp/registry/registry.html/202-6461367-3647865?id=32RLYO966NV3B"));

    public static final Action DISPLAY_WL_ORJAN = add(new DisplayWishList("Örjan’s Wish List", "http://www.amazon.co.uk/gp/registry/registry.html/203-5255811-7236765?id=14PROST9BIEH3"));

    public static final Action EXPORT_TO_HTML = add(new ExportToHtml());

    public static final Action EXPORT_TO_RVSCRIPT = add(new ExportToRvScript());

    public static final Action EXPORT_TO_RVTEST = add(new ExportToRvTest());

    public static final Action HELP = add(new Help());

    public static final Action OPEN = add(new Open());

    public static final Action PASTE = add(new Paste());

    public static final Action PAUSE_ALL = add(new PauseAllConnections());

    public static final Action PRUNE_EMPTY_SUBJECTS = add(new PruneEmptySubjects());
    
    public static final Action QUIT = add(new Quit());
    
    public static final Action REPORT_BUG = add(new ReportBug());

    public static final Action SAVE = add(new Save());

    public static final Action SAVE_AS = add(new SaveAs());

    public static final Action SEARCH = add(new Search(Search.SEARCH, "Find...", "Search for text in the messages", KeyEvent.VK_F));

    public static final Action SEARCH_AGAIN = add(new Search(Search.SEARCH_AGAIN, "Find Again", "Repeat the last search", KeyEvent.VK_G));

    public static final Action SUBSCRIBE_TO_UPDATES = add(new SubscribeToUpdates());
    
    static String WARN_ACCEL_REDEFINITION = "Redefining accelerator key '{0}' from {1} to {2}.";
    
    private static Action add(Action action) {
        final String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        actionCommandMap.put(command, action);
        final KeyStroke accelerator = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (accelerator != null) {
            final Object old = acceleratorKeyMap.put(accelerator, action);
            if (old != null && logger.isWarnEnabled()) {
                final Object[] fields = new Object[] { accelerator.toString(), ((Action) old).getValue(Action.ACTION_COMMAND_KEY), command };
                logger.warn(StringUtils.format(WARN_ACCEL_REDEFINITION, fields));
            }
        }
        return action;
    }
    
    /**
     * Perform an action.
     * 
     * @param actionCommand
     * @throws NullPointerException if the action does not exist.
     */
    public static void execute(String actionCommand) {
        ((Action) actionCommandMap.get(actionCommand)).actionPerformed(null);
    }

    /**
     * Returns a read-only collection containing all of the actions known to the
     * application.
     * 
     * @return All known actions.
     */
    public static Collection getActions() {
        return Collections.unmodifiableCollection(actionCommandMap.values());
    }

    /**
     * Returns a read-only set containing all of the action commands known
     * to the application.
     *
     * @return All known action commands.
     */
    public static Set getActionCommands() {
        return Collections.unmodifiableSet(actionCommandMap.keySet());
    }
    
    public static KeyListener getAcceleratorKeyListener() {
        return acceleratorKeyListener;
    }

    private Actions() {
        super();
    }

}
