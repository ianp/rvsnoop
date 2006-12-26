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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang.SystemUtils;
import org.rvsnoop.RecordLedgerFormat;
import org.rvsnoop.RecordLedgerFormat.ColumnFormat;
import org.rvsnoop.ui.RecordLedgerTable;

import rvsnoop.Connections;
import rvsnoop.Logger;
import rvsnoop.MessageLedger;
import rvsnoop.PreferencesManager;
import rvsnoop.RecentConnections;
import rvsnoop.RecentProjects;
import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.SubjectHierarchy;
import rvsnoop.TreeModelAdapter;
import rvsnoop.Version;
import rvsnoop.actions.Actions;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventTableModel;

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

    private final class FilterBySelectionListener implements ListSelectionListener {
        FilterBySelectionListener() {
            super();
        }
        public void valueChanged(ListSelectionEvent e) {
            Actions.FILTER_BY_SELECTION.setEnabled(getSelectedRecord() != null);
        }
    }

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

    private static final class WindowCloseListener extends WindowAdapter {
        WindowCloseListener() {
            super();
        }
        public void windowClosing(WindowEvent e) {
            Actions.QUIT.actionPerformed(null);
        }
    }

    private static final int DEFAULT_HEIGHT = 480;

    private static final int DEFAULT_WIDTH = 640;

    private static final Icon ICON_FILTER = Icons.createIcon("/resources/icons/filter.png", 14);

    private static final String TOOLTIP_VISIBLE_COLUMNS = "Show or hide individual table columns";

    private static final Logger logger = Logger.getLogger(UIManager.class);

    public static final UIManager INSTANCE = new UIManager();

    // Popup menus.

    private final JPopupMenu columnsPopup = new JPopupMenu();

    // Main frame.

    private final JFrame frame = new JFrame(Version.getAsStringWithName());

    // Widgets.

    private final JList connectionList = createConnectionList();
    private final JTree subjectExplorer = createSubjectExplorer();
    private final RvDetailsPanel detailsPanel = new RvDetailsPanel();
    private final RecordLedgerTable messageLedger = createMessageLedger();

    // Widget scrollers.

    private final JScrollPane connectionListScroller = createConnectionListScroller(connectionList);
    private final JScrollPane subjectExplorerScroller = createSubjectExplorerScroller(subjectExplorer);
    private final JScrollPane messageLedgerScroller = createMessageLedgerScroller(messageLedger);

    // Widget splitters.

    private final JSplitPane messageLedgerSplitter = createMessageLedgerSplitter(messageLedgerScroller, detailsPanel);
    private final JSplitPane subjectExplorerSplitter = createSubjectExplorerSplitter(subjectExplorerScroller, messageLedgerSplitter);
    private final JSplitPane connectionListSplitter = createConnectionListSplitter(connectionListScroller, subjectExplorerSplitter);

    // Status bar and contents.

    private final StatusBar statusBar = new StatusBar();
    private final StringBuffer statusBarItemFilterBuffer = new StringBuffer(16);
    private final StatusBar.StatusBarItem statusBarItemEncoding = statusBar.createItem();
    private final StatusBar.StatusBarItem statusBarItemCount = statusBar.createItem();

    private UIManager() {
        super();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Icons.APPLICATION);
        frame.setJMenuBar(createMenuBar());
        frame.getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
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
        messageLedgerSelectionModel.addListSelectionListener(new FilterBySelectionListener());
        messageLedgerSelectionModel.addListSelectionListener(new MessageLedgerListener());
        for (final Iterator i = Actions.getActions().iterator(); i.hasNext(); ) {
            final Object action = i.next();
            if (action instanceof ListSelectionListener)
                messageLedgerSelectionModel.addListSelectionListener((ListSelectionListener) action);
        }
        frame.addWindowListener(new WindowCloseListener());
    }

    private void createColumnMenuItem(JPopupMenu popupMenu, final ColumnFormat column, final RecordLedgerFormat format, final EventTableModel model, boolean selected) {
        final JCheckBoxMenuItem item = new JCheckBoxMenuItem(column.getName());
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                item.setSelected(format.getColumns().contains(column));
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // no-op
            }
            public void popupMenuCanceled(PopupMenuEvent e) {
                // no-op
            }
        });
        item.setSelected(selected);
        item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    format.add(column);
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    format.remove(column);
                }
                MessageLedgerRenderer.installStripedRenderers(messageLedger);
            }
        });
        popupMenu.add(item);
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

    private JList createConnectionList() {
        final JList list = new JList(new EventListModel(Connections.getInstance()));
        list.setBorder(BorderFactory.createEmptyBorder());
        list.setCellRenderer(new ConnectionListRenderer(list, false));
        // This line allows the cell renderer to provide a custom tooltip for each node.
        ToolTipManager.sharedInstance().registerComponent(list);
        return list;
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
        edit.add(Actions.CUT);
        edit.add(Actions.COPY);
        edit.add(Actions.PASTE);
        edit.addSeparator();
        edit.add(Actions.SEARCH);
        edit.add(Actions.SEARCH_AGAIN);
        edit.addSeparator();
        edit.add(Actions.REPUBLISH);
        edit.addSeparator();
        edit.add(Actions.PRUNE_EMPTY_SUBJECTS);
        edit.addSeparator();
        edit.add(Actions.DELETE);
        return edit;
    }

    private JMenu createFileMenu() {
        final JMenu file = new JMenu("File");
        file.setMnemonic('f');
        file.add(Actions.OPEN);
        final JMenu fileRecent = new JMenu("Recent Projects");
        fileRecent.setIcon(Icons.OPEN);
        fileRecent.addMenuListener(RecentProjects.INSTANCE.new MenuManager());
        file.add(fileRecent);
        file.addSeparator();
        file.add(Actions.SAVE);
        file.add(Actions.SAVE_AS);
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
        file.add(Actions.ADD_CONNECTION);
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.setIcon(Icons.ADD_CONNECTION);
        connRecent.addMenuListener(RecentConnections.getInstance().new MenuManager());
        file.add(connRecent);
        file.addSeparator();
        file.add(Actions.QUIT);
        return file;
    }

    private JMenu createHelpMenu() {
        final JMenu help = new JMenu("Help");
        help.setMnemonic('h');
        help.add(Actions.HELP);
        help.add(Actions.DISPLAY_HOME_PAGE);
        help.add(Actions.REPORT_BUG);
        help.add(Actions.CHECK_FOR_UPDATES);
        help.add(Actions.SUBSCRIBE_TO_UPDATES);
        help.addSeparator();
        help.add(Actions.DISPLAY_LICENSE);
        help.add(Actions.DISPLAY_ABOUT);
        return help;
    }

    private JMenuBar createMenuBar() {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu());
        bar.add(createEditMenu());
        bar.add(createViewMenu());
        bar.add(createConfigureMenu());
        bar.add(createHelpMenu());
        // Mac OS X screen menu bars do not normally show icons.
        if (SystemUtils.IS_OS_MAC_OSX)
            removeIconsFromMenuElements(bar);
        return bar;
    }

    private RecordLedgerTable createMessageLedger() {
        final RecordLedgerFormat format = new RecordLedgerFormat();
        final EventTableModel model = new EventTableModel(MessageLedger.INSTANCE.getEventList(), format);
        format.setModel(model);
        final RecordLedgerTable table = new RecordLedgerTable(model);
        table.setBackground(Color.WHITE);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowGrid(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setDragEnabled(true);
        table.setTransferHandler(new RecordLedgerTransferHandler());
        MessageLedgerRenderer.installStripedRenderers(table);
        final List columns = format.getColumns();
        final Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator();
        while (i.hasNext()) {
            final ColumnFormat column = (ColumnFormat) i.next();
            createColumnMenuItem(columnsPopup, column, format, model, columns.contains(column));
        }
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
        tree.setCellEditor(new SubjectExplorerEditor(tree));
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

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        toolBar.setRollover(true);
        createToolBarButton(toolBar, Actions.OPEN);
        createToolBarButton(toolBar, Actions.SAVE);
        toolBar.addSeparator();
        createToolBarButton(toolBar, Actions.ADD_CONNECTION);
        toolBar.addSeparator();
        createToolBarButton(toolBar, Actions.CLEAR_LEDGER);
        toolBar.addSeparator();
        createToolBarButton(toolBar, Actions.PAUSE_ALL);
        return toolBar;
    }

    private void createToolBarButton(JToolBar toolBar, Action action) {
        final JButton button = toolBar.add(action);
        // The painted borders on Java toolbar buttons look really bad...
        button.setBorderPainted(false);
        button.setOpaque(false);
    }

    private JMenu createViewMenu() {
        final JMenu view = new JMenu("View");
        view.setMnemonic('v');
        view.add(Actions.SELECT_ALL_MESSAGES);
        view.add(Actions.SHOW_ALL_COLUMNS);
        view.addSeparator();
        view.add(Actions.FILTER);
        view.add(Actions.FILTER_BY_SELECTION);
        view.addSeparator();
        final JMenu viewColumns = new JMenu("Columns");
        viewColumns.setIcon(Icons.FILTER_COLUMNS);
        final JPopupMenu viewColumnsPopup = viewColumns.getPopupMenu();
        final EventTableModel model = (EventTableModel) messageLedger.getModel();
        final RecordLedgerFormat format = messageLedger.getTableFormat();
        final List columns = format.getColumns();
        final Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator();
        while (i.hasNext()) {
            final ColumnFormat column = (ColumnFormat) i.next();
            createColumnMenuItem(viewColumnsPopup, column, format, model, columns.contains(column));
        }
        view.add(viewColumns);
        final JMenu viewTypes = new JMenu("Types");
        viewTypes.setIcon(Icons.FILTER);
        viewTypes.addMenuListener(RecordTypes.getInstance().new MenuManager());
        //viewTypes.getPopupMenu().addPopupMenuListener(RecordTypes.getInstance().new MenuManager());
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
            return index >= 0 ? MessageLedger.INSTANCE.getRecord(index) : null;
        } catch (IndexOutOfBoundsException e) {
            if (Logger.isErrorEnabled())
                logger.error("Failed to get selected record from ledger.", e);
            return null;
        }
    }

    public Record[] getSelectedRecords() {
        return MessageLedger.INSTANCE.getRecords(messageLedger.getSelectedRows());
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
        for (int i = 0, imax = elts.length; i < imax; ++i)
            removeIconsFromMenuElements(elts[i]);
    }

    public void selectRecordInLedger(int index) {
        logger.info("Selecting record " + index);
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

    public void setStatusBarMessage(String message) {
        statusBar.setMessage(message);
    }

    public void setStatusBarWarning(String message) {
        statusBar.setWarning(message);
    }

    public void setSubjectExplorerDividerLocation(final int location) {
        subjectExplorerSplitter.setDividerLocation(location);
    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        // Enable UI notification from the logger if the frame is visible.
        Logger.setRunningHeadless(visible);
    }

    public void updateStatusLabel() {
        final int visible = MessageLedger.INSTANCE.getRowCount();
        final int total = MessageLedger.INSTANCE.getTotalRowCount();
        final String toolTipText = statusBarItemCount.getToolTipText();
        final Icon icon = toolTipText == null || toolTipText.length() == 0 ? null : ICON_FILTER;
        statusBarItemFilterBuffer.setLength(0);
        statusBarItemFilterBuffer.append(visible).append(" / ").append(total);
        statusBarItemCount.set(statusBarItemFilterBuffer.toString(), toolTipText, icon);
    }

}
