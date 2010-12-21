// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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

import com.tibco.tibrv.TibrvException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.ProjectFileFilter;
import org.rvsnoop.ProjectService;
import org.rvsnoop.RecordLedger;
import org.rvsnoop.actions.ClearLedger;
import org.rvsnoop.actions.Copy;
import org.rvsnoop.actions.Cut;
import org.rvsnoop.actions.Delete;
import org.rvsnoop.actions.Filter;
import org.rvsnoop.actions.FilterBySelection;
import org.rvsnoop.actions.NewRvConnection;
import org.rvsnoop.actions.Paste;
import org.rvsnoop.actions.PruneEmptySubjects;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.Search;
import org.rvsnoop.actions.SearchBySelection;
import org.rvsnoop.actions.SelectAllRecords;
import org.rvsnoop.actions.ShowAllColumns;

import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;
import rvsnoop.TreeModelAdapter;
import rvsnoop.actions.Actions;
import rvsnoop.actions.ExportToHtml;
import rvsnoop.actions.ExportToRecordBundle;
import rvsnoop.actions.ImportFromRecordBundle;
import rvsnoop.actions.PauseAllConnections;
import rvsnoop.ui.RvDetailsPanel;
import rvsnoop.ui.SubjectExplorerEditor;
import rvsnoop.ui.SubjectExplorerRenderer;
import rvsnoop.ui.UIUtils;

/**
 * The single user interface class for the application.
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

    private static final Logger logger = Logger.getLogger();

    private final ApplicationContext context;
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

    private final RecordTypes recordTypes;

    private final ProjectService projectService;

    public MainFrame(ApplicationContext context, Application application, RecordTypes recordTypes, ProjectService projectService) {
        setName("mainFrame");
        this.context = context;
        this.application = application;
        this.recordTypes = recordTypes;
        this.projectService = projectService;
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
        setJMenuBar(createMenuBar(messageLedger));
        getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        getContentPane().add(createToolBar(), BorderLayout.NORTH);
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        pack();

        connectListeners();
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

    private JMenu createEditMenu() {
        final JMenu edit = new JMenu("Edit");
        edit.setMnemonic('e');
        edit.add(application.getAction(Cut.COMMAND));
        edit.add(application.getAction(Copy.COMMAND));
        edit.add(application.getAction(Paste.COMMAND));
        edit.addSeparator();
        edit.add(application.getAction(Search.COMMAND));
        edit.add(application.getAction(SearchBySelection.COMMAND));
        edit.addSeparator();
        edit.add(application.getAction(Republish.COMMAND));
        edit.addSeparator();
        edit.add(application.getAction(PruneEmptySubjects.COMMAND));
        edit.addSeparator();
        edit.add(application.getAction(Delete.COMMAND));
        return edit;
    }

    private JMenu createFileMenu() {
        ActionMap actionMap = context.getActionMap(this);
        final JMenu file = new JMenu("File");
        file.setMnemonic('f');
        file.add(actionMap.get("openProject"));
        final JMenu fileRecent = new JMenu("Recent Projects");
        fileRecent.setIcon(new ImageIcon("/resources/icons/open.png"));
        fileRecent.addMenuListener(new RecentProjectsMenuManager(application, projectService));
        file.add(fileRecent);
        file.addSeparator();
        final JMenu fileExport = new JMenu("Export To");
        fileExport.setIcon(new ImageIcon("/resources/icons/exportTo.png"));
        fileExport.add(application.getAction(ExportToHtml.COMMAND));
        fileExport.add(application.getAction(ExportToRecordBundle.COMMAND));
        file.add(fileExport);
        final JMenu fileImport = new JMenu("Import From");
        fileExport.setIcon(new ImageIcon("/resources/icons/import.png"));
        fileImport.add(application.getAction(ImportFromRecordBundle.COMMAND));
        file.add(fileImport);
        file.addSeparator();
        file.add(application.getAction(NewRvConnection.COMMAND));
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.addMenuListener(new RecentConnectionsMenuManager(application));
        file.add(connRecent);
        return file;
    }

    private JMenuBar createMenuBar(RecordLedgerTable table) {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu());
        bar.add(createEditMenu());
        bar.add(createViewMenu(table));
        return bar;
    }

    private RecordLedgerTable createMessageLedger(RecordLedger ledger) {
        final RecordLedgerTable table = new RecordLedgerTable(ledger, application.getConnections(), recordTypes);
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

    private JToolBar createToolBar() {
        ActionMap actionMap = context.getActionMap(this);
        final JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        toolbar.setRollover(true);
        toolbar.add(actionMap.get("openProject"));
        toolbar.addSeparator();
        toolbar.add(application.getAction(NewRvConnection.COMMAND));
        toolbar.addSeparator();
        toolbar.add(application.getAction(ClearLedger.COMMAND));
        toolbar.addSeparator();
        toolbar.add(application.getAction(PauseAllConnections.COMMAND));
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

    private JMenu createViewMenu(RecordLedgerTable table) {
        final JMenu view = new JMenu("View");
        view.setMnemonic('v');
        view.add(application.getAction(SelectAllRecords.COMMAND));
        view.add(application.getAction(ShowAllColumns.COMMAND));
        view.addSeparator();
        view.add(application.getAction(Filter.COMMAND));
        view.add(application.getAction(FilterBySelection.COMMAND));
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

    public Record getSelectedRecord() {
        try {
            final int index = messageLedger.getSelectedRow();
            return index >= 0 ? application.getFilteredLedger().get(index) : null;
        } catch (IndexOutOfBoundsException e) {
            logger.error(e, "Failed to get selected record from ledger.");
            return null;
        }
    }

    public JTree getSubjectExplorer() {
        return subjectExplorer;
    }

    @Action()
    public void openProject() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new ProjectFileFilter());
        final int option = chooser.showOpenDialog(this);
        if (JFileChooser.APPROVE_OPTION != option) { return; }
        File file = chooser.getSelectedFile();
        ResourceMap resourceMap = context.getResourceMap();
        try {
            file = file.getCanonicalFile();
            logger.info(resourceMap.getString("openProject.info.loading"), file.getName());
            projectService.openProject(file);
            logger.info(resourceMap.getString("openProject.info.loaded"), file.getName());
        } catch (IOException e) {
            logger.error(e, resourceMap.getString("openProject.error.loading"), file.getName());
        }
        try {
            // XXX we should really save connection state in the project file
            //     and restore it after re-opening
            RvConnection.resumeQueue();
        } catch (TibrvException e) {
            logger.error(e, resourceMap.getString("openProject.error.starting"), e.error);
        }
    }

}
