//:File:    RecordMatcher.java
//:Created: Jan 18, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
        private final String name;
        private final String value;
        public NameValueMatcher(String name, String value) {
            this.name = name;
            this.value = value;
        }
        public final String getName() {
            return name;
        }
        public final String getValue() {
            return value;
        }
    }

    public static final class SendSubjectContains extends NameValueMatcher {
        public static final String NAME = "SendSubjectContains";
        public SendSubjectContains(String value) {
            super(NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().indexOf(getValue()) >= 0;
        }
    }

    public static final class SendSubjectStartsWith extends NameValueMatcher {
        public static final String NAME = "SendSubjectStartsWith";
        public SendSubjectStartsWith(String value) {
            super(NAME, value);
        }
        public boolean matches(Object item) {
            return ((Record) item).getSendSubject().startsWith(getValue());
        }
    }

    private static final Logger logger = Logger.getLogger(RecordMatcher.class);
    
    private static final Map matchers = new HashMap();
    
    static {
        matchers.put(SendSubjectContains.NAME, SendSubjectContains.class);
        matchers.put(SendSubjectStartsWith.NAME, SendSubjectStartsWith.class);
    }
    
    public static NameValueMatcher getMatcher(String name, String value) {
        final Class clazz = (Class) matchers.get(name);
        if (clazz == null) throw new IllegalArgumentException("No matcher named " + name + ".");
        try {
            final Constructor ctor = clazz.getConstructor(new Class[] { String.class });
            return (NameValueMatcher) ctor.newInstance(new Object[] { value });
        } catch (Exception e) {
            if (Logger.isErrorEnabled())
                logger.error("Could not create a matcher from name='" + name + "' and value='" + value + "'.", e);
            return null;
        }
    }
    
    /**
     * Do not instantiate.
     */
    private RecordMatcher() {
        throw new UnsupportedOperationException();
    }

}
