//:File:    PreferencesManager.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import rvsn00p.viewer.LogTable;
import rvsn00p.viewer.LogTableColumn;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Handles the storage and retrival of the state of the application.
 * <p>
 * This class only handles global application state, there are also projects
 * which handle storing connection details and so on.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class PreferencesManager extends XMLConfigFile {

    private static final String COLUMN = "column";
    private static final String COLUMN_SELECTED = "selected";
    private static final String COLUMN_WIDTH = "width";
    private static final String COLUMNS = "columns";
    private static final String CONFIG_DIRECTORY = ".rvsnoop";
    private static final String CONFIG_FILE = "preferences.xml";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String DATE_FORMAT_PATTERN = "HH:mm:ss.S";
    private static final String FONT = "font";
    private static final String FONT_NAME = "name";
    private static final String FONT_SIZE = "size";
    private static final String FONT_STYLE = "style";
    private static final String ROOT = "preferences";
    private static final String SPLIT_CONNECTIONS = "connectionList";
    private static final String SPLIT_DIVIDER = "divider";
    private static final String SPLIT_EXPLORER = "subjectExplorer";
    private static final String SPLIT_LEDGER = "messageLedger";
    private static final String SPLIT_NAME = "name";
    private static final String SPLIT_POSITION = "splitPosition";
    private static final String WINDOW_HEIGHT = "h";
    private static final String WINDOW_POSITION = "windowPosition";
    private static final String WINDOW_WIDTH = "w";
    private static final String WINDOW_X = "x";
    private static final String WINDOW_Y = "y";
    
    private static final Logger logger = Logger.getLogger(PreferencesManager.class);

    public static final PreferencesManager INSTANCE = new PreferencesManager();
    
    private static File getPreferencesFile() {
        final String home = System.getProperty("user.home");
        final String fs = System.getProperty("file.separator");
        return new File(home + fs + CONFIG_DIRECTORY + fs + CONFIG_FILE);
    }

    public PreferencesManager() {
        super(getPreferencesFile());
    }

    private void dateFormatExport(Element parent) {
        setString(parent, DATE_FORMAT, StringUtils.getDateFormat());
    }

    private void dateFormatImport(Element parent) {
        final String dateFormat = getString(parent, DATE_FORMAT);
        StringUtils.setDateFormat(dateFormat != null ? dateFormat : DATE_FORMAT_PATTERN);
    }

    private void fontExport(Element parent) {
        final Font font = RvSnooperGUI.getInstance().getTableFont();
        final Element fontElement = appendElement(parent, FONT);
        setString(fontElement, FONT_NAME, font.getFamily());
        setInteger(fontElement, FONT_SIZE, font.getSize());
        setInteger(fontElement, FONT_STYLE, font.getStyle());
    }

    private void fontImport(Element parent) {
        try {
            final Font font = RvSnooperGUI.getInstance().getTableFont();
            final Element elt = parent.getFirstChildElement(FONT);
            final String name = getString(elt, FONT_NAME);
            final int size = getInteger(elt, FONT_SIZE, font.getSize());
            final int style = getInteger(elt, FONT_STYLE, font.getStyle());
            RvSnooperGUI.getInstance().setTableFont(new Font(name != null ? name : font.getName(), style, size));
        } catch (Exception e) {
            if (logger.isWarnEnabled())
                logger.warn("Unable to restore font preferences.", e);
            RvSnooperGUI.getInstance().setTableFont(new Font("Dialog", Font.PLAIN, 11));
        }
    }

    protected Document getDocument() {
        final Element root = new Element(ROOT);
        tableColumnsExport(root);
        fontExport(root);
        dateFormatExport(root);
        splitPositionExport(root);
        windowPositionExport(root);
        return new Document(root);
    }

    protected void load(Element root) {
        tableColumnsImport(root);
        fontImport(root);
        dateFormatImport(root);
        splitPositionImport(root);
        windowPositionImport(root);
    }

    private void splitPositionExport(Element parent) {
        final Element mlSplit = appendElement(parent, SPLIT_POSITION);
        setString(mlSplit, SPLIT_NAME, SPLIT_LEDGER);
        setInteger(mlSplit, SPLIT_DIVIDER, RvSnooperGUI.getInstance().getMessageLedgerDividerLocation());
        final Element seSplit = appendElement(parent, SPLIT_POSITION);
        setString(seSplit, SPLIT_NAME, SPLIT_EXPLORER);
        setInteger(seSplit, SPLIT_DIVIDER, RvSnooperGUI.getInstance().getSubjectExplorerDividerLocation());
        final Element clSplit = appendElement(parent, SPLIT_POSITION);
        setString(clSplit, SPLIT_NAME, SPLIT_CONNECTIONS);
        setInteger(clSplit, SPLIT_DIVIDER, RvSnooperGUI.getInstance().getConnectionListDividerLocation());
    }

    private void splitPositionImport(Element parent) {
        final Elements elements = parent.getChildElements(SPLIT_POSITION);
        for (int i = 0, imax = elements.size(); i < imax; ++i) {
            final Element element = elements.get(i);
            final String name = getString(element, SPLIT_NAME);
            if (SPLIT_CONNECTIONS.equals(name))
                RvSnooperGUI.getInstance().setConnectionListDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
            else if (SPLIT_EXPLORER.equals(name))
                RvSnooperGUI.getInstance().setSubjectExplorerDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
            else if (SPLIT_LEDGER.equals(name))
                RvSnooperGUI.getInstance().setMessageLedgerDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
        }
    }
    
    private void tableColumnsExport(Element parent) {
        final Element columns = appendElement(parent, COLUMNS);
        final Iterator i = LogTableColumn.getLogTableColumns().iterator();
        while (i.hasNext()) {
            final LogTableColumn column = (LogTableColumn) i.next();
            final JCheckBoxMenuItem item = RvSnooperGUI.getInstance().getTableColumnMenuItem(column);
            final int size = RvSnooperGUI.getInstance().getMessageLedger().getColumnWidth(column.getLabel());
            final Element columnElt = setString(columns, COLUMN, column.getLabel());
            columnElt.addAttribute(new Attribute(COLUMN_SELECTED, Boolean.toString(item.isSelected())));
            columnElt.addAttribute(new Attribute(COLUMN_WIDTH, Integer.toString(size)));
        }
    }

    private void tableColumnsImport(Element parent) {
        final Map menuItems = RvSnooperGUI.getInstance().getLogTableColumnMenuItems();
        final Elements columns = parent.getChildElements(COLUMNS);
        final int size = columns.size();
        final List selectedColumns = new ArrayList(size);
        final LogTable table = RvSnooperGUI.getInstance().getMessageLedger();
        for (int i = 0; i < size; ++i) {
            final Element columnElt = columns.get(i);
            final String name = columnElt.getValue();
            final LogTableColumn column = LogTableColumn.valueOf(name);
            if (column == null) continue;
            try {
                final String width = columnElt.getAttributeValue(COLUMN_WIDTH);
                table.setColumnWidth(name, Integer.parseInt(width));
                final Object item = menuItems.get(columnElt);
                final Boolean selected = Boolean.valueOf(columnElt.getAttributeValue(COLUMN_SELECTED));
                if (selected.booleanValue()) {
                    ((AbstractButton) item).setSelected(true);
                    selectedColumns.add(column);
                }
            } catch (Exception ignored) {
                // Intentionally ignored.
            }
        }
        if (selectedColumns.isEmpty())
            table.setView(LogTableColumn.getLogTableColumns());
        else
            table.setView(selectedColumns);
    }

    private void windowPositionExport(Element parent) {
        final Rectangle r = RvSnooperGUI.getFrame().getBounds();
        final Element window = appendElement(parent, WINDOW_POSITION);
        setString(window, WINDOW_X, Integer.toString(r.x));
        setString(window, WINDOW_Y, Integer.toString(r.y));
        setString(window, WINDOW_WIDTH, Integer.toString(r.width));
        setString(window, WINDOW_HEIGHT, Integer.toString(r.height));
    }

    private void windowPositionImport(Element parent) {
        final Element window = parent.getFirstChildElement(WINDOW_POSITION);
        if (window == null) return;
        final Rectangle bounds = RvSnooperGUI.getFrame().getBounds();
        try {
            bounds.x = Integer.parseInt(getString(window, WINDOW_X));
            bounds.y = Integer.parseInt(getString(window, WINDOW_Y));
            bounds.width = Integer.parseInt(getString(window, WINDOW_WIDTH));
            bounds.height = Integer.parseInt(getString(window, WINDOW_HEIGHT));
            RvSnooperGUI.getFrame().setBounds(bounds);
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

}
