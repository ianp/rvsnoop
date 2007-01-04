/*
 * Class:     PreferencesManager
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.awt.Font;
import java.awt.Rectangle;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.RecordLedgerFormat;
import org.rvsnoop.RecordLedgerFormat.ColumnFormat;
import org.rvsnoop.ui.RecordLedgerTable;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import rvsnoop.ui.UIManager;

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
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SELECTED = "selected";
    private static final String COLUMN_WIDTH = "width";
    private static final String COLUMNS = "columns";
    private static final String CONFIG_DIRECTORY = ".rvsnoop";
    private static final String CONFIG_FILE = "preferences.xml";
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

    private static final Log log = LogFactory.getLog(PreferencesManager.class);

    public static final PreferencesManager INSTANCE = new PreferencesManager();

    private static File getPreferencesFile() {
        final String home = System.getProperty("user.home");
        final String fs = System.getProperty("file.separator");
        return new File(home + fs + CONFIG_DIRECTORY + fs + CONFIG_FILE);
    }

    private static void loadSplitPosition(Element parent) {
        final Elements elements = parent.getChildElements(SPLIT_POSITION);
        for (int i = 0, imax = elements.size(); i < imax; ++i) {
            final Element element = elements.get(i);
            final String name = getString(element, SPLIT_NAME);
            if (SPLIT_CONNECTIONS.equals(name))
                UIManager.INSTANCE.setConnectionListDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
            else if (SPLIT_EXPLORER.equals(name))
                UIManager.INSTANCE.setSubjectExplorerDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
            else if (SPLIT_LEDGER.equals(name))
                UIManager.INSTANCE.setMessageLedgerDividerLocation(getInteger(element, SPLIT_DIVIDER, -1));
        }
    }

    private static void loadTableColumns(RecordLedgerTable table, Element parent) {
        final Elements columns = parent.getChildElements(COLUMNS);
        final int size = columns.size();
        final RecordLedgerFormat format = table.getTableFormat();
        for (int i = 0; i < size; ++i) {
            final Element element = columns.get(i);
            final String identifier = getString(element, COLUMN_NAME);
            final ColumnFormat column = RecordLedgerFormat.getColumn(identifier);
            try {
                if (!getBoolean(element, COLUMN_SELECTED, true)) {
                    format.remove(column);
                } else {
                    final TableColumn tc = table.getColumn(column.getName());
                    tc.setPreferredWidth(getInteger(element, COLUMN_WIDTH, tc.getWidth()));
                }
            } catch (Exception ignored) {
                // Intentionally ignored.
            }
        }
    }

    private static void loadTableFont(Element parent) {
        try {
            final Font font = UIManager.INSTANCE.getMessageLedgerFont();
            final Element elt = parent.getFirstChildElement(FONT);
            final String name = getString(elt, FONT_NAME);
            final int size = getInteger(elt, FONT_SIZE, font.getSize());
            final int style = getInteger(elt, FONT_STYLE, font.getStyle());
            UIManager.INSTANCE.setMessageLedgerFont(new Font(name != null ? name : font.getName(), style, size));
        } catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn("Unable to restore font preferences.", e);
            UIManager.INSTANCE.setMessageLedgerFont(new Font("Dialog", Font.PLAIN, 11));
        }
    }

    private static void loadWindowPosition(Element parent) {
        final Element window = parent.getFirstChildElement(WINDOW_POSITION);
        if (window == null) return;
        final Rectangle bounds = UIManager.INSTANCE.getFrame().getBounds();
        try {
            bounds.x = Integer.parseInt(getString(window, WINDOW_X));
            bounds.y = Integer.parseInt(getString(window, WINDOW_Y));
            bounds.width = Integer.parseInt(getString(window, WINDOW_WIDTH));
            bounds.height = Integer.parseInt(getString(window, WINDOW_HEIGHT));
            UIManager.INSTANCE.getFrame().setBounds(bounds);
        } catch (Exception ignored) {
            // Intentionally ignored.
        }
    }

    private static void storeSplitPosition(Element parent) {
        final Element mlSplit = appendElement(parent, SPLIT_POSITION);
        setString(mlSplit, SPLIT_NAME, SPLIT_LEDGER);
        setInteger(mlSplit, SPLIT_DIVIDER, UIManager.INSTANCE.getMessageLedgerDividerLocation());
        final Element seSplit = appendElement(parent, SPLIT_POSITION);
        setString(seSplit, SPLIT_NAME, SPLIT_EXPLORER);
        setInteger(seSplit, SPLIT_DIVIDER, UIManager.INSTANCE.getSubjectExplorerDividerLocation());
        final Element clSplit = appendElement(parent, SPLIT_POSITION);
        setString(clSplit, SPLIT_NAME, SPLIT_CONNECTIONS);
        setInteger(clSplit, SPLIT_DIVIDER, UIManager.INSTANCE.getConnectionListDividerLocation());
    }

    private static void storeTableColumns(RecordLedgerTable table, Element parent) {
        final Element columns = appendElement(parent, COLUMNS);
        final List visible = table.getTableFormat().getColumns();
        final Iterator i = RecordLedgerFormat.ALL_COLUMNS.iterator();
        while (i.hasNext()) {
            final ColumnFormat column = (ColumnFormat) i.next();
            if (column == null) { throw new NullPointerException(); }
            final String identifier = column.getIdentifier();
            final Element element = appendElement(columns, COLUMN);
            setString(element, COLUMN_NAME, identifier);
            setBoolean(element, COLUMN_SELECTED, visible.contains(column));
            final int width = table.getColumn(column.getName()).getWidth();
            setInteger(element, COLUMN_WIDTH, width);
        }
    }

    private static void storeTableFont(Element parent) {
        final Font font = UIManager.INSTANCE.getMessageLedgerFont();
        final Element fontElement = appendElement(parent, FONT);
        setString(fontElement, FONT_NAME, font.getFamily());
        setInteger(fontElement, FONT_SIZE, font.getSize());
        setInteger(fontElement, FONT_STYLE, font.getStyle());
    }

    private static void storeWindowPosition(Element parent) {
        final Rectangle r = UIManager.INSTANCE.getFrame().getBounds();
        final Element window = appendElement(parent, WINDOW_POSITION);
        setString(window, WINDOW_X, Integer.toString(r.x));
        setString(window, WINDOW_Y, Integer.toString(r.y));
        setString(window, WINDOW_WIDTH, Integer.toString(r.width));
        setString(window, WINDOW_HEIGHT, Integer.toString(r.height));
    }

    private RecordLedgerTable table;
    public void setRecordLedgerTable(RecordLedgerTable table) {
        this.table = table;
    }

    private PreferencesManager() {
        super(getPreferencesFile());
    }

    public PreferencesManager(Application application) {
        this();
    }

    protected Document getDocument() {
        final Element root = new Element(ROOT);
        storeTableColumns(table, root);
        storeTableFont(root);
        storeSplitPosition(root);
        storeWindowPosition(root);
        return new Document(root);
    }

    protected void load(Element root) {
        loadTableColumns(table, root);
        loadTableFont(root);
        loadSplitPosition(root);
        loadWindowPosition(root);
    }

}
