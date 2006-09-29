//:File:    RecordType.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * RvSnoop allows the user to classify records based on fairly arbitrary criteria.
 * Message types are the mechanism used to enable this.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RecordType implements Matcher {

    private static int nextTypeId = 1;

    /** Key for colour JavaBean property. */
    public static final String PROP_COLOUR = "colour";

    /** Key for id JavaBean property. */
    public static final String PROP_ID = "id";

    /** Key for matcherName JavaBean property. */
    public static final String PROP_MATCHER_NAME = "matcherName";

    /** Key for matcherValue JavaBean property. */
    public static final String PROP_MATCHER_VALUE = "matcherValue";

    /** Key for name JavaBean property. */
    public static final String PROP_NAME = "name";

    /** Key for selected JavaBean property. */
    public static final String PROP_SELECTED = "selected";

    private static synchronized int generateId() {
        return nextTypeId++;
    }

    private Color colour;

    private transient final int id;

    private boolean selected = true;

    private RecordMatcher matcher;

    private String name;

    private final PropertyChangeSupport propChange =
        new PropertyChangeSupport(this);

    RecordType(String name, Color color, RecordMatcher matcher) {
        super();
        this.id = generateId();
        this.name = name;
        this.colour = color;
        this.matcher = matcher;
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(propertyName, listener);
    }

    public boolean equals(Object o) {
        return o instanceof RecordType && id == ((RecordType) o).id;
    }

    public Color getColour() {
        return colour;
    }

    public int getId() {
        return id;
    }

    public RecordMatcher getMatcher() {
        return matcher;
    }

    public String getMatcherName() {
        return matcher.getName();
    }

    public String getMatcherValue() {
        return matcher.getValue();
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        return id * 17; // 17 is just an arbitrary prime number.
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean matches(Object item) {
        return matcher.matches(item);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(propertyName, listener);
    }

    public void setColour(Color colour) {
        if (colour == null) throw new NullPointerException();
        Color oldValue = this.colour;
        this.colour = colour;
        propChange.firePropertyChange(PROP_COLOUR, oldValue, colour);
    }

    public void setMatcherName(String name) {
        if (name == null) throw new NullPointerException();
        String oldValue = matcher.getName();
        RecordMatcher.createMatcher(name, matcher.getValue());
        propChange.firePropertyChange(PROP_MATCHER_NAME, oldValue, name);
    }

    public void setMatcherValue(String value) {
        if (value == null) throw new NullPointerException();
        String oldValue = matcher.getValue();
        matcher.setValue(value);
        propChange.firePropertyChange(PROP_MATCHER_VALUE, oldValue, value);
    }

    public void setName(String name) {
        if (name == null) throw new NullPointerException();
        String oldValue = this.name;
        if (RecordTypes.getInstance().isNameInUse(name))
            throw new IllegalArgumentException("Type name already in use: " + name);
        this.name = name;
        propChange.firePropertyChange(PROP_NAME, oldValue, name);
    }

//    /**
//     * Set the priority of this record type.
//     *
//     * @param priority The priority to set.
//     * @throws IllegalArgumentException if <code>priority < 0</code> or
//     *         <code>priority >= allTypes.size()</code>.
//     */
//    public void setPriority(int priority) {
//        synchronized (allTypes) {
//            if (priority < 0 || priority >= allTypes.size())
//                throw new IllegalArgumentException("Illegal priority: " + priority);
//            int index = allTypes.indexOf(this);
//            allTypes.remove(index);
//            allTypes.add(index < priority ? priority : priority - 1, this);
//        }
//    }

    public void setSelected(boolean selected) {
        boolean oldValue = this.selected;
        this.selected = selected;
        propChange.firePropertyChange(PROP_SELECTED, oldValue, selected);
    }

    public String toString() {
        return "[RecordType: id=" + id + ", name=" + name + ", matcher=" + matcher + "]";
    }
}
