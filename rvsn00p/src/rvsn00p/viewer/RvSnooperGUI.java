//:File:    RvSnooperGUI.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import rvsn00p.LogRecord;
import rvsn00p.LogRecordFilter;
import rvsn00p.MsgType;
import rvsn00p.RecentListeners;
import rvsn00p.actions.Actions;
import rvsn00p.util.DateFormatManager;
import rvsn00p.util.rv.RvController;
import rvsn00p.util.rv.RvParameters;
import rvsn00p.util.rv.RvScriptInfo;
import rvsn00p.util.rv.RvUtils;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerTree;
import rvsn00p.viewer.categoryexplorer.CategoryPath;

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
public class RvSnooperGUI implements TibrvMsgCallback, TibrvErrorCallback {

    public static final String DETAILED_VIEW = "Detailed";
    public static final String VERSION = "RvSn00p v1.3.0";
    public static final String URL = "http://rvsn00p.sf.net";
    
    private static RvSnooperGUI instance;
    
    public static RvSnooperGUI getInstance() {
        return instance;
    }
    
    public static JFrame getAppFrame() {
        return instance._logMonitorFrame;
    }

    protected String _name = null;
    protected JFrame _logMonitorFrame;
    protected int _logMonitorFrameWidth = 550;
    protected int _logMonitorFrameHeight = 500;
    protected LogTable _table;
    protected CategoryExplorerTree _subjectExplorerTree;
    protected String _searchText;
    protected MsgType _leastSevereDisplayedMsgType = MsgType.UNKNOWN;

    protected JScrollPane _logTableScrollPane;
    protected JLabel _statusLabel;
    protected JComboBox _fontSizeCombo;
    protected JComboBox _fontNameCombo;
    protected JButton _pauseButton = null;

    private int _fontSize = 10;
    private String _fontName = "Dialog";
    protected String _currentView = DETAILED_VIEW;

    protected boolean _loadSystemFonts = false;
    protected boolean _trackTableScrollPane = true;
    protected boolean _callSystemExitOnClose = true;
    
    private final JTextArea detailsText;
    private final JTree detailsTree;
    
    protected List _displayedLogBrokerProperties = new Vector();

    protected Map _logLevelMenuItems = new HashMap();
    protected Map _logTableColumnMenuItems = new HashMap();
    protected JSplitPane _splitPaneVertical;
    protected JSplitPane _splitPaneTableViewer;


    protected List _levels = null;
    protected List _columns = null;
    protected boolean _isDisposed = false;

    protected ConfigurationManager _configurationManager = null;

    protected boolean _displaySystemMsgs = true;
    protected boolean _displayIMMsgs = true;
    protected RvParameters _lastUsedRvParameters = new RvParameters();
    protected boolean _isPaused = false;
    protected ClassLoader _cl = null;

    protected static String _trackingIDTextFilter = "";
    protected static String _subjectTextFilter = "";
    protected static boolean useMtrackingInfo = true;

