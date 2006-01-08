//:File:    RvSnooperGUI.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;

import rvsnoop.LogRecordFilter;
import rvsnoop.Logger;
import rvsnoop.MsgType;
import rvsnoop.PreferencesManager;
import rvsnoop.RecentConnections;
import rvsnoop.RecentProjects;
import rvsnoop.Record;
import rvsnoop.RvConnection;
import rvsnoop.StringUtils;
import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;
import rvsnoop.TreeModelAdapter;
import rvsnoop.Version;
import rvsnoop.actions.Actions;
import rvsnoop.ui.ConnectionListRenderer;
import rvsnoop.ui.Icons;
import rvsnoop.ui.RvDetailsPanel;
import rvsnoop.ui.StatusBar;
import rvsnoop.ui.SubjectExplorerEditor;
import rvsnoop.ui.SubjectExplorerRenderer;
import rvsnoop.ui.TrackingAdjustmentListener;
import rvsnoop.ui.UIUtils;

/**
 * RvSnooperGUI
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnooperGUI {
    
    private class ConnectionListListener extends MouseAdapter {
        private final JPopupMenu menu = new JPopupMenu();
        ConnectionListListener() {
            super();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(MouseEvent e) {
            if (!e.isPopupTrigger()) return;
            final RvConnection connection = (RvConnection) connectionList.getSelectedValue();
            if (connection == null) return;
            menu.removeAll();
            menu.add(connection.getStartAction());
            menu.add(connection.getPauseAction());
            menu.add(connection.getStopAction());
            menu.show(connectionList, e.getX(), e.getY());
        }

    }

    private static String _subjectTextFilter = "";
    
    private static String _trackingIDTextFilter = "";
    
    private static final Icon ICON_FILTER = Icons.createIcon("/resources/icons/filter.png", 14);
    
    private static final Icon ICON_MESSAGES = Icons.createIcon("/resources/icons/messages.png", 14);
    
    private static RvSnooperGUI instance;
    
    private static final Logger logger = Logger.getLogger(RvSnooperGUI.class);

    public static JFrame getFrame() {
        return instance.frame;
    }

    public static RvSnooperGUI getInstance() {
        return instance;
    }

    public static String getTrackingIdTextFilter() {
        return _trackingIDTextFilter;
    }
    
    /**
     * Selects a the specified row in the specified JTable and scrolls
     * the specified JScrollpane to the newly selected row. More importantly,
     * the call to repaint() delayed long enough to have the table
     * properly paint the newly selected row which may be offscre
     * @param table should belong to the specified JScrollPane
     */
    public static void selectRow(int row, final JTable table, JScrollPane pane) {
        try {
            if (row < table.getModel().getRowCount()) {
                // First we post some requests to the AWT event loop...
                pane.getVerticalScrollBar().setValue(row * table.getRowHeight());
                table.getSelectionModel().setSelectionInterval(row, row);
                // ...then queue a repaint to run once they have completed.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        table.repaint();
                    }
                });
            }
        } catch (NullPointerException ignored) {
            // Deliberately ignored, method parameters may be null.
        }
    }

    public static void setStatusBarMessage(String message) {
        instance.statusBar.setMessage(message);
    }
    
    public static void setStatusBarWarning(String message) {
        instance.statusBar.setWarning(message);
    }

    private final List _columns = LogTableColumn.getLogTableColumns();
    
    private final List _levels = MsgType.getAllDefaultLevels();
    
    private final Map _logLevelMenuItems = new HashMap();
    
    private final Map _logTableColumnMenuItems = new HashMap();
    
    private String _searchText;
    
    // Main frame.

    private final JFrame frame = new JFrame(Version.getAsStringWithName());
    
    // Widgets.
    
    private final JList connectionList = createConnectionList();
    
    private final RvDetailsPanel detailsPanel = new RvDetailsPanel();
    
