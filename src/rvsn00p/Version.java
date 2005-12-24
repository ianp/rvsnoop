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
 */
public final class Version {

    public static int getMajor() {
        return 1;
    }
    
    public static int getMinor() {
        return 3;
    }
    
    public static int getPatch() {
        return 0;
    }
    
    public static String getAsString() {
        return "1.3.0";
    }
    
    private Version() {
        // Do not instantiate.
    }

}
