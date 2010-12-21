// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.actions.FilterBySelection;
import org.rvsnoop.actions.NewRvConnection;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.Copy;
import org.rvsnoop.actions.Cut;
import org.rvsnoop.actions.Delete;
import org.rvsnoop.actions.EditRecordTypes;
import org.rvsnoop.actions.Filter;
import org.rvsnoop.actions.Paste;
import org.rvsnoop.actions.PruneEmptySubjects;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.RvSnoopAction;
import org.rvsnoop.actions.Search;
import org.rvsnoop.actions.SearchBySelection;
import org.rvsnoop.actions.SelectAllRecords;
import org.rvsnoop.actions.ShowAllColumns;
import org.rvsnoop.event.RecordLedgerSelectionListener;
import org.rvsnoop.ui.RecordLedgerTable;

/**
 * Singleton action instances.
 * <p>
 * All of the actions are package private and should be accessed via this class.
 * This is done to allow the use of actions to enforce modularity in the code
 * whilst preventing application state from being scattered throughout too many
 * classes.
 */
public final class Actions {

    private static final Map<KeyStroke, Action> acceleratorKeyMap = new HashMap<KeyStroke, Action>();

    private static final Map<String, Action> actionCommandMap = new HashMap<String, Action>();

    private static final Logger logger = Logger.getLogger();

    private static Action add(Action action) {
        String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        actionCommandMap.put(command, action);
        KeyStroke accelerator = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (accelerator != null) {
            final Action old = acceleratorKeyMap.put(accelerator, action);
            if (old != null) {
                logger.warn("Redefining accelerator key '%s' from %s to %s.", accelerator, old.getValue(Action.ACTION_COMMAND_KEY), command);
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
        addAction(new Filter(application));
        addAction(new FilterBySelection(application));
        addAction(new ImportFromRecordBundle(application));
        addAction(new NewRvConnection(application));
        addAction(new Paste(application));
        add(new PauseAllConnections());
        addAction(new PruneEmptySubjects(application));
        add(new ReportBug());
        addAction(new Republish(application));
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
        if (old != null) {
            old = ((Action) old).getValue(Action.ACTION_COMMAND_KEY);
            logger.warn("Redefining accelerator key '%s' from %s to %s.", acc, old, cmd);
        }
    }

    public RvSnoopAction getAction(String command) {
        return (RvSnoopAction) commandToActionMap.get(command);
    }

    public void configureListeners() {
        final RecordLedgerTable table = application.getLedgerTable();
        for (Action action : commandToActionMap.values()) {
            if (action instanceof RecordLedgerSelectionListener) {
                table.addRecordLedgerSelectionListener((RecordLedgerSelectionListener) action);
                action.setEnabled(false);
            }
        }
    }

}