//    private final JTree detailsTree = createDetailsTree();
    
    private final LogTable messageLedger = createMessageLedger(detailsPanel);
    
    private final JTree subjectExplorer = createSubjectExplorer();

    // Widget scrollers.

    private final JScrollPane connectionListScroller = createConnectionListScroller(connectionList);
    
    private final JScrollPane messageLedgerScroller = createMessageLedgerScroller(messageLedger);
    
    private final JScrollPane subjectExplorerScroller = createSubjectExplorerScroller(subjectExplorer);
    
    // Widget splitters.

    private final JSplitPane messageLedgerSplitter = createMessageLedgerSplitter(messageLedgerScroller, detailsPanel);

    private final JSplitPane subjectExplorerSplitter = createSubjectExplorerSplitter(subjectExplorerScroller, messageLedgerSplitter);

    private final JSplitPane connectionListSplitter = createConnectionListSplitter(connectionListScroller, subjectExplorerSplitter);
    
    // Status bar and contents.

    private final StatusBar statusBar = new StatusBar();
    
    private final StringBuffer statusBarItemFilterBuffer = new StringBuffer(16);

    private final StatusBar.StatusBarItem statusBarItemEncoding = statusBar.createItem();
    
    private final StatusBar.StatusBarItem statusBarItemCount = statusBar.createItem();

    private final StatusBar.StatusBarItem statusBarItemFilterSubject = statusBar.createItem();
    
    private final StatusBar.StatusBarItem statusBarItemFilterTracking = statusBar.createItem();

    /**
     * Construct a RvSnooperGUI.
     */
    public RvSnooperGUI() {
        super();
        if (instance != null) throw new IllegalStateException("There should only be 1 instance of the UI.");
        instance = this;

        initComponents();
        updateStatusLabel();

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Actions.QUIT.actionPerformed(null);
            }
        });
    }

    public void addRecordToLedger(Record record) {
        messageLedger.getFilteredLogTableModel().addLogRecord(record);
    }

    public void clearDetails() {
        detailsPanel.setMessage(null);
    }
    
    private JMenuItem createAllLogTableColumnsMenuItem() {
        final JMenuItem result = new JMenuItem("Show all Columns");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllLogTableColumns(true);
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                messageLedger.setView(selectedColumns);
            }
        });
        return result;
    }

    private JMenuItem createAllMsgTypesMenuItem() {
        final JMenuItem result = new JMenuItem("Show all Msg Types");
        result.setMnemonic('a');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllMsgTypes(true);
                messageLedger.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    private JMenuItem createConfigureDateFormat() {

        final JMenuItem result = new JMenuItem("Configure Date Format");
        result.setMnemonic('d');

        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setDateConfiguration();
            }
        });

        return result;
    }

    private JMenuItem createConfigureMaxRecords() {

        final JMenuItem result = new JMenuItem("Set Max Number of Records");
        result.setMnemonic('m');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setMaxRecordConfiguration();
            }
        });

        result.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hide();
                }
            }
        });

        return result;
    }

    private JMenu createConfigureMenu() {
        final JMenu configure = new JMenu("Configure");
        configure.setMnemonic('c');
        configure.add(createConfigureReset());
        configure.add(createConfigureMaxRecords());
        configure.add(createConfigureDateFormat());
        configure.add(Actions.CHANGE_TABLE_FONT);
        return configure;
    }

    private JMenuItem createConfigureReset() {
        final JMenuItem result = new JMenuItem("Reset");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resetConfiguration();
            }
        });

        return result;
    }

    private JList createConnectionList() {
        final JList list = new JList(RvConnection.getListModel());
        list.setBorder(BorderFactory.createEmptyBorder());
        list.setCellRenderer(new ConnectionListRenderer(false));
        list.addMouseListener(new ConnectionListListener());
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
    
//    private JTree createDetailsTree() {
//        final JTree tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("")));
//        tree.setBorder(BorderFactory.createEmptyBorder());
//        tree.setRootVisible(true);
//        tree.setCellRenderer(new LazyTreeNode.Renderer());
//        return tree; 
//    }

//    private JScrollPane createDetailsTreeScroller(JTree detailsTree) {
//        final JScrollPane scroller = new JScrollPane(detailsTree);
//        scroller.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY));
//        return scroller;
//    }

    private JMenuItem createEditFilterBySelectedSubjectMI() {
        final JMenuItem editFilterSubjectMI = new JMenuItem("Filter by selected subject");
        editFilterSubjectMI.setMnemonic('y');
        editFilterSubjectMI.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        editFilterSubjectMI.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                
                final ListSelectionModel lsm = messageLedger.getSelectionModel();
                
                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();
                    
                    final FilteredLogTableModel ftm;
                    ftm = messageLedger.getFilteredLogTableModel();
                    
                    
                    final String s = (String) messageLedger.getModel().getValueAt(selectedRow, messageLedger.getSubjectColumnID());
                    if (s != null) {
                        setSubjectTextFilter(s);
                        filterBySubject();
                        ftm.refresh();
                    }
                }
                
            }
        });
        return editFilterSubjectMI;
    }

    private JMenuItem createEditFilterBySelectedTIDMI() {
        final JMenuItem editFilterTIDMI = new JMenuItem("Filter by selected tracking id");
        editFilterTIDMI.setMnemonic('s');
        editFilterTIDMI.setAccelerator(KeyStroke.getKeyStroke("control T"));
        editFilterTIDMI.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {


                final ListSelectionModel lsm = messageLedger.getSelectionModel();

                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();

                    final FilteredLogTableModel ftm;
                    ftm = messageLedger.getFilteredLogTableModel();


                    final String sTID = (String) messageLedger.getModel().getValueAt(selectedRow, messageLedger.getTIDColumnID());
                    if (sTID != null) {
                        setTIDTextFilter(sTID);
                        filterByTID();
                        ftm.refresh();
                    }
                }

            }
        }
        );


        return editFilterTIDMI;
    }
    
    private JMenuItem createEditFilterBySubjectMI() {
        final JMenuItem editFilterSubjectMI = new JMenuItem("Filter by subject");
        editFilterSubjectMI.setAccelerator(KeyStroke.getKeyStroke("control shift Y"));
        editFilterSubjectMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final String inputValue =
                                JOptionPane.showInputDialog(
                                        frame,
                                        "Filter by this subject ",
                                        "Filter Log Records by subject",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                        setTIDTextFilter(inputValue);
                        filterBySubject();
                        messageLedger.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }

        );
        return editFilterSubjectMI;
    }
    
    private JMenuItem createEditFilterTIDMI() {
        final JMenuItem editFilterNDCMI = new JMenuItem("Filter by tracking id");
        editFilterNDCMI.setMnemonic('t');
        editFilterNDCMI.setAccelerator(KeyStroke.getKeyStroke("control shift T"));
        editFilterNDCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final String inputValue =
                                JOptionPane.showInputDialog(
                                        frame,
                                        "Filter by this tracking id: ",
                                        "Filter Log Records by tracking id",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                        setTIDTextFilter(inputValue);
                        filterByTID();
                        messageLedger.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }

        );
        return editFilterNDCMI;
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
        edit.add(createEditFilterTIDMI());
        edit.add(createEditFilterBySelectedTIDMI());
        edit.add(createEditFilterBySelectedSubjectMI());
        edit.add(createEditFilterBySubjectMI());
        edit.add(createEditRemoveAllFiltersTIDMI());
        edit.addSeparator();
        edit.add(Actions.PRUNE_EMPTY_SUBJECTS);
        edit.addSeparator();
        edit.add(Actions.DELETE);
        return edit;
    }
    
    private JMenuItem createEditRemoveAllFiltersTIDMI() {
        final JMenuItem editRestoreAllNDCMI = new JMenuItem("Remove all filters");
        editRestoreAllNDCMI.setMnemonic('r');
        editRestoreAllNDCMI.setAccelerator(KeyStroke.getKeyStroke("control R"));
        editRestoreAllNDCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        messageLedger.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());
                        // reset the text filter
                        setTIDTextFilter("");
                        messageLedger.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }
        );
        return editRestoreAllNDCMI;
    }
    
    private JMenu createFileMenu() {
        final JMenu file = new JMenu("File");
        file.setMnemonic('f');
        file.add(Actions.OPEN);
        final JMenu fileRecent = new JMenu("Recent Projects");
        fileRecent.setIcon(Icons.OPEN);
        final PopupMenuListener recentProjects = RecentProjects.getInstance().new MenuManager();
        fileRecent.getPopupMenu().addPopupMenuListener(recentProjects);
        file.add(fileRecent);
        file.addSeparator();
        file.add(Actions.SAVE);
        file.add(Actions.SAVE_AS);
        final JMenu fileExport = new JMenu("Export To");
        fileExport.setIcon(Icons.EXPORT);
        fileExport.add(Actions.EXPORT_TO_HTML);
        fileExport.add(Actions.EXPORT_TO_RVSCRIPT);
        fileExport.add(Actions.EXPORT_TO_RVTEST);
        file.add(fileExport);
        file.addSeparator();
        file.add(Actions.ADD_CONNECTION);
        final JMenu connRecent = new JMenu("Recent Connections");
        connRecent.setIcon(Icons.ADD_CONNECTION);
        final PopupMenuListener recentConnections = RecentConnections.getInstance().new MenuManager();
        connRecent.getPopupMenu().addPopupMenuListener(recentConnections);
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
        help.add(Actions.DISPLAY_WL_ORJAN);
        help.add(Actions.DISPLAY_WL_IAN);
        help.addSeparator();
        help.add(Actions.DISPLAY_LICENSE);
        help.add(Actions.DISPLAY_ABOUT);
        final StringBuffer a = new StringBuffer();
        a.toString();
        return help;
    }

    private JMenu createLogLevelColorMenu() {
        final JMenu colorMenu = new JMenu("Configure MsgType Colors");
        colorMenu.setMnemonic('c');
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            colorMenu.add(createSubMenuItem((MsgType) levels.next()));
        }

        return colorMenu;
    }

    private LogRecordFilter createLogRecordFilter() {
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final Record record) {
                return getMenuItem(record.getType()).isSelected()
                    && SubjectHierarchy.INSTANCE.getSubjectElement(record.getSendSubject()).isSelected();
            }
        };
        return result;
    }

    private JCheckBoxMenuItem createLogTableColumnMenuItem(final LogTableColumn column) {
        final JCheckBoxMenuItem result = new JCheckBoxMenuItem(column.toString());

        result.setSelected(true);
        result.setMnemonic(column.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                messageLedger.setView(selectedColumns);
            }
        });
        return result;
    }

    private JMenuBar createMenuBar() {
        final JMenuBar bar = new JMenuBar();
        bar.add(createFileMenu());
        bar.add(createEditMenu());
        bar.add(createViewMenu());
        bar.add(createConfigureMenu());
        bar.add(createHelpMenu());
        return bar;
    }

    private JCheckBoxMenuItem createMenuItem(final MsgType level) {
        final JCheckBoxMenuItem result = new JCheckBoxMenuItem(level.toString());
        result.setSelected(true);
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                messageLedger.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    private LogTable createMessageLedger(RvDetailsPanel details) {
        final LogTable table = new LogTable(details);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setShowGrid(true);
        return table;
    }
    
    private JScrollPane createMessageLedgerScroller(JTable ledger) {
        final JScrollPane scrollPane = new JScrollPane(ledger);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 0, Color.GRAY));
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new TrackingAdjustmentListener());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    private JSplitPane createMessageLedgerSplitter(JScrollPane ledger, RvDetailsPanel details) {
        final JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, ledger, details);
        splitter.setOneTouchExpandable(true);
        splitter.setDividerLocation(0.5);
        splitter.setBorder(BorderFactory.createEmptyBorder());
        return splitter;
    }

    private JMenuItem createNoLogTableColumnsMenuItem() {
        final JMenuItem result = new JMenuItem("Hide all Columns");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllLogTableColumns(false);
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                messageLedger.setView(selectedColumns);
            }
        });
        return result;
    }

    private JMenuItem createNoMsgTypesMenuItem() {
        final JMenuItem result = new JMenuItem("Hide all MsgTypes");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllMsgTypes(false);
                messageLedger.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    private JMenuItem createResetLogLevelColorMenuItem() {
        final JMenuItem result = new JMenuItem("Reset MsgType Colors");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // reset the level colors in the map
                MsgType.resetLogLevelColorMap();

                // refresh the table
                messageLedger.getFilteredLogTableModel().refresh();
            }
        });
        return result;
    }

    private JTree createSubjectExplorer() {
        final JTree tree = new JTree(SubjectHierarchy.INSTANCE);
        tree.setBorder(BorderFactory.createEmptyBorder());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setCellRenderer(new SubjectExplorerRenderer());
        tree.setCellEditor(new SubjectExplorerEditor(tree));
        SubjectHierarchy.INSTANCE.addTreeModelListener(new TreeModelAdapter() {
            private boolean isExpanded;
            public void treeNodesInserted(TreeModelEvent e) {
                if (isExpanded) return;
                isExpanded = true;
                tree.expandPath(e.getTreePath());
            }
            public void treeNodesRemoved(TreeModelEvent e) {
                if (((SubjectElement) SubjectHierarchy.INSTANCE.getRoot()).getChildCount() == 0)
                    isExpanded = false;
            }
        });
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
    
    private LogRecordFilter createSubjectLogRecordFilter(final String text) {
        setTIDTextFilter(text);
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final Record record) {
                final String subject = record.getSendSubject();
                if (subject == null || _subjectTextFilter == null) {
                    return false;
                } else if (subject.indexOf(_subjectTextFilter) == -1) {
                    return false;
                } else {
                    return getMenuItem(record.getType()).isSelected()
                        && SubjectHierarchy.INSTANCE.getSubjectElement(subject).isSelected();
                }
            }
        };

        return result;
    }

    private JMenuItem createSubMenuItem(final MsgType level) {
        final JMenuItem result = new JMenuItem(level.toString());
        final MsgType logLevel = level;
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                showLogLevelColorChangeDialog(result, logLevel);
            }
        });

        return result;

    }

    private LogRecordFilter createTIDLogRecordFilter(final String text) {
        setTIDTextFilter(text);
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final Record record) {
                final String trackingID = record.getTrackingId();

                if (trackingID == null || _trackingIDTextFilter == null) {
                    return false;
                } else if (trackingID.indexOf(_trackingIDTextFilter) == -1) {
                    return false;
                } else {
                    return getMenuItem(record.getType()).isSelected()
                        && SubjectHierarchy.INSTANCE.getSubjectElement(record.getSendSubject()).isSelected();
                }
            }
        };

        return result;
    }

    private JToolBar createToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        toolBar.setRollover(true);
        // The painted borders on Java toolbar buttons look really bad...
        toolBar.add(Actions.OPEN).setBorderPainted(false);
        toolBar.add(Actions.SAVE).setBorderPainted(false);
        toolBar.addSeparator();
        toolBar.add(Actions.ADD_CONNECTION).setBorderPainted(false);
        toolBar.addSeparator();
        toolBar.add(Actions.CLEAR_LEDGER).setBorderPainted(false);
        toolBar.addSeparator();
        toolBar.add(Actions.PAUSE_ALL).setBorderPainted(false);
        return toolBar;
    }

    private JMenu createViewMenu() {
        final JMenu view = new JMenu("View");
        view.setMnemonic('v');
        for (final Iterator columns = getLogTableColumns(); columns.hasNext(); )
            view.add(getLogTableColumnMenuItem((LogTableColumn) columns.next()));
        view.addSeparator();
        view.add(createAllLogTableColumnsMenuItem());
        view.add(createNoLogTableColumnsMenuItem());
        view.addSeparator();
        final Iterator imsgtypes = getMsgTypes();
        while (imsgtypes.hasNext())
            view.add(getMenuItem((MsgType) imsgtypes.next()));
        view.addSeparator();
        view.add(createAllMsgTypesMenuItem());
        view.add(createNoMsgTypesMenuItem());
        view.addSeparator();
        view.add(createLogLevelColorMenu());
        view.add(createResetLogLevelColorMenuItem());
        return view;
    }

    private void filterBySubject() {
        final String text = _subjectTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        messageLedger.getFilteredLogTableModel().
                setLogRecordFilter(createSubjectLogRecordFilter(text));
        statusBarItemFilterSubject.set("Subject", text, ICON_FILTER);
    }

    private void filterByTID() {
        final String text = _trackingIDTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        messageLedger.getFilteredLogTableModel().
                setLogRecordFilter(createTIDLogRecordFilter(text));
        statusBarItemFilterTracking.set("TID", text, ICON_FILTER);
    }
    
    public JTree getCategoryExplorerTree() {
        return subjectExplorer;
    }
    
    public int getConnectionListDividerLocation() {
        return connectionListSplitter.getDividerLocation();
    }
    
    public List getFilteredRecords() {
        return messageLedger.getFilteredLogTableModel().getFilteredRecords();
    }
    
    public int getFirstSelectedRow() {
        return messageLedger.getSelectionModel().getMinSelectionIndex();
    }

    public Map getLogLevelMenuItems() {
        return _logLevelMenuItems;
    }

    private JCheckBoxMenuItem getLogTableColumnMenuItem(final LogTableColumn column) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) _logTableColumnMenuItems.get(column);
        if (result == null) {
            result = createLogTableColumnMenuItem(column);
            _logTableColumnMenuItems.put(column, result);
        }
        return result;
    }

    public Map getLogTableColumnMenuItems() {
        return _logTableColumnMenuItems;
    }

    private Iterator getLogTableColumns() {
        return _columns.iterator();
    }

    private JCheckBoxMenuItem getMenuItem(final MsgType level) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logLevelMenuItems.get(level));
        if (result == null) {
            result = createMenuItem(level);
            _logLevelMenuItems.put(level, result);
        }
        return result;
    }
    
    public LogTable getMessageLedger() {
        return messageLedger;
    }

    public int getMessageLedgerDividerLocation() {
        return messageLedgerSplitter.getDividerLocation();
    }

    private Iterator getMsgTypes() {
        return _levels.iterator();
    }

    public int[] getSelectedRecords() {
        return messageLedger.getSelectedRows();
    }

    public int getSubjectExplorerDividerLocation() {
        return subjectExplorerSplitter.getDividerLocation();
    }

    public JCheckBoxMenuItem getTableColumnMenuItem(final LogTableColumn column) {
        return getLogTableColumnMenuItem(column);
    }

    public Font getTableFont() {
        return messageLedger.getFont();
    }
    
    /**
     * Hide the frame for the RvSnooperGUI.
     */
    public void hide() {
        frame.setVisible(false);
    }
    
    private void initComponents() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setIconImage(Icons.APPLICATION);

        messageLedger.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());

        
        frame.getRootPane().setJMenuBar(createMenuBar());
        frame.getContentPane().add(connectionListSplitter, BorderLayout.CENTER);
        frame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
        frame.getContentPane().add(statusBar, BorderLayout.SOUTH);
        setFrameSize(640, 480);
        UIUtils.centerWindowOnScreen(frame);

        makeLogTableListenToCategoryExplorer();

        statusBarItemEncoding.set(System.getProperty("file.encoding"), null, null);

    }

    private void makeLogTableListenToCategoryExplorer() {
        final ActionListener listener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                messageLedger.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        };
        SubjectHierarchy.INSTANCE.addActionListener(listener);
    }

    public void removeAll(Collection records) {
        messageLedger.getFilteredLogTableModel().removeAll(records);
    }

    private void resetConfiguration() {
        PreferencesManager.getInstance().delete();
    }
    
    private void selectAllLogTableColumns(final boolean selected) {
        final Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            getLogTableColumnMenuItem((LogTableColumn) columns.next()).setSelected(selected);
        }
    }

    private void selectAllMsgTypes(final boolean selected) {
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            getMenuItem((MsgType) levels.next()).setSelected(selected);
        }
    }

    public void selectRow(final int foundRow) {
        if (foundRow == -1) {
            final String message = _searchText + " not found.";
            JOptionPane.showMessageDialog(
                    frame,
                    message,
                    "Text not found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        RvSnooperGUI.selectRow(foundRow, messageLedger, messageLedgerScroller);
    }

    public void setConnectionListDividerLocation(final int location) {
        connectionListSplitter.setDividerLocation(location);
    }

    private void setDateConfiguration() {
        final String title = "Date Format";
        final String question = "Enter a date format to use";
        final String format = JOptionPane.showInputDialog(getFrame(), question, title, JOptionPane.QUESTION_MESSAGE);
        try {
            if (format != null && format.length() > 0) StringUtils.setDateFormat(format);
        } catch (Exception e) {
            logger.error("‘" + format + "’ is not a valid date pattern.", e);
        }
    }


    public void setFrameSize(final int width, final int height) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = frame.getSize();
        if (0 < width && width < screen.width) size.width = width;
        if (0 < height && height < screen.height) size.height = height;
        frame.setSize(size);
    }

    public void setMaxNumberOfLogRecords(final int maxNumberOfLogRecords) {
        messageLedger.getFilteredLogTableModel().setMaxRecords(maxNumberOfLogRecords);
    }

    private void setMaxRecordConfiguration() {
        final String title = "Max Records";
        final String question = "Enter the maximum number of records to display";
        final String number = JOptionPane.showInputDialog(getFrame(), question, title, JOptionPane.QUESTION_MESSAGE);
        try {
            if (number != null && number.length() > 0)
                setMaxNumberOfLogRecords(Integer.parseInt(number));
        } catch (Exception e) {
            logger.error("‘" + number + "’ is not a valid number of records.", e);
        }
    }
    
    public void setMessageLedgerDividerLocation(final int location) {
        this.messageLedgerSplitter.setDividerLocation(location);
    }
    
    public void setSubjectExplorerDividerLocation(final int location) {
        subjectExplorerSplitter.setDividerLocation(location);
    }

    private void setSubjectTextFilter(final String text) {
        // if no value is set, set it to a blank string
        // otherwise use the value provided
        if (text == null) {
            _subjectTextFilter = "";
        } else {
            _subjectTextFilter = text;
        }
    }
    
    public void setTableFont(Font font) {
        messageLedger.setFont(font);
    }

    private void setTIDTextFilter(final String text) {
        _trackingIDTextFilter = text != null ? text : "";
    }

    public void setTitle(final String title) {
        frame.setTitle(title);
    }
    
    public void show() {
        if (frame.isVisible())
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    private void showLogLevelColorChangeDialog(final JMenuItem result, final MsgType level) {
        final Color newColor = JColorChooser.showDialog(
                frame,
                "Choose MsgType Color",
                result.getForeground());

        if (newColor != null) {
            // set the color for the record
            MsgType.setLogLevelColorMap(level, newColor);
            messageLedger.getFilteredLogTableModel().refresh();
        }

    }

    public void updateStatusLabel() {
        final FilteredLogTableModel model = messageLedger.getFilteredLogTableModel();
        statusBarItemFilterBuffer.setLength(0);
        statusBarItemFilterBuffer.append(model.getRowCount()).append(" / ").append(model.getTotalRowCount());
        statusBarItemCount.set(statusBarItemFilterBuffer.toString(), null, ICON_MESSAGES);
    }

    private List updateView() {
        final ArrayList updatedList = new ArrayList();
        final Iterator columnIterator = _columns.iterator();
        while (columnIterator.hasNext()) {
            final LogTableColumn column = (LogTableColumn) columnIterator.next();
            final JCheckBoxMenuItem result = getLogTableColumnMenuItem(column);
            // check and see if the checkbox is checked
            if (result.isSelected())
                updatedList.add(column);
        }
        return updatedList;
    }

}
