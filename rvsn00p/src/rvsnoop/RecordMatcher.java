//:File:    RecordMatcher.java
//:Created: Jan 18, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.lang.reflect.Constructor;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A matcher for records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public abstract class RecordMatcher implements Matcher {

    private static final Map matchersById = new HashMap();
    private static final Map matchersByName = new HashMap();

    public static final class EverythingMatcher extends RecordMatcher {
        public static final String ID = "Everything";
        public static final String NAME = "Everything";
        public EverythingMatcher() {
            super(ID, NAME, "");
        }
        public boolean matches(Object item) {
            return true;
        }
    }

    public static final class SendSubjectContains extends RecordMatcher {
        public static final String ID = "SendSubjectContains";
        public static final String NAME = "Send Subject Contains";
        public SendSubjectContains(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().indexOf(getValue()) >= 0;
        }
    }

    public static final class SendSubjectStartsWith extends RecordMatcher {
        public static final String ID = "SendSubjectStartsWith";
        public static final String NAME = "Send Subject Starts With";
        public SendSubjectStartsWith(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().startsWith(getValue());
        }
    }

    /**
     * Constructs a new matcher from information contained in an XML fragment.
     *
     * @param element The element that represents the matcher.
     * @return The connection.
     */
    public static RecordMatcher fromXml(Element element) {
        Validate.isTrue(XML_ELEMENT.equals(element.getLocalName()), "The element’s localname must be " + XML_ELEMENT + '.');
        Validate.isTrue(XML_NS.equals(element.getNamespaceURI()), "The element must be in the namespace " + XML_NS + '.');
        final String type = element.getAttributeValue(PROP_TYPE);
        final String value = element.getAttributeValue(PROP_VALUE);
        return createMatcher(type, value);
    }

    public static final RecordMatcher DEFAULT_MATCHER = new EverythingMatcher();

    public static final String PROP_TYPE = "type";
    public static final String PROP_VALUE = "value";

    public static final String XML_ELEMENT = "matcher";
    public static final String XML_NS = "http://rvsnoop.org/ns/matcher/1";

    private static final Log log = LogFactory.getLog(RecordMatcher.class);

    /**
     * Create a new matcher.
     * <p>
     * We need to be able to look things up by name as well to ease building
     * editing UIs.
     *
     * @param typeOrName The name or type of the matcher type to create.
     * @param value The value to be passed to the new matcher instance.
     * @return The new matcher instance.
     */
    public static RecordMatcher createMatcher(String typeOrName, String value) {
        Class clazz = (Class) matchersById.get(typeOrName);
        if (clazz == null) clazz = (Class) matchersByName.get(typeOrName);
        if (clazz == null) throw new IllegalArgumentException("No matcher named " + typeOrName + '.');
        try {
            final Constructor ctor = clazz.getConstructor(new Class[] { String.class });
            return (RecordMatcher) ctor.newInstance(new Object[] { value });
        } catch (Exception e) {
            if (log.isErrorEnabled())
                log.error("Could not create a matcher from name=‘" + typeOrName + "’ and value=‘" + value + "’.", e);
            return null;
        }
    }

    /**
     * Get the set of known matcher names.
     * <p>
     * The array of names will be sorted according to the default locale.
     *
     * @return The names of all known record matchers.
     */
    public static String[] getMatcherNames() {
        final Set nameSet = matchersByName.keySet();
        final String[] names = (String[]) nameSet.toArray(new String[nameSet.size()]);
        Arrays.sort(names, Collator.getInstance());
        return names;
    }

    private final String type;

    private final String name;

    private String value;

    /**
     * Create a new <code>RecordMatcher</code>.
     */
    protected RecordMatcher(String type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
        matchersById.put(type, getClass());
        matchersByName.put(name, getClass());
    }

	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

    /**
     * @return The type of the matcher.
     */
    public String getType() {
        return type;
    }

	/**
	 * @return The value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

    public String toString() {
        return new ToStringBuilder(this)
            .append(PROP_TYPE, type)
            .append(PROP_VALUE, value).toString();
    }

    /**
     * Create an XML fragment that represents this matcher.
     *
     * @return the XML fragment that represents this matcher.
     */
    public Element toXml() {
        final Element element = new Element(XML_ELEMENT, XML_NS);
        element.addAttribute(new Attribute(PROP_TYPE, type));
        element.addAttribute(new Attribute(PROP_VALUE, value));
        return element;
    }

}
