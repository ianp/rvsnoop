//:File:    RvSnooperGUI.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import rvsn00p.LogRecordFilter;
import rvsn00p.MsgType;
import rvsn00p.RecentListeners;
import rvsn00p.Record;
import rvsn00p.StringUtils;
import rvsn00p.SubjectElement;
import rvsn00p.SubjectHierarchy;
import rvsn00p.TreeModelAdapter;
import rvsn00p.Version;
import rvsn00p.actions.Actions;
import rvsn00p.ui.Icons;
import rvsn00p.ui.StatusBar;
import rvsn00p.ui.SubjectExplorerEditor;
import rvsn00p.ui.SubjectExplorerRenderer;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.rv.RvController;
import rvsn00p.util.rv.RvParameters;

import com.jgoodies.forms.factories.Borders;
import com.tibco.tibrv.TibrvErrorCallback;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * RvSnooperGUI
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnooperGUI implements TibrvMsgCallback, TibrvErrorCallback {

    private static RvSnooperGUI instance;
    
    private static final Icon ICON_FILTER = Icons.createIcon("/resources/icons/filter.png", 14);
    private static final Icon ICON_MESSAGES = Icons.createIcon("/resources/icons/messages.png", 14);
    
    public static RvSnooperGUI getInstance() {
        return instance;
    }
    
    public static JFrame getAppFrame() {
        return instance._logMonitorFrame;
    }

    private String _name;
    private JFrame _logMonitorFrame;

    private LogTable _table;
    private JTree _subjectExplorerTree;
    private String _searchText;

    private JScrollPane _logTableScrollPane;
    
    private final StatusBar statusBar = new StatusBar();
    private final StatusBar.StatusBarItem statusBarItemFilter = statusBar.createItem();
    private final StatusBar.StatusBarItem statusBarItemFilterTracking = statusBar.createItem();
    private final StatusBar.StatusBarItem statusBarItemFilterSubject = statusBar.createItem();
    private final StatusBar.StatusBarItem statusBarItemEncoding = statusBar.createItem();

    private final StringBuffer statusBarItemFilterBuffer = new StringBuffer(16);
    
    private JButton _pauseButton = null;
    
    private final JTextArea detailsText;
    private final JTree detailsTree;

    private final Map _logLevelMenuItems = new HashMap();
    private final Map _logTableColumnMenuItems = new HashMap();
    private JSplitPane _splitPaneVertical;
    private JSplitPane _splitPaneTableViewer;

    private List _levels = null;
    private List _columns = null;
    private boolean _isDisposed = false;

    private ConfigurationManager _configurationManager = null;

    private RvParameters _lastUsedRvParameters = new RvParameters("7500", "", "tcp:7500");
    private boolean _isPaused = false;

    private static String _trackingIDTextFilter = "";
    private static String _subjectTextFilter = "";

    /**
     * Construct a RvSnooperGUI.
     */
    public RvSnooperGUI(final Set listeners, final String name, boolean tree, boolean text) {
        super();
        if (instance != null) throw new IllegalStateException("There should only be 1 instance of the UI.");
        instance = this;
        _levels = MsgType.getAllDefaultLevels();
        _columns = LogTableColumn.getLogTableColumns();
        _columns = LogTableColumn.getLogTableColumns();
        _name = name;
        detailsText = text ? new JTextArea() : null;
        detailsTree = tree ? new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(""))) : null;

        initComponents();
        updateStatusLabel();

        _logMonitorFrame.addWindowListener(new LogBrokerMonitorWindowAdaptor(this));

        RvController.setErrorCallback(this);
        startListeners(listeners);
    }
    
    /**
     * Start a set of listeners.
     * @param listeners set containing  RvParameters
     */
    public void startListeners(final Set listeners) {

        final Iterator itrl = listeners.iterator();

        while (itrl.hasNext()) {
            try {

                final RvParameters p = (RvParameters) itrl.next();

                RvController.startRvListener(p, this);

                _lastUsedRvParameters = p;

            } catch (ClassCastException e) {
                UIUtils.showError("Error creating listener.", e);
            } catch (TibrvException e) {
                UIUtils.showTibrvException("Error creating listener.", e);
            }
        }

        updateBanner();
    }

    /**
     * Process asynchronous errors from TibRv.
     * TIB/Rendezvous software calls this method in two asynchronous error situations:
     *
     * @param tibrvObject either a TibrvRvaTransport, or a TibrvListener.
     * @param errorCode status code indicating the error.
     * @param message  printable string describing the error.
     * @param throwable   java eception "cause"
     */
    public void onError(final Object tibrvObject, final int errorCode, final String message, final Throwable throwable) {
        JOptionPane.showMessageDialog(_logMonitorFrame, "Rendezvous system error: " + message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Add a recieved tibrv message to the  RvSnooperGUI logtable.
     * @param listener   TibrvListener
     * @param message     TibrvMsg
     */
    public void onMsg(final TibrvListener listener, final TibrvMsg message) {
        // A null listener would be a paste rather than a real onMsg.
        if (listener != null && isPaused()) return;
        // This is a bit clunky - we need to get the subject directly from the
        // message here so we cannot take advantage of the fact the Record hides
        // null subjects from us.
        final String name = message.getSendSubject();
        final SubjectElement subject = ((SubjectHierarchy) _subjectExplorerTree.getModel()).getSubjectElement(name);
        if (!subject.isSelected()) return;

        final Record record = new Record(message, subject);
        // Once I have added support for user definable types and removed this mess
        // the code above can also be made cleaner (i.e. by passing message.getSendSubject
        // directly in to the hierarchy so we can just check for null in one central place).
        if (name != null)
            if (name.lastIndexOf("ERROR") != -1)
                record.setType(MsgType.ERROR);
            else if (name.lastIndexOf("WARN") != -1)
                record.setType(MsgType.WARN);
            else if (name.startsWith("_") || name.startsWith("im"))
                record.setType(MsgType.SYSTEM);

        if (_isDisposed) return;
        // Now add the record to the message ledger, the subject explorer tree, and update the status bar.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ((SubjectHierarchy) _subjectExplorerTree.getModel()).addRecord(record);
                _table.getFilteredLogTableModel().addLogRecord(record);
                updateStatusLabel();
            }
        });
    }

    public void show() {
        if (_logMonitorFrame.isVisible())
            return;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _logMonitorFrame.setVisible(true);
            }
        });
        updateBanner();
    }

    public void updateBanner() {
        final StringBuffer buffer = new StringBuffer();
        if (_name != null)
            buffer.append(_name).append(" ");
        buffer.append(RvController.getTransports().toString());
        buffer.append(" ").append(Version.getAsString());
        this.setTitle(buffer.toString());
    }

    /**
     * Hide the frame for the RvSnooperGUI.
     */
    public void hide() {
        _logMonitorFrame.setVisible(false);
    }

    public void setMaxNumberOfLogRecords(final int maxNumberOfLogRecords) {
        _table.getFilteredLogTableModel().setMaxRecords(maxNumberOfLogRecords);
    }

    public void setTitle(final String title) {
        _logMonitorFrame.setTitle(title);
    }

    public void setFrameSize(final int width, final int height) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = _logMonitorFrame.getSize();
        if (0 < width && width < screen.width) size.width = width;
        if (0 < height && height < screen.height) size.height = height;
        _logMonitorFrame.setSize(size);
    }
    
    public void setTableFont(Font font) {
        _table.setFont(font);
    }

    public Map getLogLevelMenuItems() {
        return _logLevelMenuItems;
    }

    public Map getLogTableColumnMenuItems() {
        return _logTableColumnMenuItems;
    }

    public JCheckBoxMenuItem getTableColumnMenuItem(final LogTableColumn column) {
        return getLogTableColumnMenuItem(column);
    }

    public JTree getCategoryExplorerTree() {
        return _subjectExplorerTree;
    }

    public boolean isPaused() {
        return _isPaused;
    }

    public void pauseListeners() {
        try {
            RvController.pauseAll();
            _isPaused = true;
            _pauseButton.setText("Continue all listeners");
            _pauseButton.setToolTipText("Unpause listeners");
            statusBar.setMessage("All listeners are now paused.");
            _pauseButton.setIcon(Icons.RESUME);
        } catch (TibrvException e) {
            statusBar.setWarning("Pause all listeners failed: " + e.getMessage());
        }
    }

    private void unPauseListeners() {
        try {
            RvController.resumeAll();
            _isPaused = false;
            _pauseButton.setText("Pause all listeners");
            _pauseButton.setToolTipText("Put listeners on hold");
            statusBar.setMessage("All listeners are now active.");
            _pauseButton.setIcon(Icons.PAUSE);
        } catch (TibrvException e) {
            statusBar.setWarning("Resume all listeners failed: " + e.getMessage());
            return;
        }
    }
    
    public List getFilteredRecords() {
        return _table.getFilteredLogTableModel().getFilteredRecords();
    }

    public int getFirstSelectedRow() {
        return _table.getSelectionModel().getMinSelectionIndex();
    }
    
    public int[] getSelectedRecords() {
        return _table.getSelectedRows();
    }

    public void selectRow(final int foundRow) {
        if (foundRow == -1) {
            final String message = _searchText + " not found.";
            JOptionPane.showMessageDialog(
                    _logMonitorFrame,
                    message,
                    "Text not found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        RvSnooperGUI.selectRow(foundRow, _table, _logTableScrollPane);
    }

    public static String getTrackingIdTextFilter() {
        return _trackingIDTextFilter;
    }

    private void clearDetails() {
        if (detailsText != null) detailsText.setText("");
        if (detailsTree != null) detailsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    }

    private void initComponents() {
        _logMonitorFrame = new JFrame(_name);

        _logMonitorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        if (Icons.APPLICATION != null)
            _logMonitorFrame.setIconImage(Icons.APPLICATION);
        setFrameSize(640, 480);
        centerFrame(_logMonitorFrame);

        _table = new LogTable(detailsTree, detailsText);
        _table.setBorder(Borders.EMPTY_BORDER);
        _table.setDetailedView();
        _table.setFont(new Font("DialogInput", Font.PLAIN, 11));
        _logTableScrollPane = new JScrollPane(_table);
        _logTableScrollPane.setBorder(Borders.EMPTY_BORDER);

        _logTableScrollPane.getVerticalScrollBar().addAdjustmentListener(
            new TrackingAdjustmentListener());

        final JSplitPane tableViewerSplitPane = new JSplitPane();
        tableViewerSplitPane.setOneTouchExpandable(true);
        tableViewerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tableViewerSplitPane.setLeftComponent(_logTableScrollPane);
        tableViewerSplitPane.setRightComponent(createDetails());
        // Make sure to do this last..
        //tableViewerSplitPane.setDividerLocation(1.0); Doesn't work
        //the same under 1.2.x & 1.3
        // "350" is a magic number that provides the correct default
        // behaviour under 1.2.x & 1.3.  For example, bumping this
        // number to 400, causes the pane to be completely open in 1.2.x
        // and closed in 1.3
        tableViewerSplitPane.setDividerLocation(350);

        setSplitPaneTableViewer(tableViewerSplitPane);

        _subjectExplorerTree = createSubjectExplorer();

        _table.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());

        final JScrollPane categoryExplorerTreeScrollPane =
                new JScrollPane(_subjectExplorerTree);
        categoryExplorerTreeScrollPane.setPreferredSize(new Dimension(130, 400));

        // Load most recently used file list
        //_mruListnerManager = new MRUListnerManager();

        //
        // Configure the SplitPane between the CategoryExplorer & (LogTable/Detail)
        //

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setRightComponent(tableViewerSplitPane);
        splitPane.setLeftComponent(categoryExplorerTreeScrollPane);
        // Do this last.
        splitPane.setDividerLocation(130);
        setSplitPaneVertical(splitPane);
        //
        // Add the MenuBar, StatusArea, CategoryExplorer|LogTable to the
        // LogMonitorFrame.
        //
        _logMonitorFrame.getRootPane().setJMenuBar(createMenuBar());
        _logMonitorFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
        _logMonitorFrame.getContentPane().add(createToolBar(), BorderLayout.NORTH);
        _logMonitorFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);

        makeLogTableListenToCategoryExplorer();

        statusBarItemEncoding.set(System.getProperty("file.encoding"), null, null);
        //
        // Configure by ConfigurationManager
        //
        final RvSnooperGUI gui = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _configurationManager = new ConfigurationManager(gui, _table);
                unPauseListeners();
                statusBar.setMessage(Version.getAsStringWithName() + " started at " + new Date() + ".");
            }
        });
    }

    private JTree createSubjectExplorer() {
        final SubjectHierarchy model = SubjectHierarchy.getInstance();
        final JTree tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setEditable(true);
        tree.setCellRenderer(new SubjectExplorerRenderer());
        tree.setCellEditor(new SubjectExplorerEditor(tree));
        model.addTreeModelListener(new TreeModelAdapter() {
            private boolean isExpanded;
            public void treeNodesInserted(TreeModelEvent e) {
                if (isExpanded) return;
                isExpanded = true;
                tree.expandPath(e.getTreePath());
            }
            public void treeNodesRemoved(TreeModelEvent e) {
                if (((SubjectElement) model.getRoot()).getChildCount() == 0)
                    isExpanded = false;
            }
        });
        // This line allows the cell renderer to provide a custom tooltip for each node.
        ToolTipManager.sharedInstance().registerComponent(tree);
        return tree;
    }
    
    private LogRecordFilter createLogRecordFilter() {
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final Record record) {
                return getMenuItem(record.getType()).isSelected()
                    && ((SubjectHierarchy) _subjectExplorerTree.getModel()).getSubjectElement(record.getSendSubject()).isSelected();
            }
        };
        return result;
    }

    private void updateStatusLabel() {
        final FilteredLogTableModel model = _table.getFilteredLogTableModel();
        statusBarItemFilterBuffer.setLength(0);
        statusBarItemFilterBuffer.append(model.getRowCount()).append(" / ").append(model.getTotalRowCount());
        statusBarItemFilter.set(statusBarItemFilterBuffer.toString(), null, ICON_MESSAGES);
    }

    private void makeLogTableListenToCategoryExplorer() {
        final ActionListener listener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        };
        ((SubjectHierarchy) _subjectExplorerTree.getModel()).addActionListener(listener);
    }

    private JComponent createDetails() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        if (detailsTree != null) {
            detailsTree.setBorder(Borders.EMPTY_BORDER);
            detailsTree.setRootVisible(true);
            detailsTree.setCellRenderer(new TreeCellRenderer());
            final JScrollPane treeScroller = new JScrollPane(detailsTree,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            treeScroller.setBorder(Borders.EMPTY_BORDER);
            // If the text view is null, just return the tree directly.
            if (detailsText == null) return detailsTree; 
            tabbedPane.addTab("Tree View", treeScroller);
        }
        detailsText.setBorder(Borders.EMPTY_BORDER);
        final JScrollPane textScroller = new JScrollPane(detailsText,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroller.setBorder(Borders.EMPTY_BORDER);
        // If the tree view is null, just return the text directly.
        if (detailsTree == null) return detailsText; 
        tabbedPane.addTab("Text View", textScroller);
        return tabbedPane;
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

    public static void setStatusBarWarning(String message) {
        instance.statusBar.setWarning(message);
    }

    public static void setStatusBarMessage(String message) {
        instance.statusBar.setMessage(message);
    }
    
    private JMenuItem createSaveSelectedAsTextFileMenuItem() {
        final JMenuItem result = new JMenuItem("Save selected as txt");
        result.setMnemonic('r');

        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                final ListSelectionModel lsm = _table.getSelectionModel();
                if (lsm.isSelectionEmpty()) {
                    statusBar.setWarning("No rows selected.");
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();

                    final String sMsg = (String) _table.getModel().getValueAt(selectedRow, _table.getMsgColumnID());
                    final String sSubject = (String) _table.getModel().getValueAt(selectedRow,
                            _table.getSubjectColumnID());

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RvSnooperFileHandler.saveMsgAsTextFile(sSubject, sMsg, Version.getAsStringWithName(), getAppFrame());
                        }
                    });
                }
            }
        });
        try {
            Class.forName("com.tibco.rvscript.tibrvXmlConvert");
            result.setEnabled(true);
        } catch (Exception e) {
            result.setEnabled(false);
        }
        return result;
    }


    private JMenuItem createAllMsgTypesMenuItem() {
        final JMenuItem result = new JMenuItem("Show all Msg Types");
        result.setMnemonic('a');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllMsgTypes(true);
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
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
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
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

    private JMenuItem createResetLogLevelColorMenuItem() {
        final JMenuItem result = new JMenuItem("Reset MsgType Colors");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                // reset the level colors in the map
                MsgType.resetLogLevelColorMap();

                // refresh the table
                _table.getFilteredLogTableModel().refresh();
            }
        });
        return result;
    }

    private void selectAllMsgTypes(final boolean selected) {
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            getMenuItem((MsgType) levels.next()).setSelected(selected);
        }
    }

    private JCheckBoxMenuItem getMenuItem(final MsgType level) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logLevelMenuItems.get(level));
        if (result == null) {
            result = createMenuItem(level);
            _logLevelMenuItems.put(level, result);
        }
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

    private void showLogLevelColorChangeDialog(final JMenuItem result, final MsgType level) {
        final Color newColor = JColorChooser.showDialog(
                _logMonitorFrame,
                "Choose MsgType Color",
                result.getForeground());

        if (newColor != null) {
            // set the color for the record
            MsgType.setLogLevelColorMap(level, newColor);
            _table.getFilteredLogTableModel().refresh();
        }

    }

    private JCheckBoxMenuItem createMenuItem(final MsgType level) {
        final JCheckBoxMenuItem result = new JCheckBoxMenuItem(level.toString());
        result.setSelected(true);
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    private JMenu createViewMenu() {
        final JMenu result = new JMenu("View");
        result.setMnemonic('v');
        final Iterator columns = getLogTableColumns();
        while (columns.hasNext())
            result.add(getLogTableColumnMenuItem((LogTableColumn) columns.next()));
        result.addSeparator();
        result.add(createAllLogTableColumnsMenuItem());
        result.add(createNoLogTableColumnsMenuItem());
        result.addSeparator();
        final Iterator imsgtypes = getMsgTypes();
        while (imsgtypes.hasNext())
            result.add(getMenuItem((MsgType) imsgtypes.next()));
        result.addSeparator();
        result.add(createAllMsgTypesMenuItem());
        result.add(createNoMsgTypesMenuItem());
        result.addSeparator();
        result.add(createLogLevelColorMenu());
        result.add(createResetLogLevelColorMenuItem());
        return result;
    }

    private JCheckBoxMenuItem getLogTableColumnMenuItem(final LogTableColumn column) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) _logTableColumnMenuItems.get(column);
        if (result == null) {
            result = createLogTableColumnMenuItem(column);
            _logTableColumnMenuItems.put(column, result);
        }
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
                _table.setView(selectedColumns);
            }
        });
        return result;
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

    private JMenuItem createAllLogTableColumnsMenuItem() {
        final JMenuItem result = new JMenuItem("Show all Columns");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllLogTableColumns(true);
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                _table.setView(selectedColumns);
            }
        });
        return result;
    }

    private JMenuItem createNoLogTableColumnsMenuItem() {
        final JMenuItem result = new JMenuItem("Hide all Columns");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                selectAllLogTableColumns(false);
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                _table.setView(selectedColumns);
            }
        });
        return result;
    }

    private void selectAllLogTableColumns(final boolean selected) {
        final Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            getLogTableColumnMenuItem((LogTableColumn) columns.next()).setSelected(selected);
        }
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        fileMenu.add(createOpenMI());
        fileMenu.addSeparator();
        fileMenu.add(createSaveHTML());
        fileMenu.add(createSaveSelectedAsTextFileMenuItem());
        fileMenu.add(createSaveAsTextMI());
        fileMenu.addSeparator();
        fileMenu.add(createCloseListener());
        fileMenu.addSeparator();
        fileMenu.add(createFileSaveConfigMI());
        fileMenu.add(createFileLoadConfigMI());
        createMRUListnerListMI(fileMenu);
        fileMenu.addSeparator();
        fileMenu.add(createExitMI());
        return fileMenu;
    }

    /**
     * Menu item added to allow save of filtered table contents to html file
     *
     */
    private JMenuItem createSaveHTML() {
        final JMenuItem result = new JMenuItem("Save Table to HTML file");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                RvSnooperFileHandler.saveTableToHtml(Version.getAsStringWithName(), getAppFrame(), _table);
            }
        });
        return result;
    }

     /**
     * Menu item allows save of configuration to file
     *
     */
    private JMenuItem createFileSaveConfigMI() {
         final JMenuItem result = new JMenuItem("Save configuration to file");
         result.setMnemonic('c');
         result.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent event) {
                 try {
                     final FileDialog fd = new FileDialog(getAppFrame(), "Save config File", FileDialog.SAVE);
                     fd.setDirectory(_configurationManager.getFilename());
                     fd.setFile("*.rs0");
                     fd.show();

                     final String fileName = fd.getDirectory() + fd.getFile();
                     _configurationManager.setFilename(fileName);
                     _configurationManager.store();
                     statusBar.setMessage("Saved configuration in " + fileName);
                 } catch (Exception e) {
                     UIUtils.showError("There was an error whilst saving the configuration.", e);
                 }
             }
         });
         return result;
     }

        /**
     * Menu item allows save of configuration to file
     *
     */
    private JMenuItem createFileLoadConfigMI() {
         final JMenuItem result = new JMenuItem("Load configuration from file");
         result.setMnemonic('c');
         result.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent event) {
                 try {
                     final FileDialog fd = new FileDialog(getAppFrame(), "Open config File", FileDialog.LOAD);
                     fd.setDirectory(_configurationManager.getFilename());
                     fd.setFile("*.rs0");
                     fd.show();

                     final String fileName = fd.getDirectory() + fd.getFile();
                     _configurationManager.setFilename(fileName);
                     _configurationManager.load();
                     statusBar.setMessage("Loaded configuration from "+ fileName);
                 } catch (Exception e) {
                     UIUtils.showError("There was an error whilst loading the configuration.", e);
                 }
             }
         });
         return result;
     }

    /**
     * Menu item added to allow save of filtered table contents to rvscript file.
     *
     */
    private JMenuItem createSaveAsTextMI() {
        final JMenuItem result = new JMenuItem("Save Table to text file");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                RvSnooperFileHandler.saveTableToTextFile(Version.getAsStringWithName(), getAppFrame(), _table);
            }
        });
        return result;
    }


    /**
     * Menu item added to allow log files to be opened with
     * the  GUI.
     */
    private JMenuItem createOpenMI() {
        final JMenuItem result = new JMenuItem("New Listener...");
        result.setMnemonic('n');
        result.setAccelerator(KeyStroke.getKeyStroke("control N"));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }


    private JMenuItem createSaveConfigMI() {
        final JMenuItem result = new JMenuItem("Save Listeners to file");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }

    private JMenuItem createOpenConfigMI() {
        final JMenuItem result = new JMenuItem("Open Listeners from file");
        result.setMnemonic('o');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }


    private JMenuItem createCloseListener() {
        final JMenuItem result = new JMenuItem("Close All Listeners");
        result.setMnemonic('c');
        result.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                RvController.shutdownAll();
                updateBanner();
            }
        });
        return result;
    }

    /**
     * Creates a Most Recently Used file list to be
     * displayed in the File menu
     */
    private void createMRUListnerListMI(final JMenu menu) {
        final List listeners = RecentListeners.getInstance().getListeners();
        if (listeners.size() == 0) return;
        menu.addSeparator();
        for (final Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            final RvParameters params = (RvParameters) iter.next();
            final JMenuItem item = menu.add(new JMenuItem(params.toString()));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    requestOpenMRU(params);
                }
            });
        }
    }

    private JMenuItem createExitMI() {
        final JMenuItem result = new JMenuItem("Exit");
        result.setMnemonic('x');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestExit();
            }
        });
        return result;
    }

    private JMenu createConfigureMenu() {
        final JMenu configure = new JMenu("Configure");
        configure.setMnemonic('c');
        configure.add(createConfigureSave());
        configure.add(createConfigureReset());
        configure.add(createConfigureMaxRecords());
        configure.add(createConfigureDateFormat());
        configure.add(Actions.CHANGE_TABLE_FONT);
        return configure;
    }

    private JMenuItem createConfigureSave() {
        final JMenuItem result = new JMenuItem("Save");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                saveConfiguration();
            }
        });

        return result;
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

    private void saveConfiguration() {
        try {
            _configurationManager.store();
        } catch (IOException e) {
            UIUtils.showError("Error saving configuration to disk.", e);
        }
    }

    private void resetConfiguration() {
        _configurationManager.delete();
    }

    private void setMaxRecordConfiguration() {
        final String title = "Max Records";
        final String question = "Enter the maximum number of records to display";
        final String number = JOptionPane.showInputDialog(getAppFrame(), question, title, JOptionPane.QUESTION_MESSAGE);
        try {
            if (number != null && number.length() > 0)
                setMaxNumberOfLogRecords(Integer.parseInt(number));
        } catch (Exception e) {
            UIUtils.showError("‘" + number + "’ is not a valid number of records.", e);
            setMaxRecordConfiguration();
        }
    }

    private void setDateConfiguration() {
        final String title = "Date Format";
        final String question = "Enter a date format to use";
        final String format = JOptionPane.showInputDialog(getAppFrame(), question, title, JOptionPane.QUESTION_MESSAGE);
        try {
            if (format != null && format.length() > 0) StringUtils.setDateFormat(format);
        } catch (Exception e) {
            UIUtils.showError("‘" + format + "’ is not a valid date pattern.", e);
            setDateConfiguration();
        }
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

    private JMenuItem createEditFilterTIDMI() {
        final JMenuItem editFilterNDCMI = new JMenuItem("Filter by tracking id");
        editFilterNDCMI.setMnemonic('t');
        editFilterNDCMI.setAccelerator(KeyStroke.getKeyStroke("control shift T"));
        editFilterNDCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final String inputValue =
                                JOptionPane.showInputDialog(
                                        _logMonitorFrame,
                                        "Filter by this tracking id: ",
                                        "Filter Log Records by tracking id",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                        setTIDTextFilter(inputValue);
                        filterByTID();
                        _table.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }

        );
        return editFilterNDCMI;
    }

    private JMenuItem createEditFilterBySubjectMI() {
        final JMenuItem editFilterSubjectMI = new JMenuItem("Filter by subject");
        editFilterSubjectMI.setAccelerator(KeyStroke.getKeyStroke("control shift Y"));
        editFilterSubjectMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final String inputValue =
                                JOptionPane.showInputDialog(
                                        _logMonitorFrame,
                                        "Filter by this subject ",
                                        "Filter Log Records by subject",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                        setTIDTextFilter(inputValue);
                        filterBySubject();
                        _table.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }

        );
        return editFilterSubjectMI;
    }

    private JMenuItem createEditFilterBySelectedTIDMI() {
        final JMenuItem editFilterTIDMI = new JMenuItem("Filter by selected tracking id");
        editFilterTIDMI.setMnemonic('s');
        editFilterTIDMI.setAccelerator(KeyStroke.getKeyStroke("control T"));
        editFilterTIDMI.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {


                final ListSelectionModel lsm = _table.getSelectionModel();

                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();

                    final FilteredLogTableModel ftm;
                    ftm = _table.getFilteredLogTableModel();


                    final String sTID = (String) _table.getModel().getValueAt(selectedRow, _table.getTIDColumnID());
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

    private JMenuItem createEditFilterBySelectedSubjectMI() {
        final JMenuItem editFilterSubjectMI = new JMenuItem("Filter by selected subject");
        editFilterSubjectMI.setMnemonic('y');
        editFilterSubjectMI.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        editFilterSubjectMI.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                final ListSelectionModel lsm = _table.getSelectionModel();

                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();

                    final FilteredLogTableModel ftm;
                    ftm = _table.getFilteredLogTableModel();


                    final String s = (String) _table.getModel().getValueAt(selectedRow, _table.getSubjectColumnID());
                    if (s != null) {
                        setSubjectTextFilter(s);
                        filterBySubject();
                        ftm.refresh();
                    }
                }

            }
        }
        );


        return editFilterSubjectMI;
    }

    private void setTIDTextFilter(final String text) {
        _trackingIDTextFilter = text != null ? text : "";
    }
    
    private void filterByTID() {
        final String text = _trackingIDTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        _table.getFilteredLogTableModel().
                setLogRecordFilter(createTIDLogRecordFilter(text));
        statusBarItemFilterTracking.set("TID", text, ICON_FILTER);
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

    private void filterBySubject() {
        final String text = _subjectTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        _table.getFilteredLogTableModel().
                setLogRecordFilter(createSubjectLogRecordFilter(text));
        statusBarItemFilterSubject.set("Subject", text, ICON_FILTER);
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
                        && ((SubjectHierarchy) _subjectExplorerTree.getModel()).getSubjectElement(record.getSendSubject()).isSelected();
                }
            }
        };

        return result;
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
                        && ((SubjectHierarchy) _subjectExplorerTree.getModel()).getSubjectElement(subject).isSelected();
                }
            }
        };

        return result;
    }

    private JMenuItem createEditRemoveAllFiltersTIDMI() {
        final JMenuItem editRestoreAllNDCMI = new JMenuItem("Remove all filters");
        editRestoreAllNDCMI.setMnemonic('r');
        editRestoreAllNDCMI.setAccelerator(KeyStroke.getKeyStroke("control R"));
        editRestoreAllNDCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        _table.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());
                        // reset the text filter
                        setTIDTextFilter("");
                        _table.getFilteredLogTableModel().refresh();
                        updateStatusLabel();
                    }
                }
        );
        return editRestoreAllNDCMI;
    }


    private JToolBar createToolBar() {
        final JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        final JButton listenerButton = new JButton("Add Listener", Icons.NEW_LISTENER);
        listenerButton.setToolTipText("Create new Rv Listener.");
        listenerButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        requestNewRvListener(null);
                    }
                }
        );

        final JButton newButton = new JButton("Clear Log Table", Icons.CLEAR_LEDGER);
        newButton.setToolTipText("Clear Log Table.");
        newButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        _table.clearLogRecords();
                        ((SubjectHierarchy) _subjectExplorerTree.getModel()).reset();
                        updateStatusLabel();
                        clearDetails();
                        Record.resetSequence();
                    }
                }
        );

        _pauseButton = new JButton("Pause all listeners");
        _pauseButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        if (isPaused()) {
                            unPauseListeners();
                        } else {
                            pauseListeners();
                        }
                    }
                }
        );

        tb.add(listenerButton);
        tb.add(newButton);
        tb.add(_pauseButton);

        return tb;
    }

    private static void centerFrame(final JFrame frame) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension comp = frame.getSize();

        frame.setLocation(((screen.width - comp.width) / 2),
                ((screen.height - comp.height) / 2));

    }

    /**
     * Uses a Dialog box to accept a URL to a file to be opened
     * with the  GUI.
     */
    private void requestNewRvListener(final RvParameters p) {

        try {

            RvSnooperRvTransportInputDialog inputDialog = null;
            if (p != null) {
                inputDialog = new RvSnooperRvTransportInputDialog(p);
            } else {
                inputDialog = new RvSnooperRvTransportInputDialog(_lastUsedRvParameters);
            }
            inputDialog.setVisible(true);

            if (inputDialog.isCancelled())
                return;
            _lastUsedRvParameters = inputDialog.getParameters();
            RvController.startRvListener(_lastUsedRvParameters, this);
            updateBanner();
            //_mruListnerManager.set(_lastUsedRvParameters);
            //updateMRUList();
        } catch (TibrvException e) {
            UIUtils.showTibrvException("Error creating listener.", e);
        }

    }

    /**
     * Removes old file list and creates a new file list
     * with the updated MRU list.
     */
    private void updateMRUList() {
        final JMenu menu = _logMonitorFrame.getJMenuBar().getMenu(0);
        menu.removeAll();
        menu.add(createOpenMI());
        menu.addSeparator();
        menu.add(createSaveConfigMI());
        menu.add(createOpenConfigMI());
        menu.add(createCloseListener());
        createMRUListnerListMI(menu);
        menu.addSeparator();
        menu.add(createExitMI());
    }
    
    public void removeAll(Collection records) {
        _table.getFilteredLogTableModel().removeAll(records);
    }

    /**
     * Start a listener from the recent listeners list.
     * @param params
     */
    private void requestOpenMRU(RvParameters params) {
        if (RecentListeners.getInstance().promote(params))
            try {
                RvController.startRvListener(params, this);
            } catch (TibrvException e) {
                UIUtils.showTibrvException("Unable to start listener.", e);
            }
        updateMRUList();
    }

    private void requestExit() {
        try {
            RecentListeners.getInstance().store();
        } catch (IOException ignored) {
            // We can live with not saving the recent listeners list.
        }
        final String question = "Are you sure you want to quit?";
        final String title = "Confirm Quit";
        final int option = JOptionPane.showConfirmDialog(_logMonitorFrame, question, title, JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            _logMonitorFrame.dispose();
            _isDisposed = true;
            try {
                RvController.shutdownAll();
                RvController.close();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            System.exit(0);
        }
    }

    private Iterator getMsgTypes() {
        return _levels.iterator();
    }

    private Iterator getLogTableColumns() {
        return _columns.iterator();
    }

    public Iterator getSubscriptions() {
        return RvController.getTransports().iterator();
    }

    public Font getTableFont() {
        return _table.getFont();
    }
    
    private void setSplitPaneVertical(final JSplitPane _splitPaneVertical) {
        this._splitPaneVertical = _splitPaneVertical;
    }

    public int getSplitPaneVerticalPos() {
        return _splitPaneVertical.getDividerLocation();
    }

    public void setSplitPaneVerticalPos(final int location) {
        _splitPaneVertical.setDividerLocation(location);
    }

    public int getSplitPaneTableViewerPos() {
        return _splitPaneTableViewer.getDividerLocation();
    }


    public void setSplitPaneTableViewer(final JSplitPane _splitPaneTableViewer) {
        this._splitPaneTableViewer = _splitPaneTableViewer;
    }

    public void setSplitPaneTableViewerPos(final int location) {
        this._splitPaneTableViewer.setDividerLocation(location);
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

    private class LogBrokerMonitorWindowAdaptor extends WindowAdapter {
        private final RvSnooperGUI _monitor;

        public LogBrokerMonitorWindowAdaptor(final RvSnooperGUI monitor) {
            super();
            _monitor = monitor;
        }

        public void windowClosing(final WindowEvent ev) {
            _monitor.requestExit();
        }
    }

}
