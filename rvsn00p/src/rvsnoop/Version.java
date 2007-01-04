/*
 * Class:     Version
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
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

    private static final boolean ALPHA =
        Boolean.valueOf("@version.alpha@").booleanValue();

    private static final boolean BETA =
        Boolean.valueOf("@version.beta@").booleanValue();

    private static final String DATE = "@build.date@";

    private static final int MAJOR = Integer.parseInt("@version.major@");

    private static final int MINOR = Integer.parseInt("@version.minor@");

    private static final int PATCH = Integer.parseInt("@version.patch@");

    private static final int BUILD_NUMBER = Integer.parseInt("@build.number@");

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

    public static int getBuildNumber() {
        return BUILD_NUMBER;
    }

    private static String getBuildType() {
        return (ALPHA ? " α" : "") + (BETA ? " β" : "");
    }

    public static String getAsString() {
        return "@version@" + getBuildType();
    }

    public static String getAsStringWithName() {
        return "RvSnoop @version@" + getBuildType();
    }

    public static String getAsStringWithNameAndBuildNumber() {
        return "RvSnoop @version@ (Build @build.number@" + getBuildType() + ')';
    }

    public static boolean isFinal() {
        return !ALPHA && !BETA;
    }

    /** Private constructor. Do not instantiate. */
    private Version() {
        throw new UnsupportedOperationException();
    }

}

