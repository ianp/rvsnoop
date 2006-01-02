//:File:    Version.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

/**
 * Version information.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Version {

    public static int getMajor() {
        return 1;
    }
    
    public static int getMinor() {
        return 4;
    }
    
    public static int getPatch() {
        return 0;
    }
    
    public static String getAsString() {
        // The funny-looking version string will be substituted with
        // the correct value as part of the Ant build.
        return "@version@";
    }
    
    public static String getAsStringWithName() {
        // The funny-looking version string will be substituted with
        // the correct value as part of the Ant build.
        return "RvSn00p @version@";
    }
    
    /**
     * Do not instantiate.
     */
    private Version() {
        throw new UnsupportedOperationException();
    }

}
