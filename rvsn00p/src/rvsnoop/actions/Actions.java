/*
 * Class:     Actions
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.RvSnoopAction;

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

    private static class GlobalAcceleratorListener extends KeyAdapter {
        GlobalAcceleratorListener() {
            super();
        }
        public void keyTyped(KeyEvent e) {
            final Action a = (Action) acceleratorKeyMap.get(KeyStroke.getKeyStrokeForEvent(e));
            if (a == null) return;
            a.actionPerformed(new ActionEvent(e.getSource(), e.getID(),
                    (String) a.getValue(Action.ACTION_COMMAND_KEY)));
            e.consume();
        }
    }

    // KeyStroke -> Action
    private static final Map acceleratorKeyMap = new HashMap();

    private static final KeyListener acceleratorKeyListener = new GlobalAcceleratorListener();

    // String -> Action
    private static final Map actionCommandMap = new HashMap();

    private static final Log log = LogFactory.getLog(Actions.class);

    public static final Action ADD_CONNECTION = add(new AddConnection());

    public static final Action CHANGE_TABLE_FONT = add(new ChangeTableFont());

    public static final Action CHECK_FOR_UPDATES = add(new CheckForUpdates());

    public static final Action COPY = add(new Copy());

    public static final Action CUT = add(new Cut());

    public static final Action DELETE = add(new Delete());

    public static final Action DISPLAY_ABOUT = add(new DisplayAbout());

    public static final Action DISPLAY_HOME_PAGE = add(new DisplayHomePage());

    public static final Action DISPLAY_LICENSE = add(new DisplayLicense());

    public static final Action EDIT_RECORD_TYPES = add(new EditRecordTypes());

    public static final Action EXPORT_TO_HTML = add(new ExportToHtml());

    public static final Action EXPORT_TO_RECORD_BUNDLE = add(new ExportToRecordBundle());

    public static final Action EXPORT_TO_RVSCRIPT = add(new ExportToRvScript());

    public static final Action EXPORT_TO_RVTEST = add(new ExportToRvTest());

    public static final Action FILTER = add(new Filter(Filter.FILTER, "Filter...", "Filter the records visible in the ledger"));

    public static final Action FILTER_BY_SELECTION = add(new Filter(Filter.FILTER_BY_SELECTION, "Filter by Selection", "Filter the records visible in the ledger"));

    public static final Action HELP = add(new Help());

    public static final Action IMPORT_FROM_RECORD_BUNDLE = add(new ImportFromRecordBundle());

    public static final Action OPEN = add(new Open());

    public static final Action PASTE = add(new Paste());

    public static final Action PAUSE_ALL = add(new PauseAllConnections());

    public static final Action PRUNE_EMPTY_SUBJECTS = add(new PruneEmptySubjects());

    public static final Action QUIT = add(new Quit());

    public static final Action REPORT_BUG = add(new ReportBug());

    public static final Action REPUBLISH = add(new Republish());

    public static final Action SAVE = add(new Save());

    public static final Action SAVE_AS = add(new SaveAs());

    public static final Action SEARCH = add(new Search(Search.SEARCH, "Find...", "Search for text in the messages", KeyEvent.VK_F));

    public static final Action SEARCH_AGAIN = add(new Search(Search.SEARCH_AGAIN, "Find Next", "Repeat the last search", KeyEvent.VK_G));

    public static final Action SELECT_ALL_MESSAGES = add(new SelectAllMessages());

    public static final Action SHOW_ALL_COLUMNS = add(new ShowAllColumns());

    public static final Action SUBSCRIBE_TO_UPDATES = add(new SubscribeToUpdates());

    private static Action add(Action action) {
        final String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        actionCommandMap.put(command, action);
        final KeyStroke accelerator = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (accelerator != null) {
            final Object old = acceleratorKeyMap.put(accelerator, action);
            if (old != null && log.isWarnEnabled()) {
                final Object[] fields = new Object[] { accelerator.toString(), ((Action) old).getValue(Action.ACTION_COMMAND_KEY), command };
                if (log.isWarnEnabled()) {
                    log.warn(StringUtils.format("Redefining accelerator key '{0}' from {1} to {2}.", fields));
                }
            }
        }
        return action;
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

    public static KeyListener getAcceleratorKeyListener() {
        return acceleratorKeyListener;
    }

    private final Map acceleratorKeyToActionMap = new HashMap();
    private final Map commandToActionMap = new HashMap();

    public Actions(Application application) {
        addAction(new ClearLedger(application));
    }

    public void addAction(RvSnoopAction action) {
        final String cmd = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        commandToActionMap.put(cmd, action);
        final KeyStroke acc = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (acc == null) { return; }
        Object old = acceleratorKeyToActionMap.put(acc, action);
        if (old != null && log.isWarnEnabled()) {
            if (log.isWarnEnabled()) {
                old = ((Action) old).getValue(Action.ACTION_COMMAND_KEY);
                MessageFormat.format(
                        "Redefining accelerator key '{0}' from {1} to {2}.",
                        new Object[] { acc.toString(), old, cmd });
            }
        }
    }

    public RvSnoopAction getAction(String command) {
        final Object action = commandToActionMap.get(command);
        return action instanceof RvSnoopAction ? (RvSnoopAction) action : null;
    }

}
