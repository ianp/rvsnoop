//:File:    ConfigurationManager.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.tree.TreePath;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;
import nu.xom.Text;

import rvsn00p.MsgType;
import rvsn00p.StringUtils;
import rvsn00p.util.IOUtils;
import rvsn00p.util.rv.RvParameters;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerModel;
import rvsn00p.viewer.categoryexplorer.CategoryExplorerTree;
import rvsn00p.viewer.categoryexplorer.CategoryNode;
import rvsn00p.viewer.categoryexplorer.CategoryPath;

/**
 * Handles the storage and retrival of the state of the Category Explorer.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class ConfigurationManager extends Object {

    private static final String COLOUR = "colour";
    private static final String COLOUR_BLUE = "blue";
    private static final String COLOUR_GREEN = "green";
    private static final String COLOUR_RED = "red";
    private static final String COLUMN = "column";
    private static final String COLUMN_SELECTED = "selected";
    private static final String COLUMN_WIDTH = "width";
    private static final String COLUMNS = "columns";
    private static final String CONFIG_DIRECTORY = ".rvsnoop";
    private static final String CONFIG_FILE = "config.xml";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String DATE_FORMAT_PATTERN = "HH:mm:ss.S";
    private static final String FONT = "font";
    private static final String FONT_NAME = "name";
    private static final String FONT_SIZE = "size";
    private static final String ROOT = "rvsnoop";
    private static final String RV_DAEMON = "daemon";
    private static final String RV_LISTENER = "listener";
    private static final String RV_NETWORK = "network";
    private static final String RV_SERVICE = "service";
    private static final String SPLIT_H = "h";
    private static final String SPLIT_POSITION = "splitPosition";
    private static final String SPLIT_V = "v";
    private static final String SUBJECT = "subject";
    private static final String SUBJECT_EXPANDED = "expanded";
    private static final String SUBJECT_SELECTED = "selected";
    private static final String SUBJECTS = "subjects";
    private static final String TYPE = "messageType";
    private static final String TYPE_NAME = "name";
    private static final String TYPE_SELECTED = "selected";
    private static final String WINDOW_HEIGHT = "h";
    private static final String WINDOW_POSITION = "windowPosition";
    private static final String WINDOW_WIDTH = "w";
    private static final String WINDOW_X = "x";
    private static final String WINDOW_Y = "y";

    private static Element appendElement(Element parent, String name) {
        Element child = new Element(name);
        parent.appendChild(child);
        return child;
    }

    private static boolean getBoolean(Element parent, String attr, boolean def) {
        try {
            String value = parent.getAttributeValue(attr);
            return Boolean.valueOf(value).booleanValue();
        } catch (Exception e) {
            return def;
        }
    }
    
    private static String getDefaultFilename() {
        String home = System.getProperty("user.home");
        String fs = System.getProperty("file.separator");
        return home + fs + CONFIG_DIRECTORY + fs + CONFIG_FILE;
    }
    
    private static String getString(Element parent, String name) {
        Element child = parent.getFirstChildElement(name);
        return child != null ? child.getValue().trim() : null;
    }
    
    /**
     * @param path The path to convert.
     * @param buffer The buffer to work with.
     * @return
     */
    private static StringBuffer pathToString(TreePath path, StringBuffer buffer) {
        buffer.setLength(0);
        // Begin at one to skip the 'Categories' root node.
        Object[] nodes = path.getPath();
        for (int i = 1; i < nodes.length; ++i)
            buffer.append(((CategoryNode) nodes[i]).getTitle()).append(".");
        buffer.setLength(buffer.length() - 1);
        return buffer;
    }

    private static void setBoolean(Element parent, String name, boolean value) {
        parent.addAttribute(new Attribute(name, Boolean.toString(value)));
    }
    
    private static Element setString(Element parent, String name, String value) {
        if (value == null || value.length() == 0) return null;
        Element child = new Element(name);
        parent.appendChild(child);
        child.appendChild(new Text(value));
        return child;
    }

    private String filename;

    private final RvSnooperGUI gui;

    private final LogTable table;

    public ConfigurationManager(RvSnooperGUI gui, LogTable table) {
        this(gui, table, getDefaultFilename());
    }

    public ConfigurationManager(RvSnooperGUI gui, LogTable table, String filename) {
        super();
        this.gui = gui;
        this.table = table;
        this.filename = filename;
        load();
    }

    private void dateFormatExport(Element parent) {
        setString(parent, DATE_FORMAT, StringUtils.getDateFormat());
    }

    private void dateFormatImport(Element parent) {
        String dateFormat = getString(parent, DATE_FORMAT);
        StringUtils.setDateFormat(dateFormat != null ? dateFormat : DATE_FORMAT_PATTERN);
    }

    /**
     * Delete the saved configuration.
     * <p>
     * Whether there was a saved configuration or not, this method will always
     * reset the state of the subject explorer tree.
     */
    public void delete() {
        try {
            File file = new File(filename);
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            System.err.println("Could not delete configuration from " + filename + " because " + e.getMessage());
        }
        CategoryExplorerTree tree = gui.getCategoryExplorerTree();
        CategoryExplorerModel model = tree.getExplorerModel();
        // Collapse everything except the root node.
        for (int i = tree.getRowCount() - 1; i != 0; --i)
            tree.collapseRow(i);
        Enumeration nodes = model.getRootCategoryNode().breadthFirstEnumeration();
        while (nodes.hasMoreElements())
            ((CategoryNode) nodes.nextElement()).setSelected(true);
    }

    private void fontExport(Element parent) {
        Font font = table.getFont();
        Element fontElement = appendElement(parent, FONT);
        setString(fontElement, FONT_NAME, font.getFontName());
        setString(fontElement, FONT_SIZE, Integer.toString(font.getSize()));
    }

    private void fontImport(Element parent) {
        try {
            Element font = parent.getFirstChildElement(FONT);
            String name = getString(font, FONT_NAME);
            if (name != null) gui.setFontName(name);
            String size = getString(font, FONT_SIZE);
            gui.setFontSize(size != null ? Integer.parseInt(size) : 12);
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

    /**
     * Get the file that is used to store the configuration.
     * 
     * @return The name of the file.
     */
    public String getFilename() {
        return filename;
    }

    private void listenersExport(Element parent) {
        Iterator iter = gui.getSubscriptions();
        if (iter == null) return;
        while (iter.hasNext()) {
            RvParameters params = (RvParameters) iter.next();
            Element listener = appendElement(parent, RV_LISTENER);
            setString(listener, RV_SERVICE, params.getService());
            setString(listener, RV_NETWORK, params.getNetwork());
            setString(listener, RV_DAEMON, params.getDaemon());
            Set subjects = params.getSubjects();
            if (subjects == null) continue;
            for (Iterator i = subjects.iterator(); i.hasNext(); )
                setString(listener, SUBJECT, (String) i.next());
        }
    }

    private void listenersImport(Element parent) {
        Elements listeners = parent.getChildElements(RV_LISTENER);
        Set params = new HashSet(listeners.size());
        for (int i = listeners.size(); i != 0;) {
            Element listener = listeners.get(--i);
            String service = getString(listener, RV_SERVICE);
            String network = getString(listener, RV_NETWORK);
            String daemon = getString(listener, RV_DAEMON);
            RvParameters param = new RvParameters(service, network, daemon);
            Elements subjects = listener.getChildElements(SUBJECT);
            for (int j = subjects.size(); j != 0;)
                param.addSubject(subjects.get(--j).getValue());
            params.add(param);
        }
        gui.startListeners(params);
    }

    /**
     * Load a saved configuration from a file.
     * <p>
     * If the file does not exist then this just resets the date format.
     */
    public void load() {
        File file = new File(getFilename());
        if (!file.exists()) return;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            Document doc = new Builder().build(stream);
            Element root = doc.getRootElement();
            subjectTreeImport(root);
            typesImport(root);
            tableColumnsImport(root);
            fontImport(root);
            dateFormatImport(root);
            splitPositionImport(root);
            windowPositionImport(root);
            listenersImport(root);
        } catch (Exception e) {
            System.err.println("Unable to load configuration from " + file.getPath());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Set the file that is used to store the configuration.
     * 
     * @param filename The file name to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    private void splitPositionExport(Element parent) {
        Element splitPos = appendElement(parent, SPLIT_POSITION);
        setString(splitPos, SPLIT_H, Integer.toString(gui.getSplitPaneTableViewerPos()));
        setString(splitPos, SPLIT_V, Integer.toString(gui.getSplitPaneVerticalPos()));
    }

    private void splitPositionImport(Element parent) {
        Element splitPos = parent.getFirstChildElement(SPLIT_POSITION);
        if (splitPos == null) return;
        try {
            gui.setSplitPaneTableViewerPos(Integer.parseInt(getString(splitPos, SPLIT_H)));
            gui.setSplitPaneVerticalPos(Integer.parseInt(getString(splitPos, SPLIT_V)));
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

    /**
     * Save the current configuration to file.
     * 
     * @throws IOException
     */
    public void store() throws IOException {
        File file = new File(getFilename());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        Element config = new Element(ROOT);
        typesExport(config);
        tableColumnsExport(config);
        subjectTreeExport(config);
        fontExport(config);
        dateFormatExport(config);
        splitPositionExport(config);
        windowPositionExport(config);
        listenersExport(config);
        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            Serializer ser = new Serializer(stream);
            ser.setIndent(2);
            ser.setLineSeparator("\n");
            ser.write(new Document(config));
            ser.flush();
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
    
    private void subjectTreeExport(Element parent) {
        CategoryExplorerTree tree = gui.getCategoryExplorerTree();
        CategoryExplorerModel model = tree.getExplorerModel();
        Element subjects = appendElement(parent, SUBJECTS);
        Enumeration nodes = model.getRootCategoryNode().breadthFirstEnumeration();
        StringBuffer buffer = new StringBuffer();
        // Skip the root node, which does not represent a subject name element.
        nodes.nextElement();
        while (nodes.hasMoreElements()) {
            CategoryNode node = (CategoryNode) nodes.nextElement();
            TreePath path = model.getTreePathToRoot(node);
            Element subject = setString(subjects, SUBJECT, pathToString(path, buffer).toString());
            setBoolean(subject, SUBJECT_EXPANDED, tree.isExpanded(path));
            setBoolean(subject, SUBJECT_SELECTED, node.isSelected());
        }
    }

    private void subjectTreeImport(Element parent) {
        CategoryExplorerTree tree = gui.getCategoryExplorerTree();
        CategoryExplorerModel model = tree.getExplorerModel();
        Element subjectsRoot = parent.getFirstChildElement(SUBJECTS);
        if (subjectsRoot == null) return;
        Elements subjects = subjectsRoot.getChildElements(SUBJECT);
        for (int i = subjects.size(); i != 0;) {
            Element subject = subjects.get(--i);
            CategoryPath path = new CategoryPath(subject.getValue().trim());
            CategoryNode node = model.addCategory(path);
            node.setSelected(getBoolean(subject, SUBJECT_SELECTED, true));
            if (getBoolean(subject, SUBJECT_EXPANDED, false))
                tree.expandPath(model.getTreePathToRoot(node));
        }
    }

    private void tableColumnsExport(Element parent) {
        Iterator iter = LogTableColumn.getLogTableColumns().iterator();
        Element columns = appendElement(parent, COLUMNS);
        while (iter.hasNext()) {
            LogTableColumn column = (LogTableColumn) iter.next();
            JCheckBoxMenuItem item = gui.getTableColumnMenuItem(column);
            int size = table.getColumnWidth(column.getLabel());
            Element columnElt = setString(columns, COLUMN, column.getLabel());
            columnElt.addAttribute(new Attribute(COLUMN_SELECTED, Boolean.toString(item.isSelected())));
            columnElt.addAttribute(new Attribute(COLUMN_WIDTH, Integer.toString(size)));
        }
    }

    private void tableColumnsImport(Element parent) {
        Map menuItems = gui.getLogTableColumnMenuItems();
        Elements columns = parent.getChildElements(COLUMNS);
        int size = columns.size();
        List selectedColumns = new ArrayList(size);
        for (int i = 0; i < size; ++i) {
            Element columnElt = columns.get(i);
            String name = columnElt.getValue();
            LogTableColumn column = LogTableColumn.valueOf(name);
            if (column == null) continue;
            try {
                String width = columnElt.getAttributeValue(COLUMN_WIDTH);
                table.setColumnWidth(name, Integer.parseInt(width));
                Object item = menuItems.get(columnElt);
                Boolean selected = Boolean.valueOf(columnElt.getAttributeValue(COLUMN_SELECTED));
                if (selected.booleanValue()) {
                    ((JCheckBoxMenuItem) item).setSelected(true);
                    selectedColumns.add(column);
                }
            } catch (Exception ignored) {
                // Intentionally ignored.
            }
        }
        if (selectedColumns.isEmpty())
            table.setDetailedView();
        else
            table.setView(selectedColumns);
    }

    private void typesExport(Element parent) {
        Map menuItems = gui.getLogLevelMenuItems();
        Map colours = MsgType.getLogLevelColorMap();
        Iterator it = menuItems.keySet().iterator();
        while (it.hasNext()) {
            MsgType type = (MsgType) it.next();
            Element typeElt = appendElement(parent, TYPE);
            typeElt.addAttribute(new Attribute(TYPE_NAME, type.getLabel()));
            boolean selected = ((JCheckBoxMenuItem) menuItems.get(type)).isSelected();
            setBoolean(typeElt, TYPE_SELECTED, selected);
            Color colour = (Color) colours.get(type);
            Element colourElt = appendElement(typeElt, COLOUR);
            setString(colourElt, COLOUR_RED, Integer.toString(colour.getRed()));
            setString(colourElt, COLOUR_GREEN, Integer.toString(colour.getGreen()));
            setString(colourElt, COLOUR_BLUE, Integer.toString(colour.getBlue()));
        }
    }

    private void typesImport(Element parent) {
        Map menuItems = gui.getLogLevelMenuItems();
        Elements types = parent.getChildElements(TYPE);
        for (int i = types.size(); i != 0;) {
            Element typeElt = types.get(--i);
            String name = typeElt.getAttributeValue(TYPE_NAME);
            try {
                MsgType type = MsgType.valueOf(name);
                if (type == null)
                    continue;
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) menuItems.get(MsgType.valueOf(name));
                item.setSelected(getBoolean(typeElt, TYPE_SELECTED, true));
                Element colour = typeElt.getFirstChildElement(COLOUR);
                int r = Integer.parseInt(getString(colour, COLOUR_RED));
                int g = Integer.parseInt(getString(colour, COLOUR_GREEN));
                int b = Integer.parseInt(getString(colour, COLOUR_BLUE));
                MsgType.setLogLevelColorMap(type, new Color(r, g, b));
            } catch (Exception ignored) {
                // Intentionally ignored.
            }
        }
    }

    private void windowPositionExport(Element parent) {
        Rectangle r = gui.getWindowBounds();
        Element window = appendElement(parent, WINDOW_POSITION);
        setString(window, WINDOW_X, Integer.toString(r.x));
        setString(window, WINDOW_Y, Integer.toString(r.y));
        setString(window, WINDOW_WIDTH, Integer.toString(r.width));
        setString(window, WINDOW_HEIGHT, Integer.toString(r.height));
    }

    private void windowPositionImport(Element parent) {
        Element window = parent.getFirstChildElement(WINDOW_POSITION);
        if (window == null) return;
        Rectangle bounds = gui.getWindowBounds();
        try {
            bounds.x = Integer.parseInt(getString(window, WINDOW_X));
            bounds.y = Integer.parseInt(getString(window, WINDOW_Y));
            bounds.width = Integer.parseInt(getString(window, WINDOW_WIDTH));
            bounds.height = Integer.parseInt(getString(window, WINDOW_HEIGHT));
            gui.setWindowBounds(bounds);
            gui.updateFrameSize();
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

}
