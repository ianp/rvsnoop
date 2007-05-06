/*
 * Class:     NLSUtilsTest
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

/**
 * Unit tests for {@link NLSUtils}.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class NLSUtilsTest extends TestCase {

    /**
     * Test method for {@link org.ianp.util.NLSUtils#findNLSResource(java.lang.String, java.lang.Class)}.
     * @throws Exception
     */
    public final void testFindNLSResource() {
        try {
            InputStream stream = NLSUtils.findNLSResource("/org/rvsnoop/NLSUtilsSample.txt", NLSUtilsTest.class).openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = reader.readLine();
            assertEquals("This is sample text for the NLSUtils test cases.", line);
            stream.close();
        } catch (IOException e) {
            fail("I/O Exception: " + e.getMessage());
        }
    }

    /**
     * Test method for {@link org.ianp.util.NLSUtils#internationalize(java.lang.Class)}.
     */
    public final void testInternationalizeClass() {
        NLSUtilsTestTarget tt = new NLSUtilsTestTarget();
        assertEquals("I18N_STRING_1", tt.getFirstString());
        assertEquals("I18N_STRING_2", tt.getSecondString());
        assertEquals("I18N_STRING_3", tt.getThirdString());
    }

}