    /**
     * Construct a RvSnooperGUI.
     */
    public RvSnooperGUI(final List MsgTypes, final Set listeners, final String name, boolean tree, boolean text) {
        if (instance != null) throw new IllegalStateException("There should only be 1 instance of the UI.");
        instance = this;
        _levels = MsgTypes;
        _columns = LogTableColumn.getLogTableColumns();
        _columns = LogTableColumn.getLogTableColumns();
        _name = name;
        detailsText = text ? new JTextArea() : null;
        detailsTree = tree ? new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(""))) : null;

        initComponents();

        _logMonitorFrame.addWindowListener(
                new LogBrokerMonitorWindowAdaptor(this));

        initTibco();
        startListeners(listeners);
    }
    
    /**
     * Start a set of listeners.
     * @param listeners set containing  RvParameters
     */
    protected void startListeners(final Set listeners) {

        final Iterator itrl = listeners.iterator();

        while (itrl.hasNext()) {
            try {

                final RvParameters p = (RvParameters) itrl.next();

                p.setDescription(" <a href=\"" + URL + "\">" + VERSION + "</a> ");

                RvController.startRvListener(p, this);

                _lastUsedRvParameters = p;

            } catch (ClassCastException ex) {
                new RvSnooperErrorDialog(
                        getBaseFrame(), ex.getMessage());
            } catch (TibrvException ex) {
                new RvSnooperErrorDialog(
                        getBaseFrame(), "Tibco Listener " + ex.getMessage());

            }
        }

        updateBanner();
    }

    /**
     *  Initialize the tibco bus.
     */
    protected void initTibco() {
        try {
            RvController.open(this);
        } catch (TibrvException e) {
            String s = "Failed to open Tibrv : ";
            s += e.getMessage();
            s += "\nCheck that the \"tibrv\\bin\" (win) and  \"tibrv\\lib\" \n ";
            s += "is found within you PATH (win) or LD_LIBRARY_PATH (unix/linux) ";

            System.err.println(s);
            new RvSnooperErrorDialog(getBaseFrame(), s);
            System.exit(1);
        }
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
        new RvSnooperErrorDialog(
                getBaseFrame(), "A System error occured " + message);
    }


    /**
     * Add a recieved tibrv message to the  RvSnooperGUI logtable.
     * @param listener   TibrvListener
     * @param msg     TibrvMsg
     */
    public void onMsg(final TibrvListener listener, final TibrvMsg msg) {

        if (isPaused()) {
            return;
        }

        final LogRecord r = new LogRecord(msg);

        String name;
        name = msg.getSendSubject();
        try {
            // try to extract the two last subject parts
            final int ix = name.lastIndexOf(".");
            final int ix2 = name.substring(0, ix).lastIndexOf(".");
            name = name.substring(ix2 + 1, name.length());
        } catch (Exception ignored) {
            // Intentionally ignored.
        }

        try {
             r.setTrackingID(RvUtils.getTrackingId(msg));
        } catch (TibrvException e) {
            System.err.println("Error setting tracking ID: " + e.getMessage());
        }

        r.setSendSubject(msg.getSendSubject());
        r.setReplySubject(msg.getReplySubject());

        final String sMsg = msg.getSendSubject();

        if (sMsg.startsWith("_") || sMsg.startsWith("im")) {
            if (sMsg.lastIndexOf("ERROR") != -1 ) {
                r.setType(MsgType.ERROR);
            }
            if (sMsg.lastIndexOf("WARN") != -1 ) {
                r.setType(MsgType.WARN);
            } else {
                r.setType(MsgType.SYSTEM);
            }
        }

        if (sMsg.lastIndexOf("ERROR") != -1) {
            r.setType(MsgType.ERROR);
        } else if (sMsg.lastIndexOf("WARN") != -1) {
            r.setType(MsgType.WARN);
        }

        // todo add more message types
        // todo enable the user to define their own types?

        addMessage(r);

    }

    /**
     * Show the frame for the RvSnooperGUI. Dispatched to the
     * swing thread.
     */
    public void show(final int delay) {
        if (_logMonitorFrame.isVisible()) {
            return;
        }
        // This request is very low priority, let other threads execute first.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Thread.yield();
                pause(delay);
                _logMonitorFrame.setVisible(true);
                changeFontNameCombo(_fontNameCombo, _fontName);
                changeFontSizeCombo(_fontSizeCombo, _fontSize);
            }
        });
    }

    public void show() {
        show(0);
        updateBanner();

    }

    public void updateBanner() {
        String sBanner;

        if (_name != null) {
            sBanner = _name;
            sBanner += " ";
            sBanner += RvController.getTransports().toString();
        } else {
            sBanner = RvController.getTransports().toString();
        }

        sBanner += " ";
        sBanner += VERSION;

        this.setTitle(sBanner);

    }

    /**
     * Dispose of the frame for the RvSnooperGUI.
     */
    public void dispose() {
        _logMonitorFrame.dispose();
        _isDisposed = true;


        try {
            RvController.shutdownAll();
            RvController.close();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }


        if (_callSystemExitOnClose == true) {
            System.exit(0);
        }
    }

    /**
     * Hide the frame for the RvSnooperGUI.
     */
    public void hide() {
        _logMonitorFrame.setVisible(false);
    }

    /**
     * Get the DateFormatManager for formatting dates.
     */
    DateFormatManager getDateFormatManager() {
        return _table.getDateFormatManager();
    }

    /**
     * Set the date format manager for formatting dates.
     */
    void setDateFormatManager(final DateFormatManager dfm) {
        _table.setDateFormatManager(dfm);
    }

    public String getDateFormat() {
        final DateFormatManager dfm = getDateFormatManager();
        return dfm.getPattern();
    }

    public String setDateFormat(final String pattern) {
        final DateFormatManager dfm = new DateFormatManager(pattern);
        setDateFormatManager(dfm);
        return pattern;
    }

    /**
     * Get the value of whether or not System.exit() will be called
     * when the RvSnooperGUI is closed.
     */
    public boolean getCallSystemExitOnClose() {
        return _callSystemExitOnClose;
    }

    /**
     * Set the value of whether or not System.exit() will be called
     * when the RvSnooperGUI is closed.
     */
    public void setCallSystemExitOnClose(final boolean callSystemExitOnClose) {
        _callSystemExitOnClose = callSystemExitOnClose;
    }

    /**
     * Add a log record message to be displayed in the LogTable.
     * This method is thread-safe as it posts requests to the SwingThread
     * rather than processing directly.
     */
    public void addMessage(final LogRecord lr) {
        if (_isDisposed == true) {
            // If the frame has been disposed of, do not log any more
            // messages.
            return;
        }

        SwingUtilities.invokeLater(new AddLogRecordRunnable(lr));
    }

    public void setMaxNumberOfLogRecords(final int maxNumberOfLogRecords) {
        _table.getFilteredLogTableModel().setMaxNumberOfLogRecords(maxNumberOfLogRecords);
    }

    public JFrame getBaseFrame() {
        return _logMonitorFrame;
    }

    public void setTitle(final String title) {
        _logMonitorFrame.setTitle(title);
    }

    public void setFrameSize(final int width, final int height) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (0 < width && width < screen.width) {
            _logMonitorFrameWidth = width;
        }
        if (0 < height && height < screen.height) {
            _logMonitorFrameHeight = height;
        }
        updateFrameSize();
    }

    void setFontSize(final int fontSize) {
        changeFontSizeCombo(_fontSizeCombo, fontSize);
        // setFontSizeSilently(actualFontSize); - changeFontSizeCombo fires event
        // refreshDetailTextArea();
    }
    
    public void addDisplayedProperty(final Object messageLine) {
        _displayedLogBrokerProperties.add(messageLine);
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

    public CategoryExplorerTree getCategoryExplorerTree() {
        return _subjectExplorerTree;
    }

    protected boolean isPaused() {
        return _isPaused;
    }


    protected void pauseListeners() {
        try {
            RvController.pauseAll();
        } catch (TibrvException e) {
            _statusLabel.setText("Pause all listeners failed " + e.getMessage());
            return;
        }
        _isPaused = true;
        _pauseButton.setText("Continue all listeners");
        _pauseButton.setToolTipText("Unpause listeners");
        _statusLabel.setText("All listeners are now paused");
        if (Icons.RESUME != null)
            _pauseButton.setIcon(Icons.RESUME);
    }

    protected void unPauseListeners() {

        try {
            RvController.resumeAll();
        } catch (TibrvException e) {
            _statusLabel.setText("Resume all listeners failed " + e.getMessage());
            return;
        }
        _isPaused = false;

        _pauseButton.setText("Pause all listeners");
        _pauseButton.setToolTipText("Put listeners on hold");
        _statusLabel.setText("All listeners are now active");
        if (Icons.PAUSE != null)
            _pauseButton.setIcon(Icons.PAUSE);
    }


    protected boolean filterRvMsg(final TibrvMsg msg) {
        if (_displaySystemMsgs == false) {
            if (msg.getSendSubject().startsWith("_")) {
                // do not log anyting
                return false;
            }
        }

        if (_displayIMMsgs == false) {
            if (msg.getSendSubject().startsWith("im")) {
                return false;
            }
        }
        return true;
    }


    protected void setSearchText(final String text) {
        _searchText = text;
    }


    protected void findSearchText() {
        final String text = _searchText;
        if (text == null || text.length() == 0) {
            return;
        }
        final int startRow = getFirstSelectedRow();
        final int foundRow = findRecord(
                startRow,
                text,
                _table.getFilteredLogTableModel().getFilteredRecords()
        );
        selectRow(foundRow);
    }

    protected int getFirstSelectedRow() {
        return _table.getSelectionModel().getMinSelectionIndex();
    }

    protected void selectRow(final int foundRow) {
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
        SwingUtils.selectRow(foundRow, _table, _logTableScrollPane);
    }

    protected int findRecord(
            int startRow,
            final String searchText,
            final List records
            ) {
        if (startRow < 0) {
            startRow = 0; // start at first element if no rows are selected
        } else {
            ++startRow; // start after the first selected row
        }
        int len = records.size();

        for (int i = startRow; i < len; ++i) {
            if (matches((LogRecord) records.get(i), searchText)) {
                return i; // found a record
            }
        }
        // wrap around to beginning if when we reach the end with no match
        len = startRow;
        for (int i = 0; i < len; ++i) {
            if (matches((LogRecord) records.get(i), searchText)) {
                return i; // found a record
            }
        }
        // nothing found
        return -1;
    }

    /**
     * Check to see if the any records contain the search string.
     */
    protected static boolean matches(final LogRecord record, final String text) {
        final TibrvMsg message = record.getMessage();

        if (message == null && _trackingIDTextFilter == null || text == null) {
            return false;
        }

        try {
            if (!RvUtils.isTextInMessageData(text, message) &&
                    _trackingIDTextFilter.indexOf(text.toLowerCase()) == -1) {
                return false;
            }
        } catch (TibrvException e) {
            RvUtils.showTibrvException(null, "Error reading field", e);
        }

        return true;
    }

    protected void refreshDetails() {
        if (detailsText != null) detailsText.setText(detailsText.getText());
        if (detailsTree != null) detailsTree.repaint();
    }

    protected void clearDetails() {
        if (detailsText != null) detailsText.setText("");
        if (detailsTree != null) detailsTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
    }

    /**
     * Changes the font selection in the combo box and returns the
     * size actually selected.
     * @return -1 if unable to select an appropriate font
     */
    public static int changeFontSizeCombo(final JComboBox box, final int requestedSize) {
        final int len = box.getItemCount();
        int currentValue;
        Object currentObject;
        Object selectedObject = box.getItemAt(0);
        int selectedValue = Integer.parseInt(String.valueOf(selectedObject));
        for (int i = 0; i < len; ++i) {
            currentObject = box.getItemAt(i);
            currentValue = Integer.parseInt(String.valueOf(currentObject));
            if (selectedValue < currentValue && currentValue <= requestedSize) {
                selectedValue = currentValue;
                selectedObject = currentObject;
            }
        }
        box.setSelectedItem(selectedObject);
        return selectedValue;
    }

    /**
     * Changes the font selection in the combo box and returns the
     * type that is actually selected.
     * @return null if unable to select an appropriate font
     */
    public static String changeFontNameCombo(final JComboBox box, final String requestedName) {
        final int len = box.getItemCount();
        String currentValue;
        currentValue = null;
        Object currentObject;
        Object selectedObject = box.getItemAt(0);
        for (int i = 0; i < len; ++i) {
            currentObject = box.getItemAt(i);
            currentValue = String.valueOf(currentObject);
            if (currentValue.compareToIgnoreCase(requestedName) == 0) {
                selectedObject = currentObject;
            }
        }
        box.setSelectedItem(selectedObject);
        return currentValue;
    }

    /**
     * Does not update gui or cause any events to be fired.
     */
    protected void setFontSizeSilently(final int fontSize) {
        setFontSize(fontSize);
        if (detailsText != null) setFontSize(detailsText, fontSize);
        if (detailsTree != null) setFontSize(detailsTree, fontSize);
        selectRow(0);
        setFontSize(_table, fontSize);
    }

    protected static void setFontSize(final Component component, final int fontSize) {
        final Font oldFont = component.getFont();
        final Font newFont =
                new Font(oldFont.getFontName(), oldFont.getStyle(), fontSize);
        component.setFont(newFont);
    }

    protected void updateFrameSize() {
        _logMonitorFrame.setSize(_logMonitorFrameWidth, _logMonitorFrameHeight);
        centerFrame(_logMonitorFrame);
    }

    protected static void pause(final int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            // Intentionally ignored.
        }
    }

    protected void initComponents() {
        _logMonitorFrame = new JFrame(_name);

        _logMonitorFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        if (Icons.APPLICATION != null)
            _logMonitorFrame.setIconImage(Icons.APPLICATION);
        updateFrameSize();

        _table = new LogTable(detailsTree, detailsText);
        _table.setBorder(BorderFactory.createEmptyBorder());
        setView(_currentView, _table);
        _table.setFont(new Font(getFontName(), Font.PLAIN, getFontSize()));
        _logTableScrollPane = new JScrollPane(_table);
        _logTableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        if (_trackTableScrollPane) {
            _logTableScrollPane.getVerticalScrollBar().addAdjustmentListener(
                    new TrackingAdjustmentListener());
        }

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

        _subjectExplorerTree = new CategoryExplorerTree();

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
        _logMonitorFrame.getContentPane().add(createStatusArea(), BorderLayout.SOUTH);

        makeLogTableListenToCategoryExplorer();
        addTableModelProperties();

        //
        // Configure by ConfigurationManager
        //
        final RvSnooperGUI gui = this;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _configurationManager = new ConfigurationManager(gui, _table);
                unPauseListeners();
                _statusLabel.setText("Started @ " + new Date());
            }
        });
    }

    protected LogRecordFilter createLogRecordFilter() {
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final LogRecord record) {
                final CategoryPath path = new CategoryPath(record.getSubject());
                return
                        getMenuItem(record.getType()).isSelected() &&
                        _subjectExplorerTree.getExplorerModel().isCategoryPathActive(path);
            }
        };
        return result;
    }


    protected void updateStatusLabel() {

        final StringBuffer sb = new StringBuffer(100);
        getRecordsDisplayedMessage(sb);
        getFileEncodingMessage(sb);

        _statusLabel.setText(sb.toString() );
    }

    protected void getRecordsDisplayedMessage(final StringBuffer sb) {
        final FilteredLogTableModel model = _table.getFilteredLogTableModel();
        getStatusText(model.getRowCount(), model.getTotalRowCount(), sb);
    }

    protected void getFileEncodingMessage(final StringBuffer sb) {
        sb.append("  Encoding:");
        sb.append(System.getProperty("file.encoding") );
    }

    protected void addTableModelProperties() {
        final FilteredLogTableModel model = _table.getFilteredLogTableModel();

        addDisplayedProperty(new Object() {
            public String toString() {
                final StringBuffer sb = new StringBuffer(40);
                getRecordsDisplayedMessage(sb);
                return sb.toString();
            }
        });
        addDisplayedProperty(new Object() {
            public String toString() {
                return "Maximum number of displayed LogRecords: "
                        + model._maxNumberOfLogRecords;
            }
        });
    }

    protected static void getStatusText(final int displayedRows, final int totalRows,final StringBuffer sb) {
        sb.append("Displaying: ");
        sb.append(displayedRows);
        sb.append(" records out of a total of: ");
        sb.append(totalRows);
        sb.append(" records. ");
        sb.append(totalRows-displayedRows);
        sb.append(" are filtered.");
    }


    protected void makeLogTableListenToCategoryExplorer() {
        final ActionListener listener = new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        };
        _subjectExplorerTree.getExplorerModel().addActionListener(listener);
    }

    protected JPanel createStatusArea() {
        final JPanel statusArea = new JPanel();
        final JLabel status =
                new JLabel("No records to display.");
        _statusLabel = status;
        status.setHorizontalAlignment(SwingConstants.LEFT);

        statusArea.setBorder(BorderFactory.createEtchedBorder());
        statusArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusArea.add(status);

        return (statusArea);
    }

    private JComponent createDetails() {
        JTabbedPane tabbedPane = new JTabbedPane();
        if (detailsTree != null) {
            detailsTree.setBorder(BorderFactory.createEmptyBorder());
            detailsTree.setRootVisible(true);
            detailsTree.setCellRenderer(new TreeCellRenderer());
            JScrollPane treeScroller = new JScrollPane(detailsTree,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            treeScroller.setBorder(BorderFactory.createEmptyBorder());
            // If the text view is null, just return the tree directly.
            if (detailsText == null) return detailsTree; 
            tabbedPane.addTab("Tree View", detailsTree);
        }
        detailsText.setBorder(BorderFactory.createEmptyBorder());
        JScrollPane textScroller = new JScrollPane(detailsText,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroller.setBorder(BorderFactory.createEmptyBorder());
        // If the tree view is null, just return the text directly.
        if (detailsTree == null) return detailsText; 
        tabbedPane.addTab("Text View", detailsText);
        return tabbedPane;
    }

    protected JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());

        //menuBar.add(createMessageMenu());

        menuBar.add(createMsgTypeMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createConfigureMenu());
        menuBar.add(createHelpMenu());

        return (menuBar);
    }


    protected JMenuItem createSaveSelectedAsTextFileMenuItem() {
        final JMenuItem result = new JMenuItem("Save selected as txt");
        result.setMnemonic('r');

        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                final ListSelectionModel lsm = _table.getSelectionModel();
                if (lsm.isSelectionEmpty()) {
                    _statusLabel.setText("No rows are selected.");
                } else {
                    final int selectedRow = lsm.getMinSelectionIndex();

                    final String sMsg = (String) _table.getModel().getValueAt(selectedRow, _table.getMsgColumnID());
                    final String sSubject = (String) _table.getModel().getValueAt(selectedRow,
                            _table.getSubjectColumnID());

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RvSnooperFileHandler.saveMsgAsTextFile(sSubject, sMsg, VERSION + " " + URL, getBaseFrame(),
                                    _statusLabel);
                        }
                    });
                }
            }
        });

        if (!RvScriptInfo.isAvaliable()) {
            result.setEnabled(false);
        }
        return result;
    }


    protected JMenu createMsgTypeMenu() {
        final JMenu result = new JMenu("Msg Type");
        result.setMnemonic('t');
        final Iterator imsgtypes = getMsgTypes();
        while (imsgtypes.hasNext()) {
            result.add(getMenuItem((MsgType) imsgtypes.next()));
        }

        result.addSeparator();
        result.add(createAllMsgTypesMenuItem());
        result.add(createNoMsgTypesMenuItem());
        result.addSeparator();
        result.add(createLogLevelColorMenu());
        result.add(createResetLogLevelColorMenuItem());

        return result;
    }

    protected JMenuItem createAllMsgTypesMenuItem() {
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

    protected JMenuItem createNoMsgTypesMenuItem() {
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

    protected JMenu createLogLevelColorMenu() {
        final JMenu colorMenu = new JMenu("Configure MsgType Colors");
        colorMenu.setMnemonic('c');
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            colorMenu.add(createSubMenuItem((MsgType) levels.next()));
        }

        return colorMenu;
    }

    protected JMenuItem createResetLogLevelColorMenuItem() {
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

    protected void selectAllMsgTypes(final boolean selected) {
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            getMenuItem((MsgType) levels.next()).setSelected(selected);
        }
    }

    protected JCheckBoxMenuItem getMenuItem(final MsgType level) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logLevelMenuItems.get(level));
        if (result == null) {
            result = createMenuItem(level);
            _logLevelMenuItems.put(level, result);
        }
        return result;
    }

    protected JMenuItem createSubMenuItem(final MsgType level) {
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

    protected void showLogLevelColorChangeDialog(final JMenuItem result, final MsgType level) {
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

    protected JCheckBoxMenuItem createMenuItem(final MsgType level) {
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

    // view menu
    protected JMenu createViewMenu() {
        final JMenu result = new JMenu("View");
        result.setMnemonic('v');
        final Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            result.add(getLogTableColumnMenuItem((LogTableColumn) columns.next()));
        }

        result.addSeparator();
        result.add(createAllLogTableColumnsMenuItem());
        result.add(createNoLogTableColumnsMenuItem());
        return result;
    }

    protected JCheckBoxMenuItem getLogTableColumnMenuItem(final LogTableColumn column) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logTableColumnMenuItems.get(column));
        if (result == null) {
            result = createLogTableColumnMenuItem(column);
            _logTableColumnMenuItems.put(column, result);
        }
        return result;
    }

    protected JCheckBoxMenuItem createLogTableColumnMenuItem(final LogTableColumn column) {
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

    protected List updateView() {
        final ArrayList updatedList = new ArrayList();
        final Iterator columnIterator = _columns.iterator();
        while (columnIterator.hasNext()) {
            final LogTableColumn column = (LogTableColumn) columnIterator.next();
            final JCheckBoxMenuItem result = getLogTableColumnMenuItem(column);
            // check and see if the checkbox is checked
            if (result.isSelected()) {
                updatedList.add(column);
            }
        }

        return updatedList;
    }

    protected JMenuItem createAllLogTableColumnsMenuItem() {
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

    protected JMenuItem createNoLogTableColumnsMenuItem() {
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

    protected void selectAllLogTableColumns(final boolean selected) {
        final Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            getLogTableColumnMenuItem((LogTableColumn) columns.next()).setSelected(selected);
        }
    }

    protected JMenu createFileMenu() {
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
    protected JMenuItem createSaveHTML() {
        final JMenuItem result = new JMenuItem("Save Table to HTML file");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                RvSnooperFileHandler.saveTableToHtml(VERSION, URL, getBaseFrame(), _statusLabel, _table);
            }
        });
        return result;
    }


     /**
     * Menu item allows save of configuration to file
     *
     */
     protected JMenuItem createFileSaveConfigMI() {
         final JMenuItem result = new JMenuItem("Save configuration to file");
         result.setMnemonic('c');
         result.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent e) {
                 try {
                     FileDialog fd = new FileDialog(getBaseFrame(), "Save config File", FileDialog.SAVE);
                     fd.setDirectory(_configurationManager.getFilename());
                     fd.setFile("*.rs0");
                     fd.show();

                     String fileName = fd.getDirectory() + fd.getFile();
                     _configurationManager.setFilename(fileName);
                     _configurationManager.store();
                     _statusLabel.setText("Saved configuration in "+ fileName);
                 } catch (Exception ex) {
                     new RvSnooperErrorDialog(getBaseFrame(), ex.getMessage());
                 }
             }
         });
         return result;
     }

        /**
     * Menu item allows save of configuration to file
     *
     */
     protected JMenuItem createFileLoadConfigMI() {
         final JMenuItem result = new JMenuItem("Load configuration from file");
         result.setMnemonic('c');
         result.addActionListener(new ActionListener() {
             public void actionPerformed(final ActionEvent e) {
                 try {
                     FileDialog fd = new FileDialog(getBaseFrame(), "Open config File", FileDialog.LOAD);
                     fd.setDirectory(_configurationManager.getFilename());
                     fd.setFile("*.rs0");
                     fd.show();

                     String fileName = fd.getDirectory() + fd.getFile();
                     _configurationManager.setFilename(fileName);
                     _configurationManager.load();
                     _statusLabel.setText("Loaded configuration from "+ fileName);
                 } catch (Exception ex) {
                     new RvSnooperErrorDialog(getBaseFrame(), ex.getMessage());
                 }
             }
         });
         return result;
     }

    /**
     * Menu item added to allow save of filtered table contents to rvscript file.
     *
     */
    protected JMenuItem createSaveAsTextMI() {
        final JMenuItem result = new JMenuItem("Save Table to text file");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                try {
                    RvSnooperFileHandler.saveTableToTextFile(VERSION + " " + URL, getBaseFrame(), _statusLabel, _table);
                } catch (Exception ex) {
                    new RvSnooperErrorDialog(getBaseFrame(), ex.getMessage());
                }
            }
        });
        return result;
    }


    /**
     * Menu item added to allow log files to be opened with
     * the  GUI.
     */
    protected JMenuItem createOpenMI() {
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


    protected JMenuItem createSaveConfigMI() {
        final JMenuItem result = new JMenuItem("Save Listeners to file");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }

    protected JMenuItem createOpenConfigMI() {
        final JMenuItem result = new JMenuItem("Open Listeners from file");
        result.setMnemonic('o');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }


    protected JMenuItem createCloseListener() {
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
    protected void createMRUListnerListMI(final JMenu menu) {
        List listeners = RecentListeners.getInstance().getListeners();
        if (listeners.size() == 0) return;
        menu.addSeparator();
        for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            final RvParameters params = (RvParameters) iter.next();
            JMenuItem item = menu.add(new JMenuItem(params.toString()));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    requestOpenMRU(params);
                }
            });
        }
    }

    protected JMenuItem createExitMI() {
        final JMenuItem result = new JMenuItem("Exit");
        result.setMnemonic('x');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                requestExit();
            }
        });
        return result;
    }

    protected JMenu createConfigureMenu() {
        final JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('c');
        configureMenu.add(createConfigureSave());
        configureMenu.add(createConfigureReset());
        configureMenu.add(createConfigureMaxRecords());
        configureMenu.add(createConfigureDateFormat());

        return configureMenu;
    }

    protected JMenuItem createConfigureSave() {
        final JMenuItem result = new JMenuItem("Save");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {

                saveConfiguration();
            }
        });

        return result;
    }

    protected JMenuItem createConfigureReset() {
        final JMenuItem result = new JMenuItem("Reset");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                resetConfiguration();
            }
        });

        return result;
    }

    protected JMenuItem createConfigureMaxRecords() {

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

    protected JMenuItem createConfigureDateFormat() {

        final JMenuItem result = new JMenuItem("Configure Date Format");
        result.setMnemonic('d');

        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                setDateConfiguration();
            }
        });

        return result;
    }

    protected void saveConfiguration() {
        try {
            _configurationManager.store();
        } catch (Exception ex) {
            new RvSnooperErrorDialog(getBaseFrame(), ex.getMessage());
        }
    }

    protected void resetConfiguration() {
        _configurationManager.delete();
    }

    protected void setMaxRecordConfiguration() {
        final RvSnooperInputDialog inputDialog = new RvSnooperInputDialog(
                getBaseFrame(), "Set Max Number of Records", "", 10);

        final String temp = inputDialog.getText();

        if (temp != null) {
            try {
                setMaxNumberOfLogRecords(Integer.parseInt(temp));
            } catch (NumberFormatException e) {
                new RvSnooperErrorDialog(getBaseFrame(),
                        "'" + temp + "' is an invalid parameter.\nPlease try again.");
                setMaxRecordConfiguration();
            }
        }
    }

    protected void setDateConfiguration() {
        final RvSnooperInputDialog inputDialog = new RvSnooperInputDialog(
                getBaseFrame(), "Set DateFormat", "", 10);

        inputDialog.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hide();
                }
            }
        });

        final String temp = inputDialog.getText();

        if (temp != null) {
            try {
                setDateFormat(temp);
            } catch (NumberFormatException e) {
                new RvSnooperErrorDialog(getBaseFrame(),
                        "'" + temp + "' is an invalid parameter.\nPlease try again.");
                setMaxRecordConfiguration();
            }
        }
    }


    protected JMenu createHelpMenu() {
        final JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        helpMenu.add(Actions.DISPLAY_HOME_PAGE);
        helpMenu.add(Actions.REPORT_BUG);
        helpMenu.add(Actions.CHECK_FOR_UPDATES);
        helpMenu.add(Actions.SUBSCRIBE_TO_UPDATES);
        helpMenu.add(createHelpProperties());
        helpMenu.addSeparator();
        helpMenu.add(Actions.DISPLAY_WL_IAN);
        helpMenu.add(Actions.DISPLAY_WL_ORJAN);
        helpMenu.addSeparator();
        helpMenu.add(Actions.DISPLAY_LICENSE);
        helpMenu.add(Actions.DISPLAY_ABOUT);
        final StringBuffer a = new StringBuffer();
        a.toString();
        return helpMenu;
    }


    protected JMenuItem createHelpProperties() {
        final JMenuItem result = new JMenuItem("Show Properties");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                showPropertiesDialog();
            }
        });
        return result;
    }

    protected void showPropertiesDialog() {
        JOptionPane.showMessageDialog(
                _logMonitorFrame,
                _displayedLogBrokerProperties.toArray(),
                "Properties",
                JOptionPane.PLAIN_MESSAGE);
    }

    protected JMenu createEditMenu() {
        final JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        editMenu.add(createEditFindMI());
        editMenu.add(createEditFindNextMI());
        editMenu.addSeparator();
        editMenu.add(createEditFilterTIDMI());
        editMenu.add(createEditFilterBySelectedTIDMI());
        editMenu.add(createEditFilterBySelectedSubjectMI());
        editMenu.add(createEditFilterBySubjectMI());
        editMenu.add(createEditRemoveAllFiltersTIDMI());
        return editMenu;
    }

    protected JMenuItem createEditFindNextMI() {
        final JMenuItem editFindNextMI = new JMenuItem("Find Next");
        editFindNextMI.setMnemonic('n');
        editFindNextMI.setAccelerator(KeyStroke.getKeyStroke("F3"));
        editFindNextMI.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                findSearchText();
            }
        });
        return editFindNextMI;
    }

    protected JMenuItem createEditFindMI() {
        final JMenuItem editFindMI = new JMenuItem("Find");
        editFindMI.setMnemonic('f');
        editFindMI.setAccelerator(KeyStroke.getKeyStroke("control F"));

        editFindMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final String inputValue =
                                JOptionPane.showInputDialog(
                                        _logMonitorFrame,
                                        "Find text: ",
                                        "Search Record Messages",
                                        JOptionPane.QUESTION_MESSAGE
                                );
                        setSearchText(inputValue);
                        findSearchText();
                    }
                }

        );
        return editFindMI;
    }

    protected JMenuItem createEditFilterTIDMI() {
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

    protected JMenuItem createEditFilterBySubjectMI() {
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

    protected JMenuItem createEditFilterBySelectedTIDMI() {
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

    protected JMenuItem createEditFilterBySelectedSubjectMI() {
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

    protected void setTIDTextFilter(final String text) {
        // if no value is set, set it to a blank string
        // otherwise use the value provided
        if (text == null) {
            _trackingIDTextFilter = "";
        } else {
            _trackingIDTextFilter = text;
        }
    }

    protected void setSubjectTextFilter(final String text) {
        // if no value is set, set it to a blank string
        // otherwise use the value provided
        if (text == null) {
            _subjectTextFilter = "";
        } else {
            _subjectTextFilter = text;
        }
    }

    protected void filterByTID() {
        final String text = _trackingIDTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        _table.getFilteredLogTableModel().
                setLogRecordFilter(createTIDLogRecordFilter(text));
        _statusLabel.setText("Filtered by tracking id " + text);
    }

    protected void filterBySubject() {
        final String text = _subjectTextFilter;
        if (text == null || text.length() == 0) {
            return;
        }

        // Use new NDC filter
        _table.getFilteredLogTableModel().
                setLogRecordFilter(createSubjectLogRecordFilter(text));
        _statusLabel.setText("Filtered by subject  " + text);
    }

    protected LogRecordFilter createTIDLogRecordFilter(final String text) {
        _trackingIDTextFilter = text;
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final LogRecord record) {
                final String trackingID = record.getTrackingID();

                if (trackingID == null || _trackingIDTextFilter == null) {
                    return false;
                } else if (trackingID.indexOf(_trackingIDTextFilter) == -1) {
                    return false;
                } else {
                    final CategoryPath path = new CategoryPath(record.getSubject());
                    return getMenuItem(record.getType()).isSelected() &&
                            _subjectExplorerTree.getExplorerModel().isCategoryPathActive(path);
                }
            }
        };

        return result;
    }

    protected LogRecordFilter createSubjectLogRecordFilter(final String text) {
        _trackingIDTextFilter = text;
        final LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(final LogRecord record) {
                final String subject = record.getSubject();
                if (subject == null || _subjectTextFilter == null) {
                    return false;
                } else if (subject.indexOf(_subjectTextFilter) == -1) {
                    return false;
                } else {
                    final CategoryPath path = new CategoryPath(subject);
                    return getMenuItem(record.getType()).isSelected() &&
                            _subjectExplorerTree.getExplorerModel().isCategoryPathActive(path);
                }
            }
        };

        return result;
    }

    protected JMenuItem createEditRemoveAllFiltersTIDMI() {
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


    protected JToolBar createToolBar() {
        final JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        final JComboBox fontCombo = new JComboBox();
        final JComboBox fontSizeCombo = new JComboBox();
        _fontSizeCombo = fontSizeCombo;
        _fontNameCombo = fontCombo;

        _cl = this.getClass().getClassLoader();
        if (_cl == null) {
            _cl = ClassLoader.getSystemClassLoader();
        }

        final JButton listenerButton = new JButton("Add Listener");

        if (Icons.NEW_LISTENER != null)
            listenerButton.setIcon(Icons.NEW_LISTENER);
        
        listenerButton.setToolTipText("Create new Rv Listener.");

        listenerButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        requestNewRvListener(null);
                    }
                }
        );


        final JButton newButton = new JButton("Clear Log Table");

        if (Icons.DELETE != null)
            newButton.setIcon(Icons.DELETE);

        newButton.setToolTipText("Clear Log Table.");

        newButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        _table.clearLogRecords();
                        _subjectExplorerTree.getExplorerModel().resetAllNodeCounts();
                        updateStatusLabel();
                        clearDetails();
                        LogRecord.resetSequenceId();
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


        final String[] fonts;

        fonts = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (int j = 0; j < fonts.length; ++j) {
            fontCombo.addItem(fonts[j]);
        }

        fontCombo.setSelectedItem(getFontName());

        fontCombo.addActionListener(

                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final JComboBox box = (JComboBox) e.getSource();
                        final String font = (String) box.getSelectedItem();

                        setFontName(font);
                    }
                }
        );

        fontSizeCombo.addItem("8");
        fontSizeCombo.addItem("9");
        fontSizeCombo.addItem("10");
        fontSizeCombo.addItem("12");
        fontSizeCombo.addItem("14");
        fontSizeCombo.addItem("16");
        fontSizeCombo.addItem("18");
        fontSizeCombo.addItem("24");

        fontSizeCombo.setSelectedItem(String.valueOf(getFontSize()));
        fontSizeCombo.addActionListener(
                new ActionListener() {
                    public void actionPerformed(final ActionEvent e) {
                        final JComboBox box = (JComboBox) e.getSource();
                        final String size = (String) box.getSelectedItem();
                        final int s = Integer.valueOf(size).intValue();

                        setFontSizeSilently(s);
                        refreshDetails();
                        setFontSize(s);

                    }
                }
        );

        tb.add(new JLabel(" Font: "));
        tb.add(fontCombo);
        tb.add(fontSizeCombo);
        tb.addSeparator();
        tb.addSeparator();
        tb.add(listenerButton);
        tb.addSeparator();
        tb.add(newButton);
        tb.addSeparator();
        tb.add(_pauseButton);

        newButton.setAlignmentY(0.5f);
        newButton.setAlignmentX(0.5f);

        fontCombo.setMaximumSize(fontCombo.getPreferredSize());
        fontSizeCombo.setMaximumSize(
                fontSizeCombo.getPreferredSize());

        return (tb);
    }

    protected void setView(final String viewString, final LogTable table) {
        if (DETAILED_VIEW.equals(viewString)) {
            table.setDetailedView();
        } else {
            final String message = viewString + "does not match a supported view.";
            throw new IllegalArgumentException(message);
        }
        _currentView = viewString;
    }

    protected JComboBox createLogLevelCombo() {
        final JComboBox result = new JComboBox();
        final Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            result.addItem(levels.next());
        }
        result.setSelectedItem(_leastSevereDisplayedMsgType);

        result.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final JComboBox box = (JComboBox) e.getSource();
                final MsgType level = (MsgType) box.getSelectedItem();
                setLeastSevereDisplayedLogLevel(level);
            }
        });
        result.setMaximumSize(result.getPreferredSize());
        return result;
    }

    protected void setLeastSevereDisplayedLogLevel(final MsgType level) {
        if (level == null || _leastSevereDisplayedMsgType == level) {
            return; // nothing to do
        }
        _leastSevereDisplayedMsgType = level;
        _table.getFilteredLogTableModel().refresh();
        updateStatusLabel();
    }


    protected static void centerFrame(final JFrame frame) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension comp = frame.getSize();

        frame.setLocation(((screen.width - comp.width) / 2),
                ((screen.height - comp.height) / 2));

    }

    Rectangle getWindowBounds() {
        return this.getBaseFrame().getBounds();

    }

    void setWindowBounds(final Rectangle r) {
        this.getBaseFrame().setBounds(r);
    }


    /**
     * Uses a Dialog box to accept a URL to a file to be opened
     * with the  GUI.
     */
    protected void requestNewRvListener(final RvParameters p) {

        try {

            RvSnooperRvTransportInputDialog inputDialog = null;
            if (p != null) {
                inputDialog = new RvSnooperRvTransportInputDialog(
                        getBaseFrame(), p);
            } else {
                inputDialog = new RvSnooperRvTransportInputDialog(
                        getBaseFrame(), _lastUsedRvParameters);
            }


            if (inputDialog.isOK()) {
                _lastUsedRvParameters = inputDialog.getRvParameters();

                _lastUsedRvParameters.setDescription("<a href=\"" + URL + "\">" + VERSION + "</a> ");
                RvController.startRvListener(_lastUsedRvParameters, this);
                updateBanner();

                //_mruListnerManager.set(_lastUsedRvParameters);
                //updateMRUList();
            }

        } catch (TibrvException ex) {
            new RvSnooperErrorDialog(
                    getBaseFrame(), "Error creating listener : " + ex.getMessage());
        }

    }

    /**
     * Removes old file list and creates a new file list
     * with the updated MRU list.
     */
    protected void updateMRUList() {
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

    protected void requestCloseListener() {
        updateBanner();
    }

    protected void requestClose() {
        setCallSystemExitOnClose(true);
        closeAfterConfirm();
    }

    /**
     * Start a listener from the recent listeners list.
     * @param params
     */
    protected void requestOpenMRU(RvParameters params) {
        if (RecentListeners.getInstance().promote(params))
            try {
                RvController.startRvListener(params, this);
            } catch (TibrvException e) {
                RvUtils.showTibrvException(_logMonitorFrame,
                    "Unable to start listener.", e);
            }
        updateMRUList();
    }

    protected void requestExit() {
        try {
            RecentListeners.getInstance().store();
        } catch (IOException ignored) {
            // We can live with not saving the recent listeners list.
        }
        setCallSystemExitOnClose(true);
        closeAfterConfirm();
    }

    protected void closeAfterConfirm() {
        final StringBuffer message = new StringBuffer();

        message.append("Are you sure you want to exit?\n");

        final String title = "Are you sure you want to exit?";

        final int value = JOptionPane.showConfirmDialog(
                _logMonitorFrame,
                message.toString(),
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null
        );

        if (value == JOptionPane.OK_OPTION) {
            dispose();
        }
    }

    protected Iterator getMsgTypes() {
        return _levels.iterator();
    }

    protected Iterator getLogTableColumns() {
        return _columns.iterator();
    }

    Iterator getSubscriptions(){
        return RvController.getTransports().iterator();
    }

    /**
     * Loads and parses a log file.
     */
    protected static boolean loadLogFile(final File file) {
        final boolean ok;

        /*LogFileParser lfp = new LogFileParser(file);
        lfp.parse(this);*/
        ok = true;


        return ok;
    }

    /**
     * Loads a parses a log file running on a server.
     */
    protected static boolean loadLogFile(final URL url) {
        final boolean ok;

        /*LogFileParser lfp = new LogFileParser(url.openStream());
        lfp.parse(this);*/
        ok = true;

        return ok;
    }

    int getFontSize() {
        return _fontSize;
    }

    String getFontName() {
        return _fontName;
    }

    void setFontName(final String fontName) {
        this._fontName = fontName;
        _table.setFont(new Font(fontName, Font.PLAIN, getFontSize()));
        changeFontNameCombo(_fontNameCombo, fontName);
    }


    protected void setSplitPaneVertical(final JSplitPane _splitPaneVertical) {
        this._splitPaneVertical = _splitPaneVertical;
    }

    int getSplitPaneVerticalPos() {
        return _splitPaneVertical.getDividerLocation();
    }

    void setSplitPaneVerticalPos(final int location) {
        _splitPaneVertical.setDividerLocation(location);
    }

    int getSplitPaneTableViewerPos() {
        return _splitPaneTableViewer.getDividerLocation();
    }


    void setSplitPaneTableViewer(final JSplitPane _splitPaneTableViewer) {
        this._splitPaneTableViewer = _splitPaneTableViewer;
    }


    void setSplitPaneTableViewerPos(final int location) {
        this._splitPaneTableViewer.setDividerLocation(location);
    }

    class LogBrokerMonitorWindowAdaptor extends WindowAdapter {
        protected RvSnooperGUI _monitor;

        public LogBrokerMonitorWindowAdaptor(final RvSnooperGUI monitor) {
            _monitor = monitor;
        }

        public void windowClosing(final WindowEvent ev) {
            _monitor.requestClose();
        }
    }

    private class AddLogRecordRunnable implements Runnable {
        private final LogRecord lr;

        public AddLogRecordRunnable(LogRecord lr) {
            this.lr = lr;
        }

        public void run() {
            _subjectExplorerTree.getExplorerModel().addLogRecord(lr);
            _table.getFilteredLogTableModel().addLogRecord(lr); // update table
            updateStatusLabel(); // show updated counts
        }
    }
}
