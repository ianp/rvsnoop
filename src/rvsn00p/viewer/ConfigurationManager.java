/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import org.w3c.dom.*;
import rvsn00p.MsgType;
import rvsn00p.MsgTypeFormatException;
import rvsn00p.util.DateFormatManager;
import rvsn00p.util.rv.RvParameters;
import rvsn00p.viewer.LogTable;
import rvsn00p.viewer.LogTableColumn;
import rvsn00p.viewer.LogTableColumnFormatException;
import rvsn00p.viewer.RvSnooperGUI;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerModel;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerTree;
import rvsn00p.viewer.categoryexplorer.CategoryNode;
import rvsn00p.viewer.categoryexplorer.CategoryPath;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

/**
 * <p>ConfigurationManager handles the storage and retrival of the state of
 * the CategoryExplorer
 * @author orjan Lundberg
 *
 * Based on Logfactor5 By
 *
 * @author Richard Hurst
 * @author Brad Marlborough
 * Contributed by ThoughtWorks Inc.
 */


public class ConfigurationManager extends Object {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------
    private static final String CONFIG_FILE_NAME = "conf.xml";
    public static final String CONFIG_DIR_NAME = ".rvsnoop";
    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String SELECTED = "selected";
    private static final String COLUMWIDTH = "columnsize";
    private static final String EXPANDED = "expanded";
    private static final String CATEGORY = "subject";
    private static final String FIRST_CATEGORY_NAME = "Subjects";
    private static final String LEVEL = "type";
    private static final String COLORLEVEL = "colorlevel";
    private static final String COLOR = "color";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String BLUE = "blue";
    private static final String COLUMN = "column";
    private static final String SEP = System.getProperty("file.separator");
    private static final String FONTINFO = "fontinfo";
    private static final String FONTNAME = "name";
    private static final String FONTSIZE = "size";
    private static final String FONTSTYLE = "style";
    private static final String DATEFORMAT = "dateformat";
    private static final String DATEPATTERN = "pattern";
    private static final String SPLITPANEPOS = "splitpanepos";
    private static final String SPLITPANEPOSH = "horizontal";
    private static final String SPLITPANEPOSV = "vertical";
    private static final String WINDOWPOS = "windowpos";
    private static final String WINDOWX = "windowsx";
    private static final String WINDOWY = "windowy";
    private static final String WINDOWWIDTH = "windowwidth";
    private static final String WINDOWHEIGHT = "windowheight";
    private static final String LSNRSUBSCRIPTIONS = "SUBSCRIPTIONS";
    private static final String LSNRTIBLISTENER = "TIBLISTENER";
    private static final String LSNRTIBSERVICE = "SERVICE";
    private static final String LSNRTIBNETWORK = "NETWORK";
    private static final String LSNRTIBDAEMON = "DAEMON";
    private static final String LSNRTIBSUBJECT = "SUBJECT";
    private static final String LSNRTIBSUBJECTID = "ID";


    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private RvSnooperGUI _gui = null;
    private LogTable _table = null;
    private String _fileName;


    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    public ConfigurationManager(RvSnooperGUI gui, LogTable table) {
        super();
        _gui = gui;
        _table = table;
        _fileName = getDefaultFilename();
        load();
    }

    public ConfigurationManager(RvSnooperGUI gui, LogTable table, String fileName) {
        super();
        _gui = gui;
        _table = table;
        _fileName = fileName;
        load();
    }
    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    public void save() throws IOException {
        CategoryExplorerModel model = _gui.getCategoryExplorerTree().getExplorerModel();
        CategoryNode root = model.getRootCategoryNode();

        StringBuffer xml = new StringBuffer(2048);
        openXMLDocument(xml);
        openConfigurationXML(xml);
        processMsgTypes(_gui.getLogLevelMenuItems(), xml);
        processMsgTypesColors(_gui.getLogLevelMenuItems(),
                MsgType.getLogLevelColorMap(), xml);

        processLogTableColumns(LogTableColumn.getLogTableColumns(), xml);


        processConfigurationNode(root, xml);

        processFont(_table.getFont(), xml);

        processDateFormat(_gui, xml);

        processSplitPanes(_gui, xml);

        processWindowPosition(_gui, xml);

        processListeners(_gui.getSubscriptions(), xml);


        closeConfigurationXML(xml);
        store(xml.toString());
    }

