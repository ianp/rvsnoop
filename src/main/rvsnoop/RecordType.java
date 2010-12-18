// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import ca.odell.glazedlists.matchers.Matcher;
import com.google.common.base.Objects;

/**
 * RvSnoop allows the user to classify records based on fairly arbitrary criteria.
 * Message types are the mechanism used to enable this.
 */
public class RecordType implements Matcher<Record> {

    /** Key for colour JavaBean property. */
    public static final String KEY_COLOUR = "colour";

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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(propertyName, listener);
    }

    public Color getColour() {
        return colour;
    }

    private String getColourHexString() {
        return String.format("#%02X%02X%02X", colour.getRed(), colour.getGreen(), colour.getBlue());
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

    public boolean matches(Record item) {
        return matcher.matches(item);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(propertyName, listener);
    }

    public void setColour(final Color colour) {
        if (colour == null) throw new NullPointerException();
        final Color oldValue = this.colour;
        this.colour = colour;
        propChange.firePropertyChange(KEY_COLOUR, oldValue, colour);
    }

    public void setSelected(final boolean selected) {
        final boolean oldValue = this.selected;
        this.selected = selected;
        propChange.firePropertyChange(KEY_SELECTED, oldValue, selected);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add(KEY_NAME, name)
                .add(KEY_SELECTED, selected)
                .add(KEY_COLOUR, getColourHexString())
                .add(KEY_MATCHER_TYPE, matcher.getType())
                .add(KEY_MATCHER_VALUE, matcher.getValue()).toString();
    }

}
