/*
 * Class:     PredicateFactory
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.rvsnoop.NLSUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A factory for creating data accessors.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class PredicateFactory {

    static { NLSUtils.internationalize(PredicateFactory.class); }

    private static PredicateFactory instance;

    static String ERROR_BAD_IDENTIFIER, ERROR_BAD_NAME;

    public static synchronized PredicateFactory getInstance() {
        if (instance == null) { instance = new PredicateFactory(); }
        return instance;
    }

    private final Map identifiersToPredicatesMap = new LinkedHashMap();

    private final Map namesToPredicatesMap = new LinkedHashMap();

    private PredicateFactory() {
        identifiersToPredicatesMap.put(Predicate.StringContains.IDENTIFIER, Predicate.StringContains.class);
        identifiersToPredicatesMap.put(Predicate.StringEndsWith.IDENTIFIER, Predicate.StringEndsWith.class);
        identifiersToPredicatesMap.put(Predicate.StringEquals.IDENTIFIER, Predicate.StringEquals.class);
        identifiersToPredicatesMap.put(Predicate.StringNotEquals.IDENTIFIER, Predicate.StringNotEquals.class);
        identifiersToPredicatesMap.put(Predicate.StringRegex.IDENTIFIER, Predicate.StringRegex.class);
        identifiersToPredicatesMap.put(Predicate.StringStartsWith.IDENTIFIER, Predicate.StringStartsWith.class);
        namesToPredicatesMap.put(Predicate.STRING_CONTAINS, Predicate.StringContains.class);
        namesToPredicatesMap.put(Predicate.STRING_ENDS_WITH, Predicate.StringEndsWith.class);
        namesToPredicatesMap.put(Predicate.STRING_EQUALS, Predicate.StringEquals.class);
        namesToPredicatesMap.put(Predicate.STRING_NOT_EQUALS, Predicate.StringNotEquals.class);
        namesToPredicatesMap.put(Predicate.STRING_REGEX, Predicate.StringRegex.class);
        namesToPredicatesMap.put(Predicate.STRING_STARTS_WITH, Predicate.StringStartsWith.class);
    }

    public Predicate createStringStartsWithPredicate(String value, boolean ignoreCase) {
        return new Predicate.StringStartsWith(value, ignoreCase);
    }

    public Predicate createFromDisplayName(String name, String value, boolean ignoreCase) {
        return createFromString(namesToPredicatesMap, name, value, ignoreCase, ERROR_BAD_NAME);
    }

    public Predicate createFromIdentifier(String identifier, String value, boolean ignoreCase) {
        return createFromString(identifiersToPredicatesMap, identifier, value, ignoreCase, ERROR_BAD_IDENTIFIER);
    }

    private Predicate createFromString(Map map, String string, String value, boolean ignoreCase, String errorMessage) {
        Class clazz = (Class) map.get(checkNotNull(string));
        if (clazz == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format(errorMessage, string));
        }
        try {
            return (Predicate) clazz.getConstructor(String.class, boolean.class).newInstance(value, ignoreCase);
        } catch (Exception e) {
            // TODO handle this more gracefully somehow
            //      Maybe just log the exception and return null?
            throw new RuntimeException(e);
        }
    }

    public String[] getDisplayNames() {
        final Set names = namesToPredicatesMap.keySet();
        return (String[]) names.toArray(new String[names.size()]);
    }

}
