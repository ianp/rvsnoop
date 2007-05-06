/*
 * Class:     RecordType
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.text.StrBuilder;

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

    /** Key for colour JavaBean property. */
    public static final String KEY_COLOUR = "colour";

    /** Key for id JavaBean property. */
    public static final String KEY_ID = "id";

    /** Key for matcherName JavaBean property. */
    public static final String KEY_MATCHER_NAME = "matcherName";

    /** Key for matcherType JavaBean property. */
    public static final String KEY_MATCHER_TYPE = "matcherType";

    /** Key for matcherValue JavaBean property. */
    public static final String KEY_MATCHER_VALUE = "matcherValue";

    /** Key for name JavaBean property. */
    public static final String KEY_NAME = "name";

    /** Key for selected JavaBean property. */
    public static final String KEY_SELECTED = "selected";

    private Color colour;

    private boolean selected = true;

    private final RecordMatcher matcher;

    private String name;

    private final PropertyChangeSupport propChange =
        new PropertyChangeSupport(this);

    RecordType(String name, Color color, RecordMatcher matcher) {
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

    public Color getColour() {
        return colour;
    }

    private String getColourHexString() {
        return new StrBuilder(7).append('#')
            .appendFixedWidthPadLeft(Integer.toHexString(colour.getRed()), 2, '0')
            .appendFixedWidthPadLeft(Integer.toHexString(colour.getGreen()), 2, '0')
            .appendFixedWidthPadLeft(Integer.toHexString(colour.getBlue()), 2, '0')
            .toString();
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

    public void setColour(final Color colour) {
        if (colour == null) throw new NullPointerException();
        final Color oldValue = this.colour;
        this.colour = colour;
        propChange.firePropertyChange(KEY_COLOUR, oldValue, colour);
    }

    public void setMatcherName(final String name) {
        if (name == null) throw new NullPointerException();
        final String oldValue = matcher.getName();
        RecordMatcher.createMatcher(name, matcher.getValue());
        propChange.firePropertyChange(KEY_MATCHER_NAME, oldValue, name);
    }

    public void setMatcherValue(final String value) {
        if (value == null) throw new NullPointerException();
        final String oldValue = matcher.getValue();
        matcher.setValue(value);
        propChange.firePropertyChange(KEY_MATCHER_VALUE, oldValue, value);
    }

    public void setName(final String name) {
        if (name == null) throw new NullPointerException();
        final String oldValue = this.name;
        if (RecordTypes.getInstance().isNameInUse(name))
            throw new IllegalArgumentException("Type name already in use: " + name);
        this.name = name;
        propChange.firePropertyChange(KEY_NAME, oldValue, name);
    }

    public void setSelected(final boolean selected) {
        final boolean oldValue = this.selected;
        this.selected = selected;
        propChange.firePropertyChange(KEY_SELECTED, oldValue, selected);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append(KEY_NAME, name)
            .append(KEY_SELECTED, selected)
            .append(KEY_COLOUR, getColourHexString())
            .append(KEY_MATCHER_TYPE, matcher.getType())
            .append(KEY_MATCHER_VALUE, matcher.getValue()).toString();
    }

}
