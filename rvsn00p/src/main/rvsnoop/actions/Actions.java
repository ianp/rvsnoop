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
import static java.lang.String.format;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.actions.FilterBySelection;
import org.rvsnoop.actions.NewRvConnection;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.Copy;
import org.rvsnoop.actions.Cut;
import org.rvsnoop.actions.Delete;
import org.rvsnoop.actions.EditRecordTypes;
import org.rvsnoop.actions.Filter;
import org.rvsnoop.actions.OpenProject;
import org.rvsnoop.actions.Paste;
import org.rvsnoop.actions.PruneEmptySubjects;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.actions.SaveProjectAs;
import org.rvsnoop.actions.SaveProject;
import org.rvsnoop.actions.Search;
import org.rvsnoop.actions.SearchBySelection;
import org.rvsnoop.actions.SelectAllRecords;
import org.rvsnoop.actions.ShowAllColumns;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.RecordLedgerTable;

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
        @Override
        public void keyTyped(KeyEvent e) {
            final Action a = acceleratorKeyMap.get(KeyStroke.getKeyStrokeForEvent(e));
            if (a == null) return;
            a.actionPerformed(new ActionEvent(e.getSource(), e.getID(),
                    (String) a.getValue(Action.ACTION_COMMAND_KEY)));
            e.consume();
        }
    }

    private static final Map<KeyStroke, Action> acceleratorKeyMap = new HashMap<KeyStroke, Action>();

    private static final KeyListener acceleratorKeyListener = new GlobalAcceleratorListener();

    private static final Map<String, Action> actionCommandMap = new HashMap<String, Action>();

    private static final Log log = LogFactory.getLog(Actions.class);

    public static final Action CHANGE_TABLE_FONT = add(new ChangeTableFont());

    public static final Action DISPLAY_HOME_PAGE = add(new DisplayHomePage());

    public static final Action IMPORT_FROM_RECORD_BUNDLE = add(new ImportFromRecordBundle());

    public static final Action PAUSE_ALL = add(new PauseAllConnections());

    public static final Action REPORT_BUG = add(new ReportBug());

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

    private final Application application;

    private final Map<KeyStroke, Action> acceleratorKeyToActionMap = new HashMap<KeyStroke, Action>();

    private final Map<String, Action> commandToActionMap = new HashMap<String, Action>();

    public Actions(Application application) {
        this.application = application;
        addAction(new ClearLedger(application));
        addAction(new Copy(application));
        addAction(new Cut(application));
        addAction(new Delete(application));
        addAction(new EditRecordTypes(application));
        addAction(new ExportToHtml(application));
        addAction(new ExportToRecordBundle(application));
        addAction(new ExportToRvScript(application));
        addAction(new ExportToRvTest(application));
        addAction(new Filter(application));
        addAction(new FilterBySelection(application));
        addAction(new NewRvConnection(application));
        addAction(new OpenProject(application));
        addAction(new Paste(application));
        addAction(new PruneEmptySubjects(application));
        addAction(new Republish(application));
        addAction(new SaveProject(application));
        addAction(new SaveProjectAs(application));
        addAction(new Search(application));
        addAction(new SearchBySelection(application));
        addAction(new SelectAllRecords(application));
        addAction(new ShowAllColumns(application));
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
                log.warn(format("Redefining accelerator key '%s' from %s to %s.",
                        acc.toString(), old, cmd));
            }
        }
    }

    public RvSnoopAction getAction(String command) {
        return (RvSnoopAction) commandToActionMap.get(command);
    }

    public void configureListeners() {
        final RecordLedgerTable table = application.getLedgerTable();
        final Iterator actions = commandToActionMap.values().iterator();
        while (actions.hasNext()) {
            final RvSnoopAction action = (RvSnoopAction) actions.next();
            if (action instanceof RecordLedgerSelectionListener) {
                table.addRecordLedgerSelectionListener(
                        (RecordLedgerSelectionListener) action);
                action.setEnabled(false);
            }
        }
    }

}
