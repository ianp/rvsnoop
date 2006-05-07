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
 * A collection of useful matchers for records.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class RecordMatcher {
    
    public static abstract class NameValueMatcher implements Matcher {
        private final String id;
        private final String name;
        private final String value;
        public NameValueMatcher(String id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }
        public final String getId() {
            return id;
        }
        public final String getName() {
            return name;
        }
        public final String getValue() {
            return value;
        }
    }

    public static final class SendSubjectContains extends NameValueMatcher {
        public static final String ID = "SendSubjectContains";
        public static final String NAME = "Send Subject Contains";
        public SendSubjectContains(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().indexOf(getValue()) >= 0;
        }
    }

    public static final class SendSubjectStartsWith extends NameValueMatcher {
        public static final String ID = "SendSubjectStartsWith";
        public static final String NAME = "Send Subject Starts With";
        public SendSubjectStartsWith(String value) {
            super(ID, NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().startsWith(getValue());
        }
    }

    private static final Logger logger = Logger.getLogger(RecordMatcher.class);
    
    private static final Map matchersById = new HashMap();
    private static final Map matchersByName = new HashMap();
    
    static {
        matchersById.put(SendSubjectContains.ID, SendSubjectContains.class);
        matchersById.put(SendSubjectStartsWith.ID, SendSubjectStartsWith.class);
        matchersByName.put(SendSubjectContains.NAME, SendSubjectContains.class);
        matchersByName.put(SendSubjectStartsWith.NAME, SendSubjectStartsWith.class);
    }
    
    /**
     * Create a new matcher.
     * 
     * @param idOrName The name or ID of the matcher type to create.
     * @param value The value to be passed to the new matcher instance.
     * @return The new matcher instance.
     */
    public static NameValueMatcher createMatcher(String idOrName, String value) {
        Class clazz = (Class) matchersById.get(idOrName);
        if (clazz == null) clazz = (Class) matchersByName.get(idOrName);
        if (clazz == null) throw new IllegalArgumentException("No matcher named " + idOrName + ".");
        try {
            final Constructor ctor = clazz.getConstructor(new Class[] { String.class });
            return (NameValueMatcher) ctor.newInstance(new Object[] { value });
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
     * @return
     */
    public static String[] getMatcherNames() {
        final Set nameSet = matchersByName.keySet();
        final String[] names = (String[]) nameSet.toArray(new String[nameSet.size()]);
        Arrays.sort(names, Collator.getInstance());
        return names;
    }
    
    /**
     * Do not instantiate.
     */
    private RecordMatcher() {
        throw new UnsupportedOperationException();
    }

}
