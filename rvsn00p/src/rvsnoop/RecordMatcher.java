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

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A matcher for records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public abstract class RecordMatcher implements Matcher {

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

    public static final RecordMatcher DEFAULT_MATCHER = new EverythingMatcher();

    private static final Logger logger = Logger.getLogger(RecordMatcher.class);

    private static final Map matchersById = new HashMap();
    private static final Map matchersByName = new HashMap();

    static {
        matchersById.put(EverythingMatcher.ID, EverythingMatcher.class);
        matchersById.put(SendSubjectContains.ID, SendSubjectContains.class);
        matchersById.put(SendSubjectStartsWith.ID, SendSubjectStartsWith.class);
        matchersByName.put(EverythingMatcher.ID, EverythingMatcher.class);
        matchersByName.put(SendSubjectContains.NAME, SendSubjectContains.class);
        matchersByName.put(SendSubjectStartsWith.NAME, SendSubjectStartsWith.class);
    }

    /**
     * Create a new matcher.
     * <p>
     * We need to be able to look things up by name as well to ease building
     * editing UIs.
     *
     * @param idOrName The name or ID of the matcher type to create.
     * @param value The value to be passed to the new matcher instance.
     * @return The new matcher instance.
     */
    public static RecordMatcher createMatcher(String idOrName, String value) {
        Class clazz = (Class) matchersById.get(idOrName);
        if (clazz == null) clazz = (Class) matchersByName.get(idOrName);
        if (clazz == null) throw new IllegalArgumentException("No matcher named " + idOrName + ".");
        try {
            final Constructor ctor = clazz.getConstructor(new Class[] { String.class });
            return (RecordMatcher) ctor.newInstance(new Object[] { value });
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Could not create a matcher from name='" + idOrName + "' and value='" + value + "'.", e);
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

    private final String id;

    private final String name;

    private String value;

    /**
     * Create a new <code>RecordMatcher</code>.
     */
    protected RecordMatcher(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

	/**
	 * @return The ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return The name.
	 */
	public String getName() {
		return name;
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
        return "[RecordMatcher: id=" + id + ", name=" + name + ", value=" + value + "]";
    }
}
