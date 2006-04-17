//:File:    RecordType.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * RvSnoop allows the user to classify records based on fairly arbitrary criteria, message types are the mechanism used to enable this.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RecordType implements Matcher {

    private static class MessageTypeMatcherEditor extends AbstractMatcherEditor {
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
                    final Iterator i = allMessageTypes.iterator();
                    while (i.hasNext()) {
                        final RecordType type = (RecordType) i.next();
                        if (type.matches(record))
                            return type.isSelected();
                    }
                    return true;
                }
            };
        }
        void relax() {
            fireRelaxed(getMatcher());
        }
    }

    public static final class MenuManager implements PopupMenuListener {
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
            for (final Iterator i = allMessageTypes.iterator(); i.hasNext(); ) {
                final RecordType type = (RecordType) i.next();
                final JCheckBoxMenuItem item = new JCheckBoxMenuItem(DEFAULT_TYPE.equals(type) ? "Default" : type.name);
                item.setSelected(type.isSelected);
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
        }
    }
    
    private static final LinkedList allMessageTypes = new LinkedList();
    
    private static final MessageTypeMatcherEditor matcherEditor = new MessageTypeMatcherEditor();
    
    public static final RecordType DEFAULT_TYPE = new RecordType("", Color.BLACK, new Matcher() {
        public boolean matches(Object item) {
            return true;
        }
    });
    
    public static final RecordType ERROR_TYPE = new RecordType("Error", Color.RED, new RecordMatcher.SendSubjectContains("ERROR"));
    
    public static final RecordType SYSTEM_TYPE = new RecordType("System", Color.GRAY, new RecordMatcher.SendSubjectStartsWith("_"));
    
    public static final RecordType WARNING_TYPE = new RecordType("Warning", Color.ORANGE, new RecordMatcher.SendSubjectContains("WARN"));

    static {
        reset();
    }

    public static void clear() {
        allMessageTypes.clear();
        allMessageTypes.add(DEFAULT_TYPE);
    }

    public static RecordType createType(String name, Color color, Matcher matcher) {
        final RecordType type = new RecordType(name, color, matcher);
        final int index = allMessageTypes.indexOf(type);
        if (index >= 0) return (RecordType) allMessageTypes.get(index);
        allMessageTypes.addFirst(type);
        return type;
    }
    
    public static List getAllMessageTypes() {
        return Collections.unmodifiableList(allMessageTypes);
    }
    
    public static RecordType getFirstMatchingType(Record record) {
        final Iterator i = allMessageTypes.iterator();
        while (i.hasNext()) {
            final RecordType type = (RecordType) i.next();
            if (type.matches(record))
                return type;
        }
        return DEFAULT_TYPE;
    }
    
    public static MatcherEditor getMatcherEditor() {
        return matcherEditor;
    }
    
    public static void removeType(RecordType type) {
        if (!DEFAULT_TYPE.equals(type))
            allMessageTypes.remove(type);
    }
    
    public static void reset() {
        allMessageTypes.clear();
        allMessageTypes.addAll(Arrays.asList(new RecordType[] {
            ERROR_TYPE, WARNING_TYPE, SYSTEM_TYPE, DEFAULT_TYPE
        }));
    }
    
    private final Color color;
    
    private boolean isSelected = true;
    
    private final Matcher matcher;
    
    private final String name;

    private RecordType(String name, Color color, Matcher matcher) {
        super();
        this.color = color;
        this.name = name;
        this.matcher = matcher;
        if (allMessageTypes.contains(this))
            throw new IllegalArgumentException("Duplicate type name: " + name);
    }

    public boolean equals(Object o) {
        return o instanceof RecordType && name.equals(((RecordType) o).name);
    }
    
    public Color getColor() {
        return color;
    }

    public Matcher getMatcher() {
        return matcher;
    }
    
    public String getName() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean isSelected() {
        return isSelected;
    }

    public boolean matches(Object item) {
        return matcher.matches(item);
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
}
