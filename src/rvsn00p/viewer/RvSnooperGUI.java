/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import com.tibco.tibrv.*;
import rvsn00p.LogRecord;
import rvsn00p.LogRecordFilter;
import rvsn00p.MsgType;
import rvsn00p.sender.MsgSender;
import rvsn00p.util.DateFormatManager;
import rvsn00p.util.rv.RvController;
import rvsn00p.util.rv.RvParameters;
import rvsn00p.util.rv.MarshalRvToString;
import rvsn00p.util.rv.RvScriptInfo;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerTree;
import rvsn00p.viewer.categoryexplorer.CategoryPath;
import rvsn00p.viewer.configure.ConfigurationManager;
import rvsn00p.viewer.configure.MRUListnerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

import rvsn00p.util.BrowserLauncher;
import rvsn00p.sender.MsgSender;

/**
 *
 * RvSnooperGUI
 * @author �rjan Lundberg
 *
 * Based on Logfactor5 By
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brad Marlborough
 * @author Richard Wan
 * @author Brent Sprecher
 * @author Richard Hurst
 *
 */
public class RvSnooperGUI implements TibrvMsgCallback {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    public static final String DETAILED_VIEW = "Detailed";
    public static final String VERSION = "RvSn00p v1.2.0";
    public static final String URL = "http://rvsn00p.sf.net";



    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
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
    protected JButton _pauseButton = null;

    protected int _fontSize = 10;
    protected String _fontName = "Dialog";
    protected String _currentView = DETAILED_VIEW;

    protected boolean _loadSystemFonts = false;
    protected boolean _trackTableScrollPane = true;
    protected boolean _callSystemExitOnClose = true;
    protected List _displayedLogBrokerProperties = new Vector();

    protected Map _logLevelMenuItems = new HashMap();
    protected Map _logTableColumnMenuItems = new HashMap();

    protected List _levels = null;
    protected List _columns = null;
    protected boolean _isDisposed = false;

    protected ConfigurationManager _configurationManager = null;
    protected MRUListnerManager _mruListnerManager = null;

    protected boolean _displaySystemMsgs = true;
    protected boolean _displayIMMsgs = true;
    protected RvParameters _lastUsedRvParameters = new RvParameters();
    protected boolean _isPaused = false;
    protected ClassLoader _cl = null;

    protected static String _trackingIDTextFilter = "";
    protected static boolean useMtrackingInfo = true;




    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    /**
     * Construct a RvSnooperGUI.
     */
    public RvSnooperGUI(List MsgTypes, Set listeners, String name) {

        _levels = MsgTypes;
        _columns = LogTableColumn.getLogTableColumns();
        _columns = LogTableColumn.getLogTableColumns();
        _name = name;


        initComponents();

        _logMonitorFrame.addWindowListener(
                new LogBrokerMonitorWindowAdaptor(this));

        initTibco();
        startListeners(listeners);

    }


    protected void startListeners(Set listeners) {

        Iterator itrl = listeners.iterator();

        while (itrl.hasNext()) {
            try {

                RvParameters p = (RvParameters) itrl.next();

                p.setDescription(" <a href=\"" + URL + "\">" + VERSION + "</a> ");

                RvController.startRvListener(p, this);

                _lastUsedRvParameters = p;

            } catch (ClassCastException ex) {
                RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                        getBaseFrame(), ex.getMessage());
            } catch (TibrvException ex) {
                RvSnooperErrorDialog error;
                error = new RvSnooperErrorDialog(
                        getBaseFrame(), "Tibco Listener " + ex.getMessage());

            }
        }

