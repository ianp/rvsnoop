/*
 * Class:     Predicate
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.rvsnoop.NLSUtils;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public abstract class Predicate implements Matcher {

    final static class StringContains extends Predicate {
        final static String IDENTIFIER = "contains";
        final String argument;
        public StringContains(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            this.argument = ignoringCase
                    ? argument.toLowerCase(Locale.getDefault())
                    : argument;
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            String string = (String) item;
            if (ignoringCase) { string = string.toLowerCase(Locale.getDefault()); }
            // TODO change to String#contains(CharSequence) in 1.5
            return string.indexOf(argument) >= 0;
        }
    }

    final static class StringEndsWith extends Predicate {
        final static String IDENTIFIER = "endsWith";
        final String argument;
        public StringEndsWith(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            this.argument = ignoringCase
                    ? argument.toLowerCase(Locale.getDefault())
                    : argument;
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            String string = (String) item;
            if (ignoringCase) { string = string.toLowerCase(Locale.getDefault()); }
            return string.endsWith(argument);
        }
    }

    final static class StringEquals extends Predicate {
        final static String IDENTIFIER = "equals";
        final String argument;
        public StringEquals(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            this.argument = ignoringCase
                    ? argument.toLowerCase(Locale.getDefault())
                    : argument;
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            String string = (String) item;
            if (ignoringCase) { string = string.toLowerCase(Locale.getDefault()); }
            return string.equals(argument);
        }
    }

    final static class StringNotEquals extends Predicate {
        final static String IDENTIFIER = "notEquals";
        final String argument;
        public StringNotEquals(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            this.argument = ignoringCase
                    ? argument.toLowerCase(Locale.getDefault())
                    : argument;
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            String string = (String) item;
            if (ignoringCase) { string = string.toLowerCase(Locale.getDefault()); }
            return !string.equals(argument);
        }
    }

    final static class StringRegex extends Predicate {
        final static String IDENTIFIER = "regex";
        final Pattern pattern;
        public StringRegex(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            final int flags = ignoringCase ? Pattern.CASE_INSENSITIVE : 0;
            pattern = Pattern.compile(argument, flags);
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            return pattern.matcher((CharSequence) item).matches();
        }
    }

    final static class StringStartsWith extends Predicate {
        final static String IDENTIFIER = "startsWith";
        final String argument;
        public StringStartsWith(String argument, boolean ignoringCase) {
            super(STRING_STARTS_WITH, IDENTIFIER, argument, ignoringCase);
            this.argument = ignoringCase
                    ? argument.toLowerCase(Locale.getDefault())
                    : argument;
        }
        public boolean matches(Object item) {
            if (!(item instanceof String)) { return false; }
            String string = (String) item;
            if (ignoringCase) { string = string.toLowerCase(Locale.getDefault()); }
            return string.startsWith(argument);
        }
    }

    static String STRING_CONTAINS, STRING_ENDS_WITH, STRING_EQUALS,
            STRING_NOT_EQUALS, STRING_REGEX, STRING_STARTS_WITH;

    static { NLSUtils.internationalize(Predicate.class); }

    private final String argument;

    private final String displayName;

    private final String identifier;

    protected final boolean ignoringCase;

    private Predicate(String displayName, String identifier, String argument, boolean ignoringCase) {
        Validate.notNull(argument);
        Validate.notNull(displayName);
        Validate.notNull(identifier);
        this.argument = argument;
        this.displayName = displayName;
        this.identifier = identifier;
        this.ignoringCase = ignoringCase;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Predicate other = (Predicate) obj;
        if (!argument.equals(other.argument)) { return false; }
        if (!identifier.equals(other.identifier)) { return false; }
        if (ignoringCase != other.ignoringCase) { return false; }
        return true;
    }

    public String getArgument() {
        return argument;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + argument.hashCode();
        result = PRIME * result + identifier.hashCode();
        result = PRIME * result + 1231;
        return result;
    }

    public boolean isIgnoringCase() {
        return ignoringCase;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("identifier", identifier)
                .append("argument", argument)
                .append("ignoringCase", ignoringCase).toString();
    }

}
