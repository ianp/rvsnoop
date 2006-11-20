//:File:    RecordTypes.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableModel;

import rvsnoop.actions.Actions;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * RvSnoop allows the user to classify records based on fairly arbitrary criteria.
 * <p>
 * The mechanism used for this is the {@link RecordType} class, this object
 * holds a list of all the known types.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecordTypes {

    public final class MenuManager implements PopupMenuListener {
        public MenuManager() {
            super();
        }
        public void popupMenuCanceled(PopupMenuEvent e) {
            // Do nothing.
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // Do nothing.
        }
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            final JPopupMenu menu = (JPopupMenu) e.getSource();
            menu.removeAll();
            int index = 0;
            types.getReadWriteLock().readLock().lock();
            try {
                for (int i = 0, imax = types.size(); i < imax; ++i) {
                    final RecordType type = (RecordType) types.get(i);
                    final JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                            DEFAULT.equals(type) ? "Default" : type.getName());
                    item.setSelected(type.isSelected());
                    item.setMnemonic(KeyEvent.VK_0 + ++index);
                    item.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                type.setSelected(true);
                                matcherEditor.relax();
                            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                                type.setSelected(false);
                                matcherEditor.constrain();
                            }
                        }
                    });
                    menu.add(item);
                }
            } finally {
                types.getReadWriteLock().readLock().unlock();
            }
            menu.addSeparator();
            menu.add(Actions.EDIT_RECORD_TYPES);
        }
    }

    private class MessageTypeMatcherEditor extends AbstractMatcherEditor {
        MessageTypeMatcherEditor() {
            super();
        }
        void constrain() {
            fireConstrained(getMatcher());
        }
        public Matcher getMatcher() {
            return new Matcher() {
                public boolean matches(Object item) {
                    final Record record = (Record) item;
                    types.getReadWriteLock().readLock().lock();
                    try {
                        final Iterator i = types.iterator();
                        while (i.hasNext()) {
                            final RecordType type = (RecordType) i.next();
                            if (type.matches(record))
                                return type.isSelected();
                        }
                        return true;
                    } finally {
                        types.getReadWriteLock().readLock().unlock();
                    }
                }
            };
        }
        void relax() {
            fireRelaxed(getMatcher());
        }
    }

    private static RecordTypes instance;

    public static final RecordType DEFAULT = new RecordType("", Color.BLACK, RecordMatcher.DEFAULT_MATCHER);
    public static final RecordType ERROR = new RecordType("Error", Color.RED, new RecordMatcher.SendSubjectContains("ERROR"));

    public static synchronized RecordTypes getInstance() {
        if (instance == null) instance = new RecordTypes();
        return instance;
    }

    final MessageTypeMatcherEditor matcherEditor = new MessageTypeMatcherEditor();

    final EventList types = new BasicEventList();

    private RecordTypes() {
        reset();
    }

    public void clear() {
        types.getReadWriteLock().writeLock().lock();
        try {
            types.clear();
            types.add(DEFAULT);
        } finally {
            types.getReadWriteLock().writeLock().unlock();
        }
    }

    public RecordType createType() {
        return createType(generateName(), Color.BLACK, RecordMatcher.DEFAULT_MATCHER);
    }

    public RecordType createType(String name, Color color, RecordMatcher matcher) {
        if (RecordTypes.getInstance().isNameInUse(name))
            throw new IllegalArgumentException("Type name already in use: " + name);
        final RecordType type =  new RecordType(name, color, matcher);
        types.add(type);
        return type;
    }

    private String generateName() {
        types.getReadWriteLock().readLock().lock();
        try {
            final String[] nameArray = new String[types.size()];
            for (int i = types.size() - 1; i >= 0; --i) {
                nameArray[i] = ((RecordType) types.get(i)).getName();
            }
            final List names = Arrays.asList(nameArray);
            String name;
            for (int i = 1; true; ++i) {
                name = "My Record Type #" + Integer.toString(i);
                if (!names.contains(name)) return name;
            }
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public RecordType[] getAllTypes() {
        types.getReadWriteLock().readLock().lock();
        try {
            return (RecordType[]) types.toArray(new RecordType[types.size()]);
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public RecordType getFirstMatchingType(Record record) {
        types.getReadWriteLock().readLock().lock();
        try {
            final Iterator i = types.iterator();
            while (i.hasNext()) {
                final RecordType type = (RecordType) i.next();
                if (type.matches(record))
                    return type;
            }
            return DEFAULT;
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Get a list model suitable for displaying the record types in a swing component.
     *
     * @return A new list model that holds the record types.
     */
    public ListModel getListModel() {
        return new EventListModel(types);
    }

    public MatcherEditor getMatcherEditor() {
        return matcherEditor;
    }

    /**
     * Get a table model suitable for displaying the record types in a swing component.
     *
     * @return A new table model that holds the record types.
     */
    public TableModel getTableModel(TableFormat format) {
        return new EventTableModel(types, format);
    }

    public RecordType getType(int index) {
        return (RecordType) types.get(index);
    }

    public boolean isNameInUse(String name) {
        if (name == null) throw new NullPointerException();
        types.getReadWriteLock().readLock().lock();
        try {
            for (int i = 0, imax = types.size(); i < imax; ++i)
                if (name.equals(((RecordType) types.get(i)).getName()))
                    return true;
            return false;
        } finally {
            types.getReadWriteLock().readLock().unlock();
        }
    }

    public void removeType(RecordType type) {
        if (!DEFAULT.equals(type)) {
            types.getReadWriteLock().writeLock().lock();
            try {
                types.remove(type);
            } finally {
                types.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    /**
     * Re-order the type to a new priority.
     *
     * @param type The type to re-order.
     * @param index The new priority for the type.
     */
    public void reorderType(RecordType type, int index) {
        types.getReadWriteLock().writeLock().lock();
        try {
            if (index < 0) index = 0;
            else if (index >= types.size()) {
                types.remove(type);
                types.add(type);
                return;
            }
            final int oldIndex = types.indexOf(type);
            if (index == oldIndex) return;
            // If the new position is after the old one then adjust the index to
            // take into account the type about to be removed:
            final int newIndex = index > oldIndex ? index - 1 : index;
            types.add(newIndex, types.remove(oldIndex));
        } finally {
            types.getReadWriteLock().writeLock().unlock();
        }
    }

    public void reset() {
        types.getReadWriteLock().writeLock().lock();
        try {
            types.clear();
            types.add(ERROR);
            types.add(new RecordType("Warning", Color.ORANGE, new RecordMatcher.SendSubjectContains("WARN")));
            types.add(new RecordType("System", Color.GRAY, new RecordMatcher.SendSubjectStartsWith("_")));
            types.add(DEFAULT);
        } finally {
            types.getReadWriteLock().writeLock().unlock();
        }
    }

    public int size() {
        return types.size();
    }
}
