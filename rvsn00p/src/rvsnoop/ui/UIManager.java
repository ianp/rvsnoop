/*
 * Class:     UIManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import org.rvsnoop.actions.DisplayAbout;
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
import org.rvsnoop.actions.Quit;
import org.rvsnoop.actions.Republish;
import org.rvsnoop.actions.SaveProjectAs;
import org.rvsnoop.actions.SaveProject;
import org.rvsnoop.ui.ConnectionList;
import org.rvsnoop.ui.ImageFactory;
import org.rvsnoop.ui.RecordLedgerTable;
import org.rvsnoop.ui.RecordTypesMenuManager;
import org.rvsnoop.ui.VisibleColumnsMenuManager;

import rvsnoop.Connections;
import rvsnoop.PreferencesManager;
import rvsnoop.RecentConnections;
import rvsnoop.RecentProjects;
import rvsnoop.Record;
import rvsnoop.SubjectHierarchy;
import rvsnoop.TreeModelAdapter;
import rvsnoop.Version;
import rvsnoop.actions.Actions;

/**
 * The single user interface class for the application.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
// Class provides a static instance instead of a factory method.
// @PMD:REVIEWED:MissingStaticMethodInNonInstantiatableClass: by ianp on 1/17/06 7:58 PM
public final class UIManager {

    private final class MessageLedgerListener implements ListSelectionListener {
        MessageLedgerListener() {
            super();
        }
        public void valueChanged(ListSelectionEvent e) {
            final Record record = getSelectedRecord();
            if (record != null) detailsPanel.setMessage(record);
        }
    }

    private final class TreeExpander extends TreeModelAdapter {
        private boolean isExpanded;

        private TreeExpander() {
            super();
        }

        public void treeNodesInserted(TreeModelEvent e) {
            if (isExpanded) return;
            isExpanded = true;
            subjectExplorer.expandPath(e.getTreePath());
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            if (((TreeNode) SubjectHierarchy.INSTANCE.getRoot()).getChildCount() == 0)
                isExpanded = false;
        }
    }

    private final class WindowCloseListener extends WindowAdapter {
        WindowCloseListener() {
            super();
        }
        public void windowClosing(WindowEvent e) {
            application.shutdown();
        }
    }

    // TODO move defaults into the session state.
    private static final int DEFAULT_HEIGHT = 480;
    private static final int DEFAULT_WIDTH = 640;

    private static final Icon ICON_FILTER = Icons.createIcon("/resources/icons/filter.png", 14);

    private static final String TOOLTIP_VISIBLE_COLUMNS = "Show or hide individual table columns";

    private static final Log log = LogFactory.getLog(UIManager.class);

    // FIXME: Remove this field.
    public static UIManager INSTANCE;

    private final Application application;

    // Popup menus.

    private final JPopupMenu columnsPopup = new JPopupMenu();

    // Main frame.

    private final JFrame frame = new JFrame(Version.getAsStringWithName());

    // Widgets.

    private final ConnectionList connectionList;
    private final JTree subjectExplorer = createSubjectExplorer();
    private final RvDetailsPanel detailsPanel = new RvDetailsPanel();
    private final RecordLedgerTable messageLedger;

    // Widget scrollers.

    private final JScrollPane connectionListScroller;
    private final JScrollPane subjectExplorerScroller;
    private final JScrollPane messageLedgerScroller;

    // Widget splitters.

    private final JSplitPane messageLedgerSplitter;
    private final JSplitPane subjectExplorerSplitter;
    private final JSplitPane connectionListSplitter;

    // Status bar and contents.

    private final StatusBar statusBar = new StatusBar();
    private final StringBuffer statusBarItemFilterBuffer = new StringBuffer(16);
    private final StatusBar.StatusBarItem statusBarItemEncoding = statusBar.createItem();
    private final StatusBar.StatusBarItem statusBarItemCount = statusBar.createItem();

    public UIManager(final Application application) {
        this.application = application;
        connectionList = new ConnectionList(application, Connections.getInstance());
        messageLedger = createMessageLedger(application.getFilteredLedger());
        connectionListScroller = createConnectionListScroller(connectionList);
        subjectExplorerScroller = createSubjectExplorerScroller(subjectExplorer);
        messageLedgerScroller = createMessageLedgerScroller(messageLedger);
        messageLedgerSplitter = createMessageLedgerSplitter(messageLedgerScroller, detailsPanel);
        subjectExplorerSplitter = createSubjectExplorerSplitter(subjectExplorerScroller, messageLedgerSplitter);
        connectionListSplitter = createConnectionListSplitter(connectionListScroller, subjectExplorerSplitter);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Icons.APPLICATION);
        final Actions factory = application.getActionFactory();
        frame.setJMenuBar(createMenuBar(messageLedger, factory));
        frame.getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        frame.getContentPane().add(createToolBar(factory), BorderLayout.NORTH);
        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
        frame.pack();
        frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        UIUtils.centerWindowOnScreen(frame);
        statusBarItemEncoding.set(System.getProperty("file.encoding"), Locale.getDefault().getDisplayName(), null);
        updateStatusLabel();
        connectListeners();
    }

    public void clearDetails() {
        detailsPanel.setMessage(null);
    }

    private void connectListeners() {
        final ListSelectionModel messageLedgerSelectionModel = messageLedger.getSelectionModel();
        messageLedgerSelectionModel.addListSelectionListener(new MessageLedgerListener());
        for (final Iterator i = Actions.getActions().iterator(); i.hasNext(); ) {
            final Object action = i.next();
            if (action instanceof ListSelectionListener)
                messageLedgerSelectionModel.addListSelectionListener((ListSelectionListener) action);
        }
        frame.addWindowListener(new WindowCloseListener());
    }

    private JMenu createConfigureMenu() {
        final JMenu configure = new JMenu("Configure");
        configure.setMnemonic('c');
        configure.add(createConfigureReset());
        configure.add(Actions.CHANGE_TABLE_FONT);
        return configure;
    }

    private JMenuItem createConfigureReset() {
        final JMenuItem result = new JMenuItem("Reset");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                PreferencesManager.INSTANCE.delete();
            }
        });
        return result;
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
        edit.add(Actions.SEARCH);
        edit.add(Actions.SEARCH_AGAIN);
        edit.addSeparator();
        edit.add(factory.getAction(Republish.COMMAND));
        edit.addSeparator();
        edit.add(Actions.PRUNE_EMPTY_SUBJECTS);
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
        fileRecent.addMenuListener(RecentProjects.INSTANCE.new MenuManager());
        file.add(fileRecent);
        file.addSeparator();
        file.add(factory.getAction(SaveProject.COMMAND));
        file.add(factory.getAction(SaveProjectAs.COMMAND));
        final JMenu fileExport = new JMenu("Export To");
        fileExport.setIcon(Icons.EXPORT);
        fileExport.add(Actions.EXPORT_TO_HTML);
        fileExport.add(Actions.EXPORT_TO_RECORD_BUNDLE);
        fileExport.add(Actions.EXPORT_TO_RVSCRIPT);
        fileExport.add(Actions.EXPORT_TO_RVTEST);
        file.add(fileExport);
        final JMenu fileImport = new JMenu("Import From");
        fileExport.setIcon(Icons.IMPORT);
        fileImport.add(Actions.IMPORT_FROM_RECORD_BUNDLE);
        file.add(fileImport);
        file.addSeparator();
        file.add(factory.getAction(NewRvConnection.COMMAND));
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.setIcon(Icons.ADD_CONNECTION);
        connRecent.addMenuListener(RecentConnections.getInstance().new MenuManager());
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
        help.add(Actions.CHECK_FOR_UPDATES);
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
        final RecordLedgerTable table = new RecordLedgerTable(ledger);
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
        final JTree tree = new JTree(SubjectHierarchy.INSTANCE);
        tree.setBorder(BorderFactory.createEmptyBorder());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setCellRenderer(new SubjectExplorerRenderer());
        tree.setCellEditor(new SubjectExplorerEditor(application, tree));
        SubjectHierarchy.INSTANCE.addTreeModelListener(new TreeExpander());
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
        view.add(Actions.SELECT_ALL_MESSAGES);
        view.add(Actions.SHOW_ALL_COLUMNS);
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

    public int getConnectionListDividerLocation() {
        return connectionListSplitter.getDividerLocation();
    }

    public JFrame getFrame() {
        return frame;
    }

    public RecordLedgerTable getMessageLedger() {
        return messageLedger;
    }

    public int getMessageLedgerDividerLocation() {
        return messageLedgerSplitter.getDividerLocation();
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

    public Record[] getSelectedRecords() {
        return application.getFilteredLedger().getAll(messageLedger.getSelectedRows());
    }

    public JTree getSubjectExplorer() {
        return subjectExplorer;
    }

    public int getSubjectExplorerDividerLocation() {
        return subjectExplorerSplitter.getDividerLocation();
    }

    private void removeIconsFromMenuElements(MenuElement elt) {
        if (elt instanceof AbstractButton)
            ((AbstractButton) elt).setIcon(null);
        final MenuElement[] elts = elt.getSubElements();
        for (int i = 0, imax = elts.length; i < imax; ++i) {
            removeIconsFromMenuElements(elts[i]);
        }
    }

    public void selectRecordInLedger(int index) {
        if (log.isInfoEnabled()) { log.info("Selecting record " + index); }
        messageLedger.getSelectionModel().setSelectionInterval(index, index);
        messageLedgerScroller.getVerticalScrollBar().setValue(index);
    }

    public void setConnectionListDividerLocation(final int location) {
        connectionListSplitter.setDividerLocation(location);
    }

    public void setMessageLedgerDividerLocation(final int location) {
        messageLedgerSplitter.setDividerLocation(location);
    }

    public void setMessageLedgerFont(Font font) {
        messageLedger.setFont(font);
        // Without this the default rowNumber height is about 1-million-billion pixels...
        final int rowHeight = messageLedger.getFontMetrics(font).getHeight() + 2;
        messageLedger.setRowHeight(rowHeight);
    }

    public void setStatusBarFilter(String filters) {
        final String text = statusBarItemCount.getText();
        final Icon icon = statusBarItemCount.getIcon();
        statusBarItemCount.set(text, filters, icon);
    }

    public void setSubjectExplorerDividerLocation(final int location) {
        subjectExplorerSplitter.setDividerLocation(location);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    public void updateStatusLabel() {
        final int visible = application.getFilteredLedger().size();
        final int total = application.getLedger().size();
        final String toolTipText = statusBarItemCount.getToolTipText();
        final Icon icon = toolTipText == null || toolTipText.length() == 0 ? null : ICON_FILTER;
        statusBarItemFilterBuffer.setLength(0);
        statusBarItemFilterBuffer.append(visible).append(" / ").append(total);
        statusBarItemCount.set(statusBarItemFilterBuffer.toString(), toolTipText, icon);
    }

}
