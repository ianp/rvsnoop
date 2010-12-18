// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Objects;
import nu.xom.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.odell.glazedlists.matchers.Matcher;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A matcher for records.
 */
public abstract class RecordMatcher implements Matcher<Record> {

    private static final Map<String, Class<RecordMatcher>> matchersById = new HashMap<String, Class<RecordMatcher>>();
    private static final Map<String, Class<RecordMatcher>> matchersByName = new HashMap<String, Class<RecordMatcher>>();

    public static final class EverythingMatcher extends RecordMatcher {
        public static final String ID = "Everything";
        public static final String NAME = "Everything";
        public EverythingMatcher() {
            super(ID, NAME, "");
        }
        public boolean matches(Record item) {
            return true;
        }
    }

    public static final class SendSubjectContains extends RecordMatcher {
        public static final String ID = "SendSubjectContains";
        public static final String NAME = "Send Subject Contains";
        public SendSubjectContains(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Record item) {
            return item.getSendSubject().indexOf(getValue()) >= 0;
        }
    }

    public static final class SendSubjectStartsWith extends RecordMatcher {
        public static final String ID = "SendSubjectStartsWith";
        public static final String NAME = "Send Subject Starts With";
        public SendSubjectStartsWith(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Record item) {
            return item.getSendSubject().startsWith(getValue());
        }
    }

    /**
     * Constructs a new matcher from information contained in an XML fragment.
     *
     * @param element The element that represents the matcher.
     * @return The connection.
     */
    public static RecordMatcher fromXml(Element element) {
        checkArgument(XML_ELEMENT.equals(element.getLocalName()), "The element’s localname must be %s.", XML_ELEMENT);
        checkArgument(XML_NS.equals(element.getNamespaceURI()), "The element must be in the namespace %s.", XML_NS);
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
    	Class<RecordMatcher> clazz = matchersById.get(typeOrName);
        if (clazz == null) clazz = matchersByName.get(typeOrName);
        if (clazz == null) throw new IllegalArgumentException("No matcher named " + typeOrName + '.');
        try {
            return clazz.getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            if (log.isErrorEnabled())
                log.error("Could not create a matcher from name=‘" + typeOrName + "’ and value=‘" + value + "’.", e);
            return null;
        }
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
        matchersById.put(type, RecordMatcher.class);
        matchersByName.put(name, RecordMatcher.class);
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add(PROP_TYPE, type)
                .add(PROP_VALUE, value).toString();
    }

}
