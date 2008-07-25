/*
 * Class:     UIManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.UserPreferences;
import org.rvsnoop.actions.CheckForUpdates;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.Copy;
import org.rvsnoop.actions.Cut;
import org.rvsnoop.actions.Delete;
import org.rvsnoop.actions.DisplayAbout;
import org.rvsnoop.actions.EditRecordTypes;
import org.rvsnoop.actions.Filter;
import org.rvsnoop.actions.FilterBySelection;
import org.rvsnoop.actions.NewRvConnection;
import org.rvsnoop.actions.OpenProject;
import org.rvsnoop.actions.Paste;
import org.rvsnoop.actions.PruneEmptySubjects;
import org.rvsnoop.actions.Quit;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.SaveProject;
import org.rvsnoop.actions.SaveProjectAs;
import org.rvsnoop.actions.Search;
import org.rvsnoop.actions.SearchBySelection;
import org.rvsnoop.actions.SelectAllRecords;
import org.rvsnoop.actions.ShowAllColumns;
import org.rvsnoop.ui.ConnectionList;
import org.rvsnoop.ui.ImageFactory;
import org.rvsnoop.ui.RecentConnectionsMenuManager;
import org.rvsnoop.ui.RecentProjectsMenuManager;
import org.rvsnoop.ui.RecordLedgerTable;
import org.rvsnoop.ui.RecordTypesMenuManager;
import org.rvsnoop.ui.StatusBar;
import org.rvsnoop.ui.TrackingAdjustmentListener;
import org.rvsnoop.ui.VisibleColumnsMenuManager;

import rvsnoop.Record;
import rvsnoop.TreeModelAdapter;
import rvsnoop.Version;
import rvsnoop.actions.Actions;
import rvsnoop.actions.ExportToHtml;
import rvsnoop.actions.ExportToRecordBundle;
import rvsnoop.actions.ExportToRvScript;
import rvsnoop.actions.ExportToRvTest;
import rvsnoop.ui.Icons;
import rvsnoop.ui.RvDetailsPanel;
import rvsnoop.ui.SubjectExplorerEditor;
import rvsnoop.ui.SubjectExplorerRenderer;
import rvsnoop.ui.UIUtils;

/**
 * The single user interface class for the application.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
// Class provides a static instance instead of a factory method.
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 1/17/06 7:58 PM
public final class MainFrame extends JFrame {

    private final class MessageLedgerListener implements ListSelectionListener {
        MessageLedgerListener() {
            super();
        }
        public void valueChanged(ListSelectionEvent e) {
            final Record record = getSelectedRecord();
            if (record != null) { detailsPanel.setMessage(record); }
        }
    }

    private final class TreeExpander extends TreeModelAdapter {
        private boolean isExpanded;
        private TreeExpander() {
            super();
        }
        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            if (isExpanded) { return; }
            isExpanded = true;
            subjectExplorer.expandPath(e.getTreePath());
        }
        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            final TreeNode root = (TreeNode) application.getSubjectHierarchy().getRoot();
            if (root.getChildCount() == 0) { isExpanded = false; }
        }
    }

    private final class FontChangeListener implements PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent event) {
            if (UserPreferences.KEY_LEDGER_FONT.equals(event.getKey())) {
                final Font font = UserPreferences.getInstance().getLedgerFont();
                messageLedger.setFont(font);
                // Without this the default rowNumber height is about 1-million-billion pixels...
                final int rowHeight = messageLedger.getFontMetrics(font).getHeight() + 2;
                messageLedger.setRowHeight(rowHeight);
            }
        }

    }

    private final class WindowCloseListener extends WindowAdapter {
        WindowCloseListener() {
            super();
        }
        @Override
        public void windowClosing(WindowEvent e) {
            application.shutdown();
        }
    }

    private static final long serialVersionUID = 4315311340452405694L;

    private static final String TOOLTIP_VISIBLE_COLUMNS = "Show or hide individual table columns";

    private static final Log log = LogFactory.getLog(MainFrame.class);

    // FIXME: Remove this field.
    public static MainFrame INSTANCE;

    private final Application application;

    private final JPopupMenu columnsPopup = new JPopupMenu();

    private final ConnectionList connectionList;
    private final JTree subjectExplorer;
    private final RvDetailsPanel detailsPanel = new RvDetailsPanel();
    private final RecordLedgerTable messageLedger;
    private final JScrollPane connectionListScroller;
    private final JScrollPane subjectExplorerScroller;
    private final JScrollPane messageLedgerScroller;

    private final JSplitPane messageLedgerSplitter;
    private final JSplitPane subjectExplorerSplitter;
    private final JSplitPane connectionListSplitter;

    private final StatusBar statusBar;

    public MainFrame(final Application application) {
        super(Version.getAsStringWithName());
        this.application = application;
        final UserPreferences state = UserPreferences.getInstance();
        statusBar = new StatusBar(application);
        subjectExplorer = createSubjectExplorer();
        connectionList = new ConnectionList(application);
        messageLedger = createMessageLedger(application.getFilteredLedger());
        messageLedger.setFont(state.getLedgerFont());
        connectionListScroller = createConnectionListScroller(connectionList);
        subjectExplorerScroller = createSubjectExplorerScroller(subjectExplorer);
        messageLedgerScroller = createMessageLedgerScroller(messageLedger);
        messageLedgerSplitter = createMessageLedgerSplitter(messageLedgerScroller, detailsPanel);
        subjectExplorerSplitter = createSubjectExplorerSplitter(subjectExplorerScroller, messageLedgerSplitter);
        connectionListSplitter = createConnectionListSplitter(connectionListScroller, subjectExplorerSplitter);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconImage(ImageFactory.getInstance().getIconImage("rvsnoop"));
        final Actions factory = application.getActionFactory();
        setJMenuBar(createMenuBar(messageLedger, factory));
        getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        getContentPane().add(createToolBar(factory), BorderLayout.NORTH);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        pack();
        setBounds(state.getWindowBounds());

        connectListeners();
        configureDividerLocation(connectionListSplitter, "connectionList", state);
        configureDividerLocation(messageLedgerSplitter, "recordLedger", state);
        configureDividerLocation(subjectExplorerSplitter, "subjectExplorer", state);
        state.listenToChangesFrom(this, messageLedger, new JSplitPane[] {
                connectionListSplitter, messageLedgerSplitter, subjectExplorerSplitter });
    }

    public void clearDetails() {
        detailsPanel.setMessage(null);
    }

    private void configureDividerLocation(JSplitPane pane, String name, UserPreferences state) {
        pane.setName(name);
        final int location = state.getDividerLocation(name);
        if (location > 0) { pane.setDividerLocation(location); }
    }

    private void connectListeners() {
        final ListSelectionModel messageLedgerSelectionModel = messageLedger.getSelectionModel();
        messageLedgerSelectionModel.addListSelectionListener(new MessageLedgerListener());
        for (Object action : Actions.getActions()) {
            if (action instanceof ListSelectionListener) {
                messageLedgerSelectionModel.addListSelectionListener((ListSelectionListener) action);
            }
        }
        addWindowListener(new WindowCloseListener());
        UserPreferences.getInstance().addPreferenceChangeListener(new FontChangeListener());
    }

    private JMenu createConfigureMenu() {
        final JMenu configure = new JMenu("Configure");
        configure.setMnemonic('c');
        configure.add(Actions.CHANGE_TABLE_FONT);
        return configure;
    }

    private JScrollPane createConnectionListScroller(JList listenerList) {
        final JScrollPane scrollPane = new JScrollPane(listenerList);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        return scrollPane;
    }

    private JSplitPane createConnectionListSplitter(JScrollPane connectionList, JSplitPane subjectExplorerSplitter) {
        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, connectionList, subjectExplorerSplitter);
        splitter.setOneTouchExpandable(false);
        splitter.setDividerLocation(0.20);
        splitter.setBorder(BorderFactory.createEmptyBorder());
        return splitter;
    }

    private JMenu createEditMenu(final Actions factory) {
        final JMenu edit = new JMenu("Edit");
        edit.setMnemonic('e');
        edit.add(factory.getAction(Cut.COMMAND));
        edit.add(factory.getAction(Copy.COMMAND));
        edit.add(factory.getAction(Paste.COMMAND));
        edit.addSeparator();
        edit.add(factory.getAction(Search.COMMAND));
        edit.add(factory.getAction(SearchBySelection.COMMAND));
        edit.addSeparator();
        edit.add(factory.getAction(Republish.COMMAND));
        edit.addSeparator();
        edit.add(factory.getAction(PruneEmptySubjects.COMMAND));
        edit.addSeparator();
        edit.add(factory.getAction(Delete.COMMAND));
        return edit;
    }

    private JMenu createFileMenu(final Actions factory) {
        final ImageFactory images = ImageFactory.getInstance();
        final JMenu file = new JMenu("File");
        file.setMnemonic('f');
        file.add(factory.getAction(OpenProject.COMMAND));
        final JMenu fileRecent = new JMenu("Recent Projects");
        fileRecent.setIcon(new ImageIcon(images.getIconImage(OpenProject.COMMAND)));
        fileRecent.addMenuListener(new RecentProjectsMenuManager(application));
        file.add(fileRecent);
        file.addSeparator();
        file.add(factory.getAction(SaveProject.COMMAND));
        file.add(factory.getAction(SaveProjectAs.COMMAND));
        final JMenu fileExport = new JMenu("Export To");
        fileExport.setIcon(Icons.EXPORT);
        fileExport.add(factory.getAction(ExportToHtml.COMMAND));
        fileExport.add(factory.getAction(ExportToRecordBundle.COMMAND));
        fileExport.add(factory.getAction(ExportToRvScript.COMMAND));
        fileExport.add(factory.getAction(ExportToRvTest.COMMAND));
        file.add(fileExport);
        final JMenu fileImport = new JMenu("Import From");
        fileExport.setIcon(Icons.IMPORT);
        fileImport.add(Actions.IMPORT_FROM_RECORD_BUNDLE);
        file.add(fileImport);
        file.addSeparator();
        file.add(factory.getAction(NewRvConnection.COMMAND));
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.setIcon(Icons.ADD_CONNECTION);
        connRecent.addMenuListener(new RecentConnectionsMenuManager(application));
        file.add(connRecent);
        file.addSeparator();
        file.add(factory.getAction(Quit.COMMAND));
        return file;
    }

    private JMenu createHelpMenu(final Actions factory) {
        final JMenu help = new JMenu("Help");
        help.setMnemonic('h');
        help.add(Actions.HELP);
        help.add(Actions.DISPLAY_HOME_PAGE);
        help.add(Actions.REPORT_BUG);
        help.add(factory.getAction(CheckForUpdates.COMMAND));
        help.add(Actions.SUBSCRIBE_TO_UPDATES);
        help.addSeparator();
        help.add(Actions.DISPLAY_LICENSE);
        help.add(factory.getAction(DisplayAbout.COMMAND));
        return help;
    }

    private JMenuBar createMenuBar(RecordLedgerTable table, Actions factory) {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu(factory));
        bar.add(createEditMenu(factory));
        bar.add(createViewMenu(factory, table));
        bar.add(createConfigureMenu());
        bar.add(createHelpMenu(factory));
        // Mac OS X screen menu bars do not normally show icons.
        if (SystemUtils.IS_OS_MAC_OSX)
            removeIconsFromMenuElements(bar);
        return bar;
    }

    private RecordLedgerTable createMessageLedger(RecordLedger ledger) {
        final RecordLedgerTable table = new RecordLedgerTable(ledger, application.getConnections());
        table.getTableFormat().setColumns(UserPreferences.getInstance().getLedgerColumns());
        columnsPopup.addPopupMenuListener(new VisibleColumnsMenuManager(table.getTableFormat()));
        return table;
    }

    private JScrollPane createMessageLedgerScroller(JTable ledger) {
        final JScrollPane scrollPane = new JScrollPane(ledger);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.GRAY));
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new TrackingAdjustmentListener());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        final JButton colsButton = UIUtils.createSmallButton(Icons.COLUMNS_CORNER, TOOLTIP_VISIBLE_COLUMNS, null);
        colsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                columnsPopup.show(colsButton, e.getX(), e.getY());
            }
        });
        colsButton.setBorderPainted(false);
        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, colsButton);
        return scrollPane;
    }

    private JSplitPane createMessageLedgerSplitter(JScrollPane ledger, RvDetailsPanel details) {
        final JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ledger, details);
        splitter.setOneTouchExpandable(true);
        splitter.setDividerLocation(0.5);
        splitter.setBorder(BorderFactory.createEmptyBorder());
        return splitter;
    }

    private JTree createSubjectExplorer() {
        final JTree tree = new JTree(application.getSubjectHierarchy());
        tree.setBorder(BorderFactory.createEmptyBorder());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setCellRenderer(new SubjectExplorerRenderer());
        tree.setCellEditor(new SubjectExplorerEditor(application, tree));
        application.getSubjectHierarchy().addTreeModelListener(new TreeExpander());
        // This line allows the cell renderer to provide a custom tooltip for each node.
        ToolTipManager.sharedInstance().registerComponent(tree);
        return tree;
    }

    private JScrollPane createSubjectExplorerScroller(JTree subjectExplorer) {
        final JScrollPane scroller = new JScrollPane(subjectExplorer);
        scroller.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.GRAY));
        return scroller;
    }

    private JSplitPane createSubjectExplorerSplitter(JScrollPane subjectExplorer, JSplitPane messageLedger) {
        final JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, subjectExplorer, messageLedger);
        splitter.setOneTouchExpandable(false);
        splitter.setDividerLocation(0.25);
        splitter.setBorder(BorderFactory.createEmptyBorder());
        return splitter;
    }

    private JToolBar createToolBar(final Actions factory) {
        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        toolbar.setRollover(true);
        toolbar.add(factory.getAction(OpenProject.COMMAND));
        toolbar.add(factory.getAction(SaveProject.COMMAND));
        toolbar.addSeparator();
        toolbar.add(factory.getAction(NewRvConnection.COMMAND));
        toolbar.addSeparator();
        toolbar.add(factory.getAction(ClearLedger.COMMAND));
        toolbar.addSeparator();
        toolbar.add(Actions.PAUSE_ALL);
        final Component[] components = toolbar.getComponents();
        for (int i = 0, imax = components.length; i < imax; ++i) {
            Component component = components[i];
            if (component instanceof AbstractButton) {
                ((AbstractButton) component).setBorderPainted(false);
                ((AbstractButton) component).setOpaque(false);
            }
        }
        return toolbar;
    }

    private JMenu createViewMenu(final Actions factory, final RecordLedgerTable table) {
        final ImageFactory images = ImageFactory.getInstance();
        final JMenu view = new JMenu("View");
        view.setMnemonic('v');
        view.add(factory.getAction(SelectAllRecords.COMMAND));
        view.add(factory.getAction(ShowAllColumns.COMMAND));
        view.addSeparator();
        view.add(factory.getAction(Filter.COMMAND));
        view.add(factory.getAction(FilterBySelection.COMMAND));
        view.addSeparator();
        final JMenu viewColumns = new JMenu("Columns");
        viewColumns.setIcon(Icons.FILTER_COLUMNS);
        viewColumns.addMenuListener(new VisibleColumnsMenuManager(table.getTableFormat()));
        view.add(viewColumns);
        final JMenu viewTypes = new JMenu("Types");
        viewTypes.setIcon(new ImageIcon(images.getIconImage(EditRecordTypes.COMMAND)));
        viewTypes.addMenuListener(new RecordTypesMenuManager(application));
        view.add(viewTypes);
        return view;
    }

    public RecordLedgerTable getRecordLedger() {
        return messageLedger;
    }

    public Font getMessageLedgerFont() {
        return messageLedger.getFont();
    }

    public Record getSelectedRecord() {
        try {
            final int index = messageLedger.getSelectedRow();
            return index >= 0 ? application.getFilteredLedger().get(index) : null;
        } catch (IndexOutOfBoundsException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get selected record from ledger.", e);
            }
            return null;
        }
    }

    public JTree getSubjectExplorer() {
        return subjectExplorer;
    }

    private void removeIconsFromMenuElements(MenuElement elt) {
        if (elt instanceof AbstractButton)
            ((AbstractButton) elt).setIcon(null);
        final MenuElement[] elts = elt.getSubElements();
        for (int i = 0, imax = elts.length; i < imax; ++i) {
            removeIconsFromMenuElements(elts[i]);
        }
    }

}
