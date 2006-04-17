//:File:    Version.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

/**
 * Version information.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Version {

    // The funny-looking version strings will be substituted with
    // the correct value as part of the Ant build. Parsing string
    // representations of integers is a bit of a hack, but if you
    // work in an IDE having the Ant filter tags in their raw form
    // just won't cut it, so this is a (lame) work-around.

    private static final String DATE = "@build.date@";

    private static final int MAJOR = Integer.parseInt("@version.major@");

    private static final int MINOR = Integer.parseInt("@version.minor@");

    private static final int PATCH = Integer.parseInt("@version.patch@");

    public static String getDateString() {
        return DATE;
    }

    public static int getMajor() {
        return MAJOR;
    }

    public static int getMinor() {
        return MINOR;
    }

    public static int getPatch() {
        return PATCH;
    }

    public static String getAsString() {
        return "@version@";
    }

    public static String getAsStringWithName() {
        return "rvSnoop @version@";
    }

    /**
     * Do not instantiate.
     */
    private Version() {
        throw new UnsupportedOperationException();
    }

}
