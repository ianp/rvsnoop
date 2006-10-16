//:File:    RecordType.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.text.StrBuilder;

import nu.xom.Attribute;
import nu.xom.Element;

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
    public static final String PROP_COLOUR = "colour";

    /** Key for id JavaBean property. */
    public static final String PROP_ID = "id";

    /** Key for matcherName JavaBean property. */
    public static final String PROP_MATCHER_NAME = "matcherName";

    /** Key for matcherType JavaBean property. */
    public static final String PROP_MATCHER_TYPE = "matcherType";

    /** Key for matcherValue JavaBean property. */
    public static final String PROP_MATCHER_VALUE = "matcherValue";

    /** Key for name JavaBean property. */
    public static final String PROP_NAME = "name";

    /** Key for selected JavaBean property. */
    public static final String PROP_SELECTED = "selected";

    public static final String XML_ELEMENT = "type";
    public static final String XML_NS = "http://rvsnoop.org/ns/recordType/1";

    /**
     * Constructs a new type from information contained in an XML fragment.
     * <p>
     * Note that this will add an entry to the types list if the type
     * represented by the XML fragment is not already present.
     * 
     * @param element The element that represents the type.
     * @return The connection.
     */
    public static RecordType fromXml(Element element) {
        Validate.isTrue(XML_ELEMENT.equals(element.getLocalName()), "The element’s localname must be " + XML_ELEMENT + ".");
        Validate.isTrue(XML_NS.equals(element.getNamespaceURI()), "The element must be in the namespace " + XML_NS + ".");
        final Element matcherElt = element.getFirstChildElement(RecordMatcher.XML_ELEMENT, RecordType.XML_NS);
        final RecordMatcher matcher = RecordMatcher.fromXml(matcherElt);
        final Color colour = Color.decode(element.getAttributeValue(PROP_COLOUR));
        if (RecordMatcher.DEFAULT_MATCHER.equals(matcher)) {
            RecordTypes.DEFAULT.setColour(colour);
            return RecordTypes.DEFAULT;
        }
        final String name = element.getAttributeValue(PROP_NAME);
        final RecordType type = RecordTypes.getInstance().createType(name, colour, matcher);
        type.setSelected(Boolean.valueOf(element.getAttributeValue(PROP_SELECTED)).booleanValue());
        return type;
    }

    private Color colour;

    private boolean selected = true;

    private RecordMatcher matcher;

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
            .append(StringUtils.leftPad(Integer.toHexString(colour.getRed()), 2, '0'))
            .append(StringUtils.leftPad(Integer.toHexString(colour.getGreen()), 2, '0'))
            .append(StringUtils.leftPad(Integer.toHexString(colour.getBlue()), 2, '0'))
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

    public void setSelected(boolean selected) {
        boolean oldValue = this.selected;
        this.selected = selected;
        propChange.firePropertyChange(PROP_SELECTED, oldValue, selected);
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append(PROP_NAME, name)
            .append(PROP_SELECTED, selected)
            .append(PROP_COLOUR, getColourHexString())
            .append(PROP_MATCHER_TYPE, matcher.getType())
            .append(PROP_MATCHER_VALUE, matcher.getValue()).toString();
    }

    /**
     * Create an XML fragment that represents this matcher.
     *
     * @return the XML fragment that represents this matcher.
     */
    public Element toXml() {
        final Element element = new Element(XML_ELEMENT, XML_NS);
        element.addAttribute(new Attribute(PROP_NAME, name));
        element.addAttribute(new Attribute(PROP_SELECTED, Boolean.toString(selected)));
        element.addAttribute(new Attribute(PROP_COLOUR, getColourHexString()));
        element.appendChild(matcher.toXml());
        return element;
    }

}
