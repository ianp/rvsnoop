/*
 * Class:     NLSUtilsTestTarget
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

/**
 * A class to test I18N.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class NLSUtilsTestTarget {

    static String STRING_1;
    static String STRING_2;
    static String STRING_3;

    static { NLSUtils.internationalize(NLSUtilsTestTarget.class); }

    public String getFirstString()  { return STRING_1; }
    public String getSecondString() { return STRING_2; }
    public String getThirdString()  { return STRING_3; }

}