    private void processListeners(final Iterator it, StringBuffer xml) {

        if (it == null) {
            return;
        }
        xml.append("\t<").append(LSNRSUBSCRIPTIONS).append(">\n");

        while (it.hasNext()) {
            exportListener((RvParameters) it.next(), xml);
        }

        xml.append("\t</").append(LSNRSUBSCRIPTIONS).append(">\n");

    }

    private void exportListener(RvParameters rvParam, StringBuffer xml) {
        xml.append("\t\t<").append(LSNRTIBLISTENER).append(" ");
        xml.append(LSNRTIBSERVICE).append("=\"").append(rvParam.getService() == null ? "" : rvParam.getService()).append("\" ");
        xml.append(LSNRTIBNETWORK).append("=\"").append(rvParam.getNetwork() == null ? "" : rvParam.getNetwork()).append("\" ");
        xml.append(LSNRTIBDAEMON).append("=\"").append(rvParam.getDaemon() == null ? "" : rvParam.getDaemon()).append("\" >\n");
        //xml.append("DESCRIPTION").append("=\"").append(rvParam.getDescription()).append("\" >");

        final Set s = rvParam.getSubjects();
        if (s != null) {
            final Iterator it = s.iterator();
            while (it.hasNext()) {
                xml.append("\t\t\t<").append(LSNRTIBSUBJECT).append(" ");
                xml.append(LSNRTIBSUBJECTID).append("=\"<![CDATA[ ").append(it.next()).append("]]>\" />\n");
            }
        }
        xml.append("\t\t</").append(LSNRTIBLISTENER).append(">\n");
    }

    private void processWindowPosition(RvSnooperGUI gui, StringBuffer xml) {

        exportWindowPosition(gui.getWindowBounds(), xml);
    }

    private void exportWindowPosition(Rectangle r, StringBuffer xml) {
        xml.append("\t<").append(WINDOWPOS).append(" ");
        xml.append(WINDOWX).append("=\"").append(String.valueOf((int) r.getX())).append("\"").append(" ");
        xml.append(WINDOWY).append("=\"").append(String.valueOf((int) r.getY())).append("\"").append(" ");
        xml.append(WINDOWWIDTH).append("=\"").append(String.valueOf((int) r.getWidth())).append("\"").append(" ");
        xml.append(WINDOWHEIGHT).append("=\"").append(String.valueOf((int) r.getHeight())).append("\"").append(" ");
        xml.append("/>\n\r");
    }

    private void processSplitPanes(RvSnooperGUI gui, StringBuffer xml) {
        exportSplitPanes(gui.getSplitPaneTableViewerPos(), gui.getSplitPaneVerticalPos(), xml);
    }

    private void exportSplitPanes(int horizontal, int vertical, StringBuffer xml) {
        xml.append("\t<").append(SPLITPANEPOS).append(" ");
        xml.append(SPLITPANEPOSH).append("=\"").append(String.valueOf(horizontal)).append("\"").append(" ");
        xml.append(SPLITPANEPOSV).append("=\"").append(String.valueOf(vertical)).append("\"").append(" ");
        xml.append("/>\n\r");
    }

    private void processDateFormat(RvSnooperGUI gui, StringBuffer xml) {

        exportDateFormatPattern(gui.getDateFormat(), xml);

    }

    private void exportDateFormatPattern(String pattern, StringBuffer xml) {
        xml.append("\t<").append(DATEFORMAT).append(" ");
        xml.append(DATEPATTERN).append("=\"").append(pattern).append("\"").append(" ");
        xml.append("/>\n\r");
    }

    protected void processDateFormat(Document doc) {

        try {
            Node n;
            NodeList nodes = doc.getElementsByTagName(DATEFORMAT);

            String dateFormatPattern;

            n = nodes.item(0);
            NamedNodeMap map = n.getAttributes();
            dateFormatPattern = getValue(map, DATEPATTERN);

            if (dateFormatPattern != null) {
                _gui.setDateFormat(dateFormatPattern);
            }

        } catch (Exception e1) {
            //  if not there
            _gui.setDateFormat("HH:mm:ss.S");

        }
    }


    public void reset() {
        deleteConfigurationFile();
        collapseTree();
        selectAllNodes();
    }

