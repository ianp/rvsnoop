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
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.UserPreferences;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.Copy;
import org.rvsnoop.actions.Cut;
import org.rvsnoop.actions.Delete;
import org.rvsnoop.actions.Filter;
import org.rvsnoop.actions.FilterBySelection;
import org.rvsnoop.actions.NewRvConnection;
import org.rvsnoop.actions.OpenProject;
import org.rvsnoop.actions.Paste;
import org.rvsnoop.actions.PruneEmptySubjects;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.SaveProject;
import org.rvsnoop.actions.SaveProjectAs;
import org.rvsnoop.actions.Search;
import org.rvsnoop.actions.SearchBySelection;
import org.rvsnoop.actions.SelectAllRecords;
import org.rvsnoop.actions.ShowAllColumns;

import rvsnoop.Record;
import rvsnoop.TreeModelAdapter;
import rvsnoop.actions.Actions;
import rvsnoop.actions.ExportToHtml;
import rvsnoop.actions.ExportToRecordBundle;
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
        setName("mainFrame");
        this.application = application;
        final UserPreferences state = UserPreferences.getInstance();
        statusBar = new StatusBar(application);
        subjectExplorer = createSubjectExplorer();
        connectionList = new ConnectionList(application);
        messageLedger = createMessageLedger(application.getFilteredLedger());
        connectionListScroller = createConnectionListScroller(connectionList);
        subjectExplorerScroller = createSubjectExplorerScroller(subjectExplorer);
        messageLedgerScroller = createMessageLedgerScroller(messageLedger);
        messageLedgerSplitter = createMessageLedgerSplitter(messageLedgerScroller, detailsPanel);
        subjectExplorerSplitter = createSubjectExplorerSplitter(subjectExplorerScroller, messageLedgerSplitter);
        connectionListSplitter = createConnectionListSplitter(connectionListScroller, subjectExplorerSplitter);
        final Actions factory = application.getActionFactory();
        setJMenuBar(createMenuBar(messageLedger, factory));
        getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        getContentPane().add(createToolBar(factory), BorderLayout.NORTH);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        pack();

        connectListeners();
        state.listenToChangesFrom(this, messageLedger, new JSplitPane[] {
                connectionListSplitter, messageLedgerSplitter, subjectExplorerSplitter });
    }

    public void clearDetails() {
        detailsPanel.setMessage(null);
    }

    private void connectListeners() {
        final ListSelectionModel messageLedgerSelectionModel = messageLedger.getSelectionModel();
        messageLedgerSelectionModel.addListSelectionListener(new MessageLedgerListener());
        for (Object action : Actions.getActions()) {
            if (action instanceof ListSelectionListener) {
                messageLedgerSelectionModel.addListSelectionListener((ListSelectionListener) action);
            }
        }
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
        final JMenu file = new JMenu("File");
        file.setMnemonic('f');
        file.add(factory.getAction(OpenProject.COMMAND));
        final JMenu fileRecent = new JMenu("Recent Projects");
        fileRecent.setIcon(new ImageIcon("/resources/icons/open.png"));
        fileRecent.addMenuListener(new RecentProjectsMenuManager(application));
        file.add(fileRecent);
        file.addSeparator();
        file.add(factory.getAction(SaveProject.COMMAND));
        file.add(factory.getAction(SaveProjectAs.COMMAND));
        final JMenu fileExport = new JMenu("Export To");
        fileExport.setIcon(new ImageIcon("/resources/icons/exportTo.png"));
        fileExport.add(factory.getAction(ExportToHtml.COMMAND));
        fileExport.add(factory.getAction(ExportToRecordBundle.COMMAND));
        file.add(fileExport);
        final JMenu fileImport = new JMenu("Import From");
        fileExport.setIcon(new ImageIcon("/resources/icons/import.png"));
        fileImport.add(Actions.IMPORT_FROM_RECORD_BUNDLE);
        file.add(fileImport);
        file.addSeparator();
        file.add(factory.getAction(NewRvConnection.COMMAND));
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.addMenuListener(new RecentConnectionsMenuManager(application));
        file.add(connRecent);
//        file.addSeparator();
//        file.add(factory.getAction(Quit.COMMAND));
        return file;
    }

    private JMenuBar createMenuBar(RecordLedgerTable table, Actions factory) {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu(factory));
        bar.add(createEditMenu(factory));
        bar.add(createViewMenu(factory, table));
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
        final JButton colsButton = UIUtils.createSmallButton(
                new ImageIcon("/resources/icons/columns_corner_button.png"), TOOLTIP_VISIBLE_COLUMNS, null);
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
        final JMenu view = new JMenu("View");
        view.setMnemonic('v');
        view.add(factory.getAction(SelectAllRecords.COMMAND));
        view.add(factory.getAction(ShowAllColumns.COMMAND));
        view.addSeparator();
        view.add(factory.getAction(Filter.COMMAND));
        view.add(factory.getAction(FilterBySelection.COMMAND));
        view.addSeparator();
        final JMenu viewColumns = new JMenu("Columns");
        viewColumns.setIcon(new ImageIcon("/resources/icons/filter_columns.png"));
        viewColumns.addMenuListener(new VisibleColumnsMenuManager(table.getTableFormat()));
        view.add(viewColumns);
        final JMenu viewTypes = new JMenu("Types");
        viewTypes.setIcon(new ImageIcon("/resources/icons/editRecordTypes.png"));
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

}