        updateBanner();
    }

    protected void initTibco() {
        try {
            RvController.open();
        } catch (TibrvException e) {
            String s = "Failed to open Tibrv : ";
            s += e.getMessage();
            s += "\nCheck that the \"tibrv\\bin\" (win) and  \"tibrv\\lib\" \n ";
            s += "is found within you PATH (win) or LD_LIBRARY_PATH (unix/linux) ";

            System.err.println(s);
            RvSnooperErrorDialog error;
            error = new RvSnooperErrorDialog(
                    getBaseFrame(), s);
            System.exit(1);
        }
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------


    /**
     * Show the frame for the RvSnooperGUI. Dispatched to the
     * swing thread.
     */
    public void onMsg(TibrvListener listener, TibrvMsg msg) {

        if (isPaused()) {
            return;
        }
        if (filterRvMsg(msg) == false) {
            return;
        }

        LogRecord r = LogRecord.getInstance();

        String name;
        name = msg.getSendSubject();
        try {
            // try to extract the two last nameparts
            int ix = name.lastIndexOf(".");
            int ix2 = name.substring(0,ix).lastIndexOf(".");
            name = name.substring(ix2+1,name.length());
        } catch (Exception e) {
        }


        try {
            r.setMessage(MarshalRvToString.rvmsgToString(msg, name) );
        }  catch (Exception ex) {
            ex.printStackTrace();
            _statusLabel.setText(ex.getMessage() );
            try {
                // sometimes rvscript generates strange error messages such as
                //   KeyError: INVALID
                //   Traceback (innermost last):
                //   File "/home/stefan/tprojects/rvscript/work/tibrvXmlConvert.py", line 0, in rvmsgToXml
                // try to use the msg.toString instead of an error dialog
                r.setMessage(msg.toString() );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            TibrvMsgField f = null;
            f = msg.getField("^tracking^");
            if (f != null) {
                TibrvMsg tid = (TibrvMsg) f.data;
                r.setTrackingID((String) tid.get("^id^"));
            }
        } catch (TibrvException e) {
        }

        r.setSendSubject(msg.getSendSubject());
        r.setReplySubject(msg.getReplySubject());

        String sMsg = msg.getSendSubject();

        if (sMsg.startsWith("_")) {
            r.setType(MsgType.SYSTEM);
        }
        // todo add more message types
        // enable the user to define their own types?

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
            }
        });
    }

    public void show() {
        show(0);
        updateBanner();
    }

    public void updateBanner() {
        String sBanner;

        if(_name != null ){
          sBanner = _name;
          sBanner += " ";
          sBanner += RvController.getTransports().toString();
        } else {
          sBanner = RvController.getTransports().toString();
        }

        sBanner += " ";
        sBanner += VERSION;

        this.setTitle(sBanner );

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
    public DateFormatManager getDateFormatManager() {
        return _table.getDateFormatManager();
    }

    /**
     * Set the date format manager for formatting dates.
     */
    public void setDateFormatManager(DateFormatManager dfm) {
        _table.setDateFormatManager(dfm);
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
    public void setCallSystemExitOnClose(boolean callSystemExitOnClose) {
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

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _subjectExplorerTree.getExplorerModel().addLogRecord(lr);
                _table.getFilteredLogTableModel().addLogRecord(lr); // update table
                updateStatusLabel(); // show updated counts
            }
        });
    }

    public void setMaxNumberOfLogRecords(int maxNumberOfLogRecords) {
        _table.getFilteredLogTableModel().setMaxNumberOfLogRecords(maxNumberOfLogRecords);
    }

    public JFrame getBaseFrame() {
        return _logMonitorFrame;
    }

    public void setTitle(String title) {
        _logMonitorFrame.setTitle(title);
    }

    public void setFrameSize(int width, int height) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if (0 < width && width < screen.width) {
            _logMonitorFrameWidth = width;
        }
        if (0 < height && height < screen.height) {
            _logMonitorFrameHeight = height;
        }
        updateFrameSize();
    }

    public void setFontSize(int fontSize) {
        changeFontSizeCombo(_fontSizeCombo, fontSize);
        // setFontSizeSilently(actualFontSize); - changeFontSizeCombo fires event
        // refreshDetailTextArea();
    }

    public void addDisplayedProperty(Object messageLine) {
        _displayedLogBrokerProperties.add(messageLine);
    }

    public Map getLogLevelMenuItems() {
        return _logLevelMenuItems;
    }

    public Map getLogTableColumnMenuItems() {
        return _logTableColumnMenuItems;
    }

    public JCheckBoxMenuItem getTableColumnMenuItem(LogTableColumn column) {
        return getLogTableColumnMenuItem(column);
    }

    public CategoryExplorerTree getCategoryExplorerTree() {
        return _subjectExplorerTree;
    }




    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    protected boolean isPaused() {
        return _isPaused;
    }


    protected void pauseListeners() {
        _pauseButton.setText("Continue all listeners");
        _pauseButton.setToolTipText("Unpause listeners");
        _statusLabel.setText("All listeners are now paused");


        ImageIcon pbIcon = null;
        URL pbIconURL = _cl.getResource("rvsn00p/viewer/images/restart.gif");

        if (pbIconURL != null) {
            pbIcon = new ImageIcon(pbIconURL);
        }

        if (pbIcon != null) {
            _pauseButton.setIcon(pbIcon);
        }

        _isPaused = true;
    }

    protected void unPauseListeners() {
        _pauseButton.setText("Pause all listeners");
        _pauseButton.setToolTipText("Put listeners on hold");
        _statusLabel.setText("All listeners are now active");

        ImageIcon pbIcon = null;
        URL pbIconURL = _cl.getResource("rvsn00p/viewer/images/pauseon.gif");

        if (pbIconURL != null) {
            pbIcon = new ImageIcon(pbIconURL);
        }

        if (pbIcon != null) {
            _pauseButton.setIcon(pbIcon);
        }
        _isPaused = false;
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


    protected void setSearchText(String text) {
        _searchText = text;
    }


    protected void findSearchText() {
        String text = _searchText;
        if (text == null || text.length() == 0) {
            return;
        }
        int startRow = getFirstSelectedRow();
        int foundRow = findRecord(
                startRow,
                text,
                _table.getFilteredLogTableModel().getFilteredRecords()
        );
        selectRow(foundRow);
    }

    protected int getFirstSelectedRow() {
        return _table.getSelectionModel().getMinSelectionIndex();
    }

    protected void selectRow(int foundRow) {
        if (foundRow == -1) {
            String message = _searchText + " not found.";
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
            String searchText,
            List records
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
    protected static boolean matches(LogRecord record, String text) {
        String message = record.getMessage();

        if (message == null && _trackingIDTextFilter == null || text == null) {
            return false;
        }
        if (message.toLowerCase().indexOf(text.toLowerCase()) == -1 &&
           _trackingIDTextFilter.indexOf(text.toLowerCase()) == -1) {
            return false;
        }

        return true;
    }

    /**
     * When the fontsize of a JTextArea is changed, the word-wrapped lines
     * may become garbled.  This method clears and resets the text of the
     * text area.
     */
    protected static void refresh(JTextArea textArea) {
        String text = textArea.getText();
        textArea.setText("");
        textArea.setText(text);
    }

    protected void refreshDetailTextArea() {
        refresh(_table._detailTextArea);
    }

    protected void clearDetailTextArea() {
        _table._detailTextArea.setText("");
    }

    /**
     * Changes the font selection in the combo box and returns the
     * size actually selected.
     * @return -1 if unable to select an appropriate font
     */
    protected static int changeFontSizeCombo(JComboBox box, int requestedSize) {
        int len = box.getItemCount();
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
     * Does not update gui or cause any events to be fired.
     */
    protected void setFontSizeSilently(int fontSize) {
        _fontSize = fontSize;
        setFontSize(_table._detailTextArea, fontSize);
        selectRow(0);
        setFontSize(_table, fontSize);
    }

    protected static void setFontSize(Component component, int fontSize) {
        Font oldFont = component.getFont();
        Font newFont =
                new Font(oldFont.getFontName(), oldFont.getStyle(), fontSize);
        component.setFont(newFont);
    }

    protected void updateFrameSize() {
        _logMonitorFrame.setSize(_logMonitorFrameWidth, _logMonitorFrameHeight);
        centerFrame(_logMonitorFrame);
    }

    protected static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    protected void initComponents() {
        //
        // Configure the Frame.
        //
        _logMonitorFrame = new JFrame(_name);

        _logMonitorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        String resource =
                "/rvsn00p/viewer/images/eye.gif";
        URL iconURL = getClass().getResource(resource);

        if (iconURL != null) {
            _logMonitorFrame.setIconImage(new ImageIcon(iconURL).getImage());
        }
        updateFrameSize();

        //
        // Configure the LogTable.
        //
        JTextArea detailTA = createDetailTextArea();
        JScrollPane detailTAScrollPane = new JScrollPane(detailTA);
        _table = new LogTable(detailTA);
        setView(_currentView, _table);
        _table.setFont(new Font(_fontName, Font.PLAIN, _fontSize));
        _logTableScrollPane = new JScrollPane(_table);

        if (_trackTableScrollPane) {
            _logTableScrollPane.getVerticalScrollBar().addAdjustmentListener(
                    new TrackingAdjustmentListener()
            );
        }


        // Configure the SplitPane between the LogTable & DetailTextArea
        //

        JSplitPane tableViewerSplitPane = new JSplitPane();
        tableViewerSplitPane.setOneTouchExpandable(true);
        tableViewerSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        tableViewerSplitPane.setLeftComponent(_logTableScrollPane);
        tableViewerSplitPane.setRightComponent(detailTAScrollPane);
        // Make sure to do this last..
        //tableViewerSplitPane.setDividerLocation(1.0); Doesn't work
        //the same under 1.2.x & 1.3
        // "350" is a magic number that provides the correct default
        // behaviour under 1.2.x & 1.3.  For example, bumping this
        // number to 400, causes the pane to be completely open in 1.2.x
        // and closed in 1.3
        tableViewerSplitPane.setDividerLocation(350);

        //
        // Configure the CategoryExplorer
        //

        _subjectExplorerTree = new CategoryExplorerTree();

        _table.getFilteredLogTableModel().setLogRecordFilter(createLogRecordFilter());

        JScrollPane categoryExplorerTreeScrollPane =
                new JScrollPane(_subjectExplorerTree);
        categoryExplorerTreeScrollPane.setPreferredSize(new Dimension(130, 400));

        // Load most recently used file list
        //_mruListnerManager = new MRUListnerManager();

        //
        // Configure the SplitPane between the CategoryExplorer & (LogTable/Detail)
        //

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setRightComponent(tableViewerSplitPane);
        splitPane.setLeftComponent(categoryExplorerTreeScrollPane);
        // Do this last.
        splitPane.setDividerLocation(130);
        //
        // Add the MenuBar, StatusArea, CategoryExplorer|LogTable to the
        // LogMonitorFrame.
        //
        _logMonitorFrame.getRootPane().setJMenuBar(createMenuBar());
        _logMonitorFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
        _logMonitorFrame.getContentPane().add(createToolBar(),
                BorderLayout.NORTH);
        _logMonitorFrame.getContentPane().add(createStatusArea(),
                BorderLayout.SOUTH);

        makeLogTableListenToCategoryExplorer();
        addTableModelProperties();

        //
        // Configure ConfigurationManager
        //
        _configurationManager = new ConfigurationManager(this, _table);

        unPauseListeners();

    }

    protected LogRecordFilter createLogRecordFilter() {
        LogRecordFilter result = new LogRecordFilter() {
            public boolean passes(LogRecord record) {
                CategoryPath path = new CategoryPath(record.getSubject());
                return
                        getMenuItem(record.getType()).isSelected() &&
                        _subjectExplorerTree.getExplorerModel().isCategoryPathActive(path);
            }
        };
        return result;
    }


    protected void updateStatusLabel() {
        _statusLabel.setText(getRecordsDisplayedMessage() + getFileEncodingMessage());
    }

    protected String getRecordsDisplayedMessage() {
        FilteredLogTableModel model = _table.getFilteredLogTableModel();
        return getStatusText(model.getRowCount(), model.getTotalRowCount());
    }

    protected String getFileEncodingMessage() {
        return "  Encoding:" + System.getProperty("file.encoding");
    }

    protected void addTableModelProperties() {
        final FilteredLogTableModel model = _table.getFilteredLogTableModel();

        addDisplayedProperty(new Object() {
            public String toString() {
                return getRecordsDisplayedMessage();
            }
        });
        addDisplayedProperty(new Object() {
            public String toString() {
                return "Maximum number of displayed LogRecords: "
                        + model._maxNumberOfLogRecords;
            }
        });
    }

    protected static String getStatusText(int displayedRows, int totalRows) {
        StringBuffer result = new StringBuffer();
        result.append("Displaying: ");
        result.append(displayedRows);
        result.append(" records out of a total of: ");
        result.append(totalRows);
        result.append(" records.");
        return result.toString();
    }

    protected void makeLogTableListenToCategoryExplorer() {
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        };
        _subjectExplorerTree.getExplorerModel().addActionListener(listener);
    }

    protected JPanel createStatusArea() {
        JPanel statusArea = new JPanel();
        JLabel status =
                new JLabel("No log records to display.");
        _statusLabel = status;
        status.setHorizontalAlignment(JLabel.LEFT);

        statusArea.setBorder(BorderFactory.createEtchedBorder());
        statusArea.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusArea.add(status);

        return (statusArea);
    }

    protected static JTextArea createDetailTextArea() {
        JTextArea detailTA = new JTextArea();
        detailTA.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailTA.setTabSize(3);
        detailTA.setLineWrap(true);
        detailTA.setWrapStyleWord(false);
        return (detailTA);
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());

        //menuBar.add(createMessageMenu());

        menuBar.add(createMsgTypeMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createConfigureMenu());
        menuBar.add(createHelpMenu());

        return (menuBar);
    }

    protected JMenu createMessageMenu() {
        JMenu result = new JMenu("Message");
        result.setMnemonic('m');
        result.add(createResendMenuItem());

        return result;
    }

    protected JMenuItem createSaveSelectedAsRvscriptMenuItem() {
        JMenuItem result = new JMenuItem("Save selected as rvscript");
        result.setMnemonic('r');

        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                ListSelectionModel lsm = _table.getSelectionModel();
                if (lsm.isSelectionEmpty()) {
                    _statusLabel.setText("No rows are selected.");
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();

                    FilteredLogTableModel ftm;
                    ftm = _table.getFilteredLogTableModel();

                    final String sMsg = (String) _table.getModel().getValueAt(selectedRow, _table.getMsgColumnID());
                    final String sSubject = (String) _table.getModel().getValueAt(selectedRow,
                            _table.getSubjectColumnID());

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            RvSnooperFileHandler.saveMsgAsRvScript(sSubject, sMsg, VERSION + " " + URL, getBaseFrame(),
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

    protected JMenuItem createResendMenuItem() {
        JMenuItem result = new JMenuItem("Resend selected");
        result.setMnemonic('r');


        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                ListSelectionModel lsm = _table.getSelectionModel();

                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();

                    FilteredLogTableModel ftm;
                    ftm = _table.getFilteredLogTableModel();


                    final String sMsg = (String) _table.getModel().getValueAt(selectedRow, _table.getMsgColumnID());
                    final String sSubject = (String) _table.getModel().getValueAt(selectedRow,
                            _table.getSubjectColumnID());

                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            MsgSender s = new MsgSender(_logMonitorFrame, VERSION, sSubject, sMsg,
                                    _lastUsedRvParameters.getDaemon(), _lastUsedRvParameters.getService(),
                                    _lastUsedRvParameters.getNetwork());
                            s.show();
                        }
                    });

                }
            }
        });
        return result;
    }

    protected JMenu createMsgTypeMenu() {
        JMenu result = new JMenu("Msg Type");
        result.setMnemonic('t');
        Iterator imsgtypes = getMsgTypes();
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
        JMenuItem result = new JMenuItem("Show all Msg Types");
        result.setMnemonic('a');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllMsgTypes(true);
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    protected JMenuItem createNoMsgTypesMenuItem() {
        JMenuItem result = new JMenuItem("Hide all MsgTypes");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllMsgTypes(false);
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    protected JMenu createLogLevelColorMenu() {
        JMenu colorMenu = new JMenu("Configure MsgType Colors");
        colorMenu.setMnemonic('c');
        Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            colorMenu.add(createSubMenuItem((MsgType) levels.next()));
        }

        return colorMenu;
    }

    protected JMenuItem createResetLogLevelColorMenuItem() {
        JMenuItem result = new JMenuItem("Reset MsgType Colors");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // reset the level colors in the map
                MsgType.resetLogLevelColorMap();

                // refresh the table
                _table.getFilteredLogTableModel().refresh();
            }
        });
        return result;
    }

    protected void selectAllMsgTypes(boolean selected) {
        Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            getMenuItem((MsgType) levels.next()).setSelected(selected);
        }
    }

    protected JCheckBoxMenuItem getMenuItem(MsgType level) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logLevelMenuItems.get(level));
        if (result == null) {
            result = createMenuItem(level);
            _logLevelMenuItems.put(level, result);
        }
        return result;
    }

    protected JMenuItem createSubMenuItem(MsgType level) {
        final JMenuItem result = new JMenuItem(level.toString());
        final MsgType logLevel = level;
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLogLevelColorChangeDialog(result, logLevel);
            }
        });

        return result;

    }

    protected void showLogLevelColorChangeDialog(JMenuItem result, MsgType level) {
        JMenuItem menuItem = result;
        Color newColor = JColorChooser.showDialog(
                _logMonitorFrame,
                "Choose MsgType Color",
                result.getForeground());

        if (newColor != null) {
            // set the color for the record
            level.setLogLevelColorMap(level, newColor);
            _table.getFilteredLogTableModel().refresh();
        }

    }

    protected JCheckBoxMenuItem createMenuItem(MsgType level) {
        JCheckBoxMenuItem result = new JCheckBoxMenuItem(level.toString());
        result.setSelected(true);
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _table.getFilteredLogTableModel().refresh();
                updateStatusLabel();
            }
        });
        return result;
    }

    // view menu
    protected JMenu createViewMenu() {
        JMenu result = new JMenu("View");
        result.setMnemonic('v');
        Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            result.add(getLogTableColumnMenuItem((LogTableColumn) columns.next()));
        }

        result.addSeparator();
        result.add(createAllLogTableColumnsMenuItem());
        result.add(createNoLogTableColumnsMenuItem());
        return result;
    }

    protected JCheckBoxMenuItem getLogTableColumnMenuItem(LogTableColumn column) {
        JCheckBoxMenuItem result = (JCheckBoxMenuItem) (_logTableColumnMenuItems.get(column));
        if (result == null) {
            result = createLogTableColumnMenuItem(column);
            _logTableColumnMenuItems.put(column, result);
        }
        return result;
    }

    protected JCheckBoxMenuItem createLogTableColumnMenuItem(LogTableColumn column) {
        JCheckBoxMenuItem result = new JCheckBoxMenuItem(column.toString());

        result.setSelected(true);
        result.setMnemonic(column.toString().charAt(0));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // update list of columns and reset the view
                List selectedColumns = updateView();
                _table.setView(selectedColumns);
            }
        });
        return result;
    }

    protected List updateView() {
        ArrayList updatedList = new ArrayList();
        Iterator columnIterator = _columns.iterator();
        while (columnIterator.hasNext()) {
            LogTableColumn column = (LogTableColumn) columnIterator.next();
            JCheckBoxMenuItem result = getLogTableColumnMenuItem(column);
            // check and see if the checkbox is checked
            if (result.isSelected()) {
                updatedList.add(column);
            }
        }

        return updatedList;
    }

    protected JMenuItem createAllLogTableColumnsMenuItem() {
        JMenuItem result = new JMenuItem("Show all Columns");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllLogTableColumns(true);
                // update list of columns and reset the view
                List selectedColumns = updateView();
                _table.setView(selectedColumns);
            }
        });
        return result;
    }

    protected JMenuItem createNoLogTableColumnsMenuItem() {
        JMenuItem result = new JMenuItem("Hide all Columns");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectAllLogTableColumns(false);
                // update list of columns and reset the view
                final List selectedColumns = updateView();
                _table.setView(selectedColumns);
            }
        });
        return result;
    }

    protected void selectAllLogTableColumns(boolean selected) {
        Iterator columns = getLogTableColumns();
        while (columns.hasNext()) {
            getLogTableColumnMenuItem((LogTableColumn) columns.next()).setSelected(selected);
        }
    }

    protected JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        JMenuItem exitMI;
        fileMenu.add(createOpenMI());
        fileMenu.add(createSaveHTML());
        fileMenu.add(createSaveSelectedAsRvscriptMenuItem());
        fileMenu.add(createSaveRvScript());
        fileMenu.addSeparator();
        fileMenu.add(createCloseListener());
        //createMRUListnerListMI(fileMenu);
        fileMenu.addSeparator();
        fileMenu.add(createExitMI());
        return fileMenu;
    }


    /**
     * Menu item added to allow save of filtered table contents to html file
     *
     */
    protected JMenuItem createSaveHTML() {
        JMenuItem result = new JMenuItem("Save Table to HTML file");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RvSnooperFileHandler.saveTableToHtml(VERSION, URL, getBaseFrame(), _statusLabel, _table);
            }
        });
        return result;
    }

    /**
     * Menu item added to allow save of filtered table contents to rvscript file.
     *
     */
    protected JMenuItem createSaveRvScript() {
        JMenuItem result = new JMenuItem("Save Table to rvscript file");
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                RvSnooperFileHandler.saveTableToRvScript(VERSION + " " + URL, getBaseFrame(), _statusLabel, _table);
            }
        });

        if (!RvScriptInfo.isAvaliable()) {
            result.setEnabled(false);
        }
        return result;
    }


    /**
     * Menu item added to allow log files to be opened with
     * the  GUI.
     */
    protected JMenuItem createOpenMI() {
        JMenuItem result = new JMenuItem("New Listener...");
        result.setMnemonic('n');
        result.setAccelerator(KeyStroke.getKeyStroke("control N"));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }


    protected JMenuItem createSaveConfigMI() {
        JMenuItem result = new JMenuItem("Save Listeners to file");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }

    protected JMenuItem createOpenConfigMI() {
        JMenuItem result = new JMenuItem("Open Listeners from file");
        result.setMnemonic('o');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestNewRvListener(null);
            }
        });
        return result;
    }


    protected JMenuItem createCloseListener() {
        JMenuItem result = new JMenuItem("Close All Listeners");
        result.setMnemonic('c');
        result.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
    protected void createMRUListnerListMI(JMenu menu) {

        String[] parameters = _mruListnerManager.getMRUFileList();

        if (parameters != null) {
            menu.addSeparator();
            for (int i = 0; i < parameters.length; ++i) {

                JMenuItem result = new JMenuItem((i + 1) + " " + parameters[i]);
                result.setMnemonic(i + 1);
                result.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        requestOpenMRU(e);
                    }
                });
                menu.add(result);
            }
        }
    }

    protected JMenuItem createExitMI() {
        JMenuItem result = new JMenuItem("Exit");
        result.setMnemonic('x');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestExit();
            }
        });
        return result;
    }

    protected JMenu createConfigureMenu() {
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('c');
        configureMenu.add(createConfigureSave());
        configureMenu.add(createConfigureReset());
        configureMenu.add(createConfigureMaxRecords());

        return configureMenu;
    }

    protected JMenuItem createConfigureSave() {
        JMenuItem result = new JMenuItem("Save");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                saveConfiguration();
            }
        });

        return result;
    }

    protected JMenuItem createConfigureReset() {
        JMenuItem result = new JMenuItem("Reset");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetConfiguration();
            }
        });

        return result;
    }

    protected JMenuItem createConfigureMaxRecords() {

        JMenuItem result = new JMenuItem("Set Max Number of Records");
        result.setMnemonic('m');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setMaxRecordConfiguration();
            }
        });

        return result;
    }

    protected void saveConfiguration() {
        try {
            _configurationManager.save();
        } catch (Exception ex) {
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    getBaseFrame(),
                    ex.getMessage());
        }
    }

    protected void resetConfiguration() {
        _configurationManager.reset();
    }

    protected void setMaxRecordConfiguration() {
        RvSnooperInputDialog inputDialog = new RvSnooperInputDialog(
                getBaseFrame(), "Set Max Number of Records", "", 10);

        String temp = inputDialog.getText();

        if (temp != null) {
            try {
                setMaxNumberOfLogRecords(Integer.parseInt(temp));
            } catch (NumberFormatException e) {
                RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                        getBaseFrame(),
                        "'" + temp + "' is an invalid parameter.\nPlease try again.");
                setMaxRecordConfiguration();
            }
        }
    }


    protected JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        helpMenu.add(createHelpAbout());
        helpMenu.add(createHelpBugReport());
        helpMenu.add(createHelpDownload());
        helpMenu.add(createHelpGotoHomepage());
        helpMenu.add(createHelpSubscribe() );
        helpMenu.add(createHelpSupport() );
        helpMenu.add(createHelpProperties());
        helpMenu.add(createHelpLICENSE());
            StringBuffer a = new StringBuffer();
        a.toString();
        return helpMenu;
    }


    protected JMenuItem createHelpProperties() {
        final String title = "Show Properties";
        final JMenuItem result = new JMenuItem(title);
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPropertiesDialog(title);
            }
        });
        return result;
    }

    protected JMenuItem createHelpLICENSE() {
        final String title = "License information";
        final JMenuItem result = new JMenuItem(title);
        result.setMnemonic('l');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.apache.org/licenses/LICENSE");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }


    protected JMenuItem createHelpBugReport() {
        final String title = "Submit bug report or Feature Request";
        final JMenuItem result = new JMenuItem(title);
        result.setMnemonic('b');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://sf.net/tracker/?group_id=63447");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }

    protected JMenuItem createHelpDownload() {
        final String title = "Download latest version";
        final JMenuItem result = new JMenuItem(title);
        result.setMnemonic('b');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://sf.net/project/showfiles.php?group_id=63447");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }

    protected JMenuItem createHelpGotoHomepage() {
        final String title = "Help topics";
        final JMenuItem result = new JMenuItem(title);
        result.setMnemonic('t');
        result.setAccelerator(KeyStroke.getKeyStroke("F1"));
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://rvsn00p.sf.net");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }

        protected JMenuItem createHelpSubscribe() {
        final String title = "Subscribe to update messages";
        final JMenuItem result = new JMenuItem(title);
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://sourceforge.net/project/filemodule_monitor.php?filemodule_id=60335");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }

     protected JMenuItem createHelpSupport() {
        final String title = "My wish list - support me";
        final JMenuItem result = new JMenuItem(title);
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    BrowserLauncher.openURL("http://www.amazon.co.uk/exec/obidos/registry/wishlist/026-3142533-7083625");
                } catch (Exception ex) {
                    RvSnooperErrorDialog error;
                    error = new RvSnooperErrorDialog(
                            getBaseFrame(), "Could not open browser : " + ex.getMessage());
                }
            }
        });
        return result;
    }

    protected JMenuItem createHelpAbout() {
        final String title = "About Rendevous Sn00per";
        final JMenuItem result = new JMenuItem(title);
        result.setMnemonic('a');
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAboutDialog(title);
            }
        });
        return result;
    }

    protected void showPropertiesDialog(String title) {
        JOptionPane.showMessageDialog(
                _logMonitorFrame,
                _displayedLogBrokerProperties.toArray(),
                title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    protected void showAboutDialog(String title) {
        JOptionPane.showMessageDialog(
                _logMonitorFrame,
                new String[]{VERSION,
                             " ",
                             "Constructed by �rjan Lundberg <orjan.lundberg@netresult.se>",
                             " ",
                             "This product includes software developed by the Apache Software Foundation (http://www.apache.org/). ",
                             " ",
                             "Thanks goes to (in no special order):",
                             "Stephanie Lundberg",
                             "Thomas Bonderud",
                             "Anders Lindl�f",
                             "Stefan Axelsson",
                             "Linda Lundberg",
                             "Johan Hjort",
                             "Magnus L Johansson",
                             "Eric Albert (browserLauncher.sf.net)",
                             "Richard Valk",
                             " ",
                             "Based on Jakarta log4J LogFactor5 ",
                             " ",
                             "Copyright (C) The Apache Software Foundation. All rights reserved.",
                             " ",
                             "This software is published under the terms of the Apache Software",
                             "License version 1.1, a copy of which has been included with this",
                             "distribution in the LICENSE.txt file. ",
                             " "},
                title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    protected JMenu createEditMenu() {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        editMenu.add(createEditFindMI());
        editMenu.add(createEditFindNextMI());
        editMenu.addSeparator();
        editMenu.add(createEditFilterTIDMI());
        editMenu.add(createEditFilterBySelectedTIDMI() );
        editMenu.add(createEditRestoreAllTIDMI());
        return editMenu;
    }

    protected JMenuItem createEditFindNextMI() {
        JMenuItem editFindNextMI = new JMenuItem("Find Next");
        editFindNextMI.setMnemonic('n');
        editFindNextMI.setAccelerator(KeyStroke.getKeyStroke("F3"));
        editFindNextMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                findSearchText();
            }
        });
        return editFindNextMI;
    }

    protected JMenuItem createEditFindMI() {
        JMenuItem editFindMI = new JMenuItem("Find");
        editFindMI.setMnemonic('f');
        editFindMI.setAccelerator(KeyStroke.getKeyStroke("control F"));

        editFindMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String inputValue =
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
    JMenuItem editFilterNDCMI = new JMenuItem("Filter by tracking id");
    editFilterNDCMI.setMnemonic('t');
    editFilterNDCMI.setAccelerator(KeyStroke.getKeyStroke("control shift T"));
    editFilterNDCMI.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String inputValue =
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

  protected JMenuItem createEditFilterBySelectedTIDMI() {
      JMenuItem editFilterNDCMI = new JMenuItem("Filter by selected tracking id");
      editFilterNDCMI.setMnemonic('s');
      editFilterNDCMI.setAccelerator(KeyStroke.getKeyStroke("control T"));
      editFilterNDCMI.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {


              ListSelectionModel lsm = _table.getSelectionModel();

              if (lsm.isSelectionEmpty()) {
                  //no rows are selected
              } else {
                  int selectedRow = lsm.getMinSelectionIndex();

                  FilteredLogTableModel ftm;
                  ftm = _table.getFilteredLogTableModel();


                  final String sTID = (String) _table.getModel().getValueAt(selectedRow, _table.getTIDColumnID());
                  if( sTID != null )  {
                     setTIDTextFilter(sTID);
                     filterByTID();
                     _table.getFilteredLogTableModel().refresh();
                  }
              }

          }}
      );


      return editFilterNDCMI;
  }

   protected void setTIDTextFilter(String text) {
    // if no value is set, set it to a blank string
    // otherwise use the value provided
    if (text == null) {
      _trackingIDTextFilter = "";
    } else {
      _trackingIDTextFilter = text;
    }
  }
  protected void filterByTID() {
    String text = _trackingIDTextFilter;
    if (text == null || text.length() == 0) {
      return;
    }

    // Use new NDC filter
    _table.getFilteredLogTableModel().
        setLogRecordFilter(createTIDLogRecordFilter(text));
    _statusLabel.setText("Filtered by tracking id " + text );
  }
   protected LogRecordFilter createTIDLogRecordFilter(String text) {
    _trackingIDTextFilter = text;
    LogRecordFilter result = new LogRecordFilter() {
      public boolean passes(LogRecord record) {
        String trackingID = record.getTrackingID();
        CategoryPath path = new CategoryPath(record.getSubject() );
        if (trackingID == null || _trackingIDTextFilter == null) {
          return false;
        } else if (trackingID.indexOf(_trackingIDTextFilter) == -1) {
          return false;
        } else {
          return getMenuItem(record.getType()).isSelected() &&
              _subjectExplorerTree.getExplorerModel().isCategoryPathActive(path);
        }
      }
    };

    return result;
  }
  protected JMenuItem createEditRestoreAllTIDMI() {
    JMenuItem editRestoreAllNDCMI = new JMenuItem("Remove tracking id filter");
    editRestoreAllNDCMI.setMnemonic('r');
    editRestoreAllNDCMI.setAccelerator(KeyStroke.getKeyStroke("control R"));
    editRestoreAllNDCMI.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
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
        JToolBar tb = new JToolBar();
        tb.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        JComboBox fontCombo = new JComboBox();
        JComboBox fontSizeCombo = new JComboBox();
        _fontSizeCombo = fontSizeCombo;

        _cl = this.getClass().getClassLoader();
        if (_cl == null) {
            _cl = ClassLoader.getSystemClassLoader();
        }
        URL newIconURL = _cl.getResource("rvsn00p/viewer/images/channelexplorer_new.gif");

        ImageIcon newIcon = null;

        if (newIconURL != null) {
            newIcon = new ImageIcon(newIconURL);
        }

        JButton listenerButton = new JButton("Add Listener");

        if (newIcon != null) {
            listenerButton.setIcon(newIcon);
        }

        listenerButton.setToolTipText("Create new Rv Listener.");

        listenerButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        requestNewRvListener(null);
                    }
                }
        );


        JButton newButton = new JButton("Clear Log Table");


        URL tcIconURL = _cl.getResource("rvsn00p/viewer/images/trash.gif");

        ImageIcon tcIcon = null;

        if (tcIconURL != null) {
            tcIcon = new ImageIcon(tcIconURL);
        }

        if (newIcon != null) {
            newButton.setIcon(tcIcon);
        }

        newButton.setToolTipText("Clear Log Table.");

        newButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _table.clearLogRecords();
                        _subjectExplorerTree.getExplorerModel().resetAllNodeCounts();
                        updateStatusLabel();
                        clearDetailTextArea();
                        LogRecord.resetSequenceNumber();
                    }
                }
        );


        _pauseButton = new JButton("Pause all listeners");


        _pauseButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (isPaused()) {
                            unPauseListeners();
                        } else {
                            pauseListeners();
                        }
                    }
                }
        );


        Toolkit tk = Toolkit.getDefaultToolkit();
        // This will actually grab all the fonts

        String[] fonts;

        if (_loadSystemFonts) {
            fonts = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        } else {
            fonts = tk.getFontList();
        }

        for (int j = 0; j < fonts.length; ++j) {
            fontCombo.addItem(fonts[j]);
        }

        fontCombo.setSelectedItem(_fontName);

        fontCombo.addActionListener(

                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox box = (JComboBox) e.getSource();
                        String font = (String) box.getSelectedItem();
                        _table.setFont(new Font(font, Font.PLAIN, _fontSize));
                        _fontName = font;
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

        fontSizeCombo.setSelectedItem(String.valueOf(_fontSize));
        fontSizeCombo.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox box = (JComboBox) e.getSource();
                        String size = (String) box.getSelectedItem();
                        int s = Integer.valueOf(size).intValue();

                        setFontSizeSilently(s);
                        refreshDetailTextArea();
                        _fontSize = s;
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

    protected void setView(String viewString, LogTable table) {
        if (DETAILED_VIEW.equals(viewString)) {
            table.setDetailedView();
        } else {
            String message = viewString + "does not match a supported view.";
            throw new IllegalArgumentException(message);
        }
        _currentView = viewString;
    }

    protected JComboBox createLogLevelCombo() {
        JComboBox result = new JComboBox();
        Iterator levels = getMsgTypes();
        while (levels.hasNext()) {
            result.addItem(levels.next());
        }
        result.setSelectedItem(_leastSevereDisplayedMsgType);

        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                MsgType level = (MsgType) box.getSelectedItem();
                setLeastSevereDisplayedLogLevel(level);
            }
        });
        result.setMaximumSize(result.getPreferredSize());
        return result;
    }

    protected void setLeastSevereDisplayedLogLevel(MsgType level) {
        if (level == null || _leastSevereDisplayedMsgType == level) {
            return; // nothing to do
        }
        _leastSevereDisplayedMsgType = level;
        _table.getFilteredLogTableModel().refresh();
        updateStatusLabel();
    }


    protected static void centerFrame(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension comp = frame.getSize();

        frame.setLocation(((screen.width - comp.width) / 2),
                ((screen.height - comp.height) / 2));

    }

    /**
     * Uses a Dialog box to accept a URL to a file to be opened
     * with the  GUI.
     */
    protected void requestNewRvListener(RvParameters p) {

        try {

            RvSnooperRvTransportInputDialog inputDialog = null;
            if (p != null) {
                inputDialog = new RvSnooperRvTransportInputDialog(
                        getBaseFrame(), "Add Rv Listener", p);
            } else {
                inputDialog = new RvSnooperRvTransportInputDialog(
                        getBaseFrame(), "Add Rv Listener", _lastUsedRvParameters);
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
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    getBaseFrame(), "Error creating listener : " + ex.getMessage());
        }

    }

    /**
     * Removes old file list and creates a new file list
     * with the updated MRU list.
     */
    protected void updateMRUList() {
        JMenu menu = _logMonitorFrame.getJMenuBar().getMenu(0);
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
     * Opens a file in the MRU list.
     */
    protected void requestOpenMRU(ActionEvent e) {

        //todo
        String file = e.getActionCommand();
        StringTokenizer st = new StringTokenizer(file);
        String num = st.nextToken().trim();
        file = st.nextToken("\n");

        try {
            int index = Integer.parseInt(num) - 1;

            InputStream in = _mruListnerManager.getInputStream(index);
            /*LogFileParser lfp = new LogFileParser(in);
            lfp.parse(this);*/

            _mruListnerManager.moveToTop(index);
            updateMRUList();

        } catch (Exception me) {
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    getBaseFrame(), "Unable to load file " + file);
        }

    }

    protected void requestExit() {
        //_mruListnerManager.save();
        setCallSystemExitOnClose(true);
        closeAfterConfirm();
    }

    protected void closeAfterConfirm() {
        StringBuffer message = new StringBuffer();

        message.append("Are you sure you want to exit?\n");

        String title = "Are you sure you want to exit?";

        int value = JOptionPane.showConfirmDialog(
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

    /**
     * Loads and parses a log file.
     */
    protected static boolean loadLogFile(File file) {
        boolean ok;

        /*LogFileParser lfp = new LogFileParser(file);
        lfp.parse(this);*/
        ok = true;


        return ok;
    }

    /**
     * Loads a parses a log file running on a server.
     */
    protected static boolean loadLogFile(URL url) {
        boolean ok;

        /*LogFileParser lfp = new LogFileParser(url.openStream());
        lfp.parse(this);*/
        ok = true;

        return ok;
    }
    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

    class LogBrokerMonitorWindowAdaptor extends WindowAdapter {
        protected RvSnooperGUI _monitor;

        public LogBrokerMonitorWindowAdaptor(RvSnooperGUI monitor) {
            _monitor = monitor;
        }

        public void windowClosing(WindowEvent ev) {
            _monitor.requestClose();
        }
    }
}