    public static String treePathToString(TreePath path) {
        // count begins at one so as to not include the 'Categories' - root category
        StringBuffer sb = new StringBuffer();
        CategoryNode n = null;
        Object[] objects = path.getPath();
        for (int i = 1; i < objects.length; ++i) {
            n = (CategoryNode) objects[i];
            if (i > 1) {
                sb.append(".");
            }
            sb.append(n.getTitle());
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------
    protected void load() {
        File file = new File(getFilename());
        if (file.exists()) {
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.
                        newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(file);
                doc.normalize();
                processCategories(doc);
                processMsgTypes(doc);
                processLogLevelColors(doc);
                processLogTableColumns(doc);
                processFont(doc);
                processDateFormat(doc);
                processSplitPanes(doc);
                processWindowPosition(doc);

                processListeners(doc);


            } catch (Exception e) {
                // ignore all error and just continue as if there was no
                // configuration xml file but do report a message
                System.err.println("Unable process configuration file at " +
                        getFilename() + ". Error Message=" + e.getMessage());


            }
        } else {
            _table.setDateFormatManager(new DateFormatManager("HH:mm:ss.S"));
        }

    }

    /**
     * Read configured tib listeners from configuration file
     *
     * @param doc  DOM object
     */
    private void processListeners(Document doc) {

        try {
            Node n;
            NodeList tiblisternodes = doc.getElementsByTagName("SUBSCRIPTIONS");

            if (tiblisternodes == null) {
                return;
            }

            // for-each configured tiblisttner
            NodeList ln = tiblisternodes.item(0).getChildNodes();
            Set setRvParameters = new HashSet();
            int len = ln.getLength();
            for (int i = 0; i < len; ++i) {
                RvParameters p = new RvParameters();
                Node listener = ln.item(i);
                if (listener.getNodeType() == 3) {
                    //ignore whitespace
                    continue;
                }

                if (listener.hasAttributes()) {
                    NamedNodeMap nnm;
                    Node service;
                    Node network;
                    Node daemon;

                    nnm = listener.getAttributes();

                    service = nnm.getNamedItem(LSNRTIBSERVICE);
                    network = nnm.getNamedItem(LSNRTIBNETWORK);
                    daemon = nnm.getNamedItem(LSNRTIBDAEMON);

                    p.setService(service.getNodeValue());
                    p.setNetwork(network.getNodeValue());
                    p.setDeamon(daemon.getNodeValue());

                }

                // collect the listeners
                NodeList subs = listener.getChildNodes();
                int leni = subs.getLength();
                Set setRvListeners = new HashSet();
                for (int iSubscription = 0; iSubscription < leni; ++iSubscription) {
                    Node subscription = subs.item(iSubscription);

                    if (subscription.getNodeType() == 3) {
                        //ignore whitespace
                        continue;
                    }

                    if (subscription.hasAttributes()) {
                        NamedNodeMap nnm;
                        Node id;
                        nnm = subscription.getAttributes();
                        id = nnm.getNamedItem(LSNRTIBSUBJECTID);

                        setRvListeners.add(id.getNodeValue());
                    }
                     p.setSubjects(setRvListeners);
                }

                setRvParameters.add(p);

            }
            // start the configured listeners
            _gui.startListeners(setRvParameters);
        } catch (DOMException e) {
            System.err.println(e.getLocalizedMessage());
        }


    }

    private void processWindowPosition(Document doc) {
        try {
            Node n;
            NodeList nodes = doc.getElementsByTagName(WINDOWPOS);

            String windowY;
            String windowHeight;
            String windowWidth;
            String windowX;

            n = nodes.item(0);
            NamedNodeMap map = n.getAttributes();
            windowHeight = getValue(map, WINDOWHEIGHT);
            windowWidth = getValue(map, WINDOWWIDTH);
            windowX = getValue(map, WINDOWX);
            windowY = getValue(map, WINDOWY);

            if (windowHeight != null && windowWidth != null
                    && windowX != null && windowY != null) {
                Rectangle r = new Rectangle(Integer.parseInt(windowX),
                        Integer.parseInt(windowY),
                        Integer.parseInt(windowWidth),
                        Integer.parseInt(windowHeight));
                _gui.setWindowBounds(r);

            } else {

                throw new Exception("");
            }

        } catch (Exception e1) {
            //  if not there
            e1.printStackTrace();
            System.out.println(e1.getMessage());
            _gui.updateFrameSize();
        }

    }

    protected void processSplitPanes(Document doc) {
        try {
            Node n;
            NodeList nodes = doc.getElementsByTagName(SPLITPANEPOS);

            String sizeHorizontal;
            String sizeVertical;

            n = nodes.item(0);
            NamedNodeMap map = n.getAttributes();
            sizeHorizontal = getValue(map, SPLITPANEPOSH);
            sizeVertical = getValue(map, SPLITPANEPOSV);

            if (sizeHorizontal != null && sizeHorizontal != null) {
                _gui.setSplitPaneTableViewerPos(Integer.parseInt(sizeHorizontal));
                _gui.setSplitPaneVerticalPos(Integer.parseInt(sizeVertical));
            } else {

                throw new Exception("");
            }

        } catch (Exception e1) {
            //  if not there
            _gui.setSplitPaneVerticalPos(130);
            _gui.setSplitPaneTableViewerPos(350);
        }

    }


    protected void processCategories(Document doc) {
        CategoryExplorerTree tree = _gui.getCategoryExplorerTree();
        CategoryExplorerModel model = tree.getExplorerModel();
        NodeList nodeList = doc.getElementsByTagName(CATEGORY);

        // determine where the starting node is
        NamedNodeMap map = nodeList.item(0).getAttributes();
        int j = (getValue(map, NAME).equalsIgnoreCase(FIRST_CATEGORY_NAME)) ? 1 : 0;
        // iterate backwards throught the nodeList so that expansion of the
        // list can occur
        for (int i = nodeList.getLength() - 1; i >= j; --i) {
            Node n = nodeList.item(i);
            map = n.getAttributes();
            CategoryNode chnode = model.addCategory(new CategoryPath(getValue(map, PATH)));
            chnode.setSelected((getValue(map, SELECTED).equalsIgnoreCase("true")) ? true : false);
            if (getValue(map, EXPANDED).equalsIgnoreCase("true")) ;
            tree.expandPath(model.getTreePathToRoot(chnode));
        }

    }

    protected void processMsgTypes(Document doc) {
        NodeList nodeList = doc.getElementsByTagName(LEVEL);
        Map menuItems = _gui.getLogLevelMenuItems();

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);
            NamedNodeMap map = n.getAttributes();
            String name = getValue(map, NAME);
            try {
                JCheckBoxMenuItem item =
                        (JCheckBoxMenuItem) menuItems.get(MsgType.valueOf(name));
                item.setSelected(getValue(map, SELECTED).equalsIgnoreCase("true"));
            } catch (MsgTypeFormatException e) {
                // ignore it will be on by default.
            }
        }
    }

    protected void processLogLevelColors(Document doc) {
        NodeList nodeList = doc.getElementsByTagName(COLORLEVEL);
        Map logLevelColors = MsgType.getLogLevelColorMap();

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);

            if (n == null) {
                return;
            }

            NamedNodeMap map = n.getAttributes();
            String name = getValue(map, NAME);
            try {
                MsgType level = MsgType.valueOf(name);
                int red = Integer.parseInt(getValue(map, RED));
                int green = Integer.parseInt(getValue(map, GREEN));
                int blue = Integer.parseInt(getValue(map, BLUE));
                Color c = new Color(red, green, blue);
                if (level != null) {
                    level.setLogLevelColorMap(level, c);
                }

            } catch (MsgTypeFormatException e) {
                // ignore it will be on by default.
            }
        }
    }

    protected void processFont(Document doc) {

        try {
            Node n;
            NodeList nodes = doc.getElementsByTagName(FONTINFO);

            String fontSize;
            String fontStyle;
            String fontName;
            fontSize = null;
            fontName = null;

            n = nodes.item(0);
            NamedNodeMap map = n.getAttributes();
            fontName = getValue(map, FONTNAME);
            fontSize = getValue(map, FONTSIZE);
            fontStyle = getValue(map, FONTSTYLE);

            if (fontName != null && fontSize != null) {
                _gui.setFontSize(Integer.parseInt(fontStyle));
                _gui.setFontName(fontName);
            }

        } catch (Exception e1) {
            //  if not there
            _gui.setFontSize(12);
        }
    }

    private void processFont(Font f, StringBuffer xml) {
        exportFont(f.getFontName(), f.getStyle(), f.getSize(), xml);

    }

    private void exportFont(String fontName, int style, int size, StringBuffer xml) {
        xml.append("\t<").append(FONTINFO).append(" ");
        xml.append(FONTNAME).append("=\"").append(fontName).append("\"").append(" ");
        xml.append(FONTSTYLE).append("=\"").append(style).append("\"").append(" ");
        xml.append(FONTSIZE).append("=\"").append(size).append("\"").append(" ");
        xml.append("/>\n\r");
    }

    private void appendStartTag(StringBuffer s, String tag) {
        s.append("\t<");
        s.append(tag);
        s.append(">\r\n");
    }

    private void appendCloseTag(StringBuffer s, String tag) {
        s.append("\n</");
        s.append(tag);
        s.append(">\r\n");
    }

    protected void processLogTableColumns(Document doc) {
        NodeList nodeList = doc.getElementsByTagName(COLUMN);
        Map menuItems = _gui.getLogTableColumnMenuItems();
        List selectedColumns = new ArrayList();
        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);

            if (n == null) {
                return;
            }
            NamedNodeMap map = n.getAttributes();
            String name = getValue(map, NAME);

            try {
                LogTableColumn column = LogTableColumn.valueOf(name);
                JCheckBoxMenuItem item =
                        (JCheckBoxMenuItem) menuItems.get(column);
                item.setSelected(getValue(map, SELECTED).equalsIgnoreCase("true"));

                if (item.isSelected()) {
                    selectedColumns.add(column);
                }
            } catch (LogTableColumnFormatException e) {
                // ignore it will be on by default.
            }
        }

        if (selectedColumns.isEmpty()) {
            _table.setDetailedView();
        } else {
            _table.setView(selectedColumns);
        }

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node n = nodeList.item(i);

            if (n == null) {
                return;
            }
            NamedNodeMap map = n.getAttributes();
            String width;
            String name;
            try {
                name = getValue(map, NAME);
                width = getValue(map, COLUMWIDTH);

                _table.setColumnWidth(name, Integer.parseInt(width));

            } catch (Exception e) {
                // ignore it will be on by default.
            }

        }
    }

    protected String getValue(NamedNodeMap map, String attr) {
        Node n = map.getNamedItem(attr);
        return n.getNodeValue();
    }

    protected void collapseTree() {
        // collapse everything except the first category
        CategoryExplorerTree tree = _gui.getCategoryExplorerTree();
        for (int i = tree.getRowCount() - 1; i > 0; i--) {
            tree.collapseRow(i);
        }
    }

    protected void selectAllNodes() {
        CategoryExplorerModel model = _gui.getCategoryExplorerTree().getExplorerModel();
        CategoryNode root = model.getRootCategoryNode();
        Enumeration all = root.breadthFirstEnumeration();
        CategoryNode n = null;
        while (all.hasMoreElements()) {
            n = (CategoryNode) all.nextElement();
            n.setSelected(true);
        }
    }

    public static void createConfigurationDirectory() {
        String home = System.getProperty("user.home");
        File f = new File(home + SEP + CONFIG_DIR_NAME);
        if (!f.exists()) {
            try {
                f.mkdirs();
            } catch (SecurityException e) {
                throw e;
            }
        }

    }

    protected void store(String s) throws IOException {

        try {
            createConfigurationDirectory();

            File f = new File(getFilename());

            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (Exception e) {

                } finally {
                    f = null;
                }
            }


            PrintWriter writer = new PrintWriter(new FileWriter(getFilename()));

            writer.print(s);
            writer.close();
        } catch (IOException e) {
            // do something with this error.
            throw e;
        }

    }

    protected void deleteConfigurationFile() {
        try {
            File f = new File(getFilename());
            if (f.exists()) {
                f.delete();
            }
        } catch (SecurityException e) {
            System.err.println("Cannot delete " + getFilename() +
                    " because a security violation occured.");
        }
    }

    protected String getDefaultFilename() {
        String home = System.getProperty("user.home");
        return home + SEP + CONFIG_DIR_NAME + SEP + CONFIG_FILE_NAME;
    }

    public String getFilename() {
        return _fileName;
    }

    public void setFilename(String fileName) {
        this._fileName = fileName;
    }

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------
    private void processConfigurationNode(CategoryNode node, StringBuffer xml) {
        CategoryExplorerModel model = _gui.getCategoryExplorerTree().getExplorerModel();

        Enumeration all = node.breadthFirstEnumeration();
        CategoryNode n = null;
        while (all.hasMoreElements()) {
            n = (CategoryNode) all.nextElement();
            exportXMLElement(n, model.getTreePathToRoot(n), xml);
        }

    }


    private void processMsgTypes(Map logLevelMenuItems, StringBuffer xml) {
        xml.append("\t<msgtypes>\r\n");
        Iterator it = logLevelMenuItems.keySet().iterator();
        while (it.hasNext()) {
            MsgType level = (MsgType) it.next();
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) logLevelMenuItems.get(level);
            exportLogLevelXMLElement(level.getLabel(), item.isSelected(), xml);
        }

        xml.append("\t</msgtypes>\r\n");
    }

    private void processMsgTypesColors(Map logLevelMenuItems, Map logLevelColors, StringBuffer xml) {
        xml.append("\t<msgtypescolors>\r\n");
        // iterate through the list of log levels being used (log4j, jdk1.4, custom levels)
        Iterator it = logLevelMenuItems.keySet().iterator();
        while (it.hasNext()) {
            MsgType level = (MsgType) it.next();
            // for each level, get the associated color from the log level color map
            Color color = (Color) logLevelColors.get(level);
            exportLogLevelColorXMLElement(level.getLabel(), color, xml);
        }

        xml.append("\t</msgtypescolors>\r\n");
    }


    private void processLogTableColumns(List logTableColumnMenuItems, StringBuffer xml) {
        xml.append("\t<logtablecolumns>\r\n");
        Iterator it = logTableColumnMenuItems.iterator();
        while (it.hasNext()) {
            LogTableColumn column = (LogTableColumn) it.next();
            JCheckBoxMenuItem item = _gui.getTableColumnMenuItem(column);
            int size = _table.getColumnWidth(column.getLabel());
            exportLogTableColumnXMLElement(column.getLabel(), item.isSelected(), size, xml);
        }

        xml.append("\t</logtablecolumns>\r\n");
    }


    private void openXMLDocument(StringBuffer xml) {
        xml.append("<?xml version=\"1.0\" encoding=\"" + System.getProperty("file.encoding") + "\" ?>\r\n");
    }

    private void openConfigurationXML(StringBuffer xml) {
        xml.append("<configuration>\r\n");
    }

    private void closeConfigurationXML(StringBuffer xml) {
        xml.append("</configuration>\r\n");
    }

    private void exportXMLElement(CategoryNode node, TreePath path, StringBuffer xml) {
        CategoryExplorerTree tree = _gui.getCategoryExplorerTree();

        xml.append("\t<").append(CATEGORY).append(" ");
        xml.append(NAME).append("=\"").append(node.getTitle()).append("\" ");
        xml.append(PATH).append("=\"").append(treePathToString(path)).append("\" ");
        xml.append(EXPANDED).append("=\"").append(tree.isExpanded(path)).append("\" ");
        xml.append(SELECTED).append("=\"").append(node.isSelected()).append("\"/>\r\n");
    }

    private void exportLogLevelXMLElement(String label, boolean selected, StringBuffer xml) {
        xml.append("\t\t<").append(LEVEL).append(" ").append(NAME);
        xml.append("=\"").append(label).append("\" ");
        xml.append(SELECTED).append("=\"").append(selected);
        xml.append("\"/>\r\n");
    }

    private void exportLogLevelColorXMLElement(String label, Color color, StringBuffer xml) {
        xml.append("\t\t<").append(COLORLEVEL).append(" ").append(NAME);
        xml.append("=\"").append(label).append("\" ");
        xml.append(RED).append("=\"").append(color.getRed()).append("\" ");
        xml.append(GREEN).append("=\"").append(color.getGreen()).append("\" ");
        xml.append(BLUE).append("=\"").append(color.getBlue());
        xml.append("\"/>\r\n");
    }

    private void exportLogTableColumnXMLElement(String label, boolean selected, int size, StringBuffer xml) {
        xml.append("\t\t<").append(COLUMN).append(" ").append(NAME);
        xml.append("=\"").append(label).append("\" ");
        xml.append(SELECTED).append("=\"").append(selected).append("\" ");
        xml.append(COLUMWIDTH).append("=\"").append(String.valueOf(size));
        xml.append("\"/>\r\n");
    }
    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}






