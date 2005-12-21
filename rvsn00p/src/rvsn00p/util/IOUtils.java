//:File:    IOUtils.java
//:Created: Nov 29, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A collection of static utility methods for working with the <code>java.io</code> package.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class IOUtils {

    public static void closeQuietly(InputStream stream) {
        if (stream == null)
            return;
        try {
            stream.close();
        } catch (IOException ignored) {
            // Intentionally ignored.
        }
    }
    
    public static void closeQuietly(OutputStream stream) {
        if (stream == null)
            return;
        try {
            stream.close();
        } catch (IOException ignored) {
            // Intentionally ignored.
        }
    }
    
    public static void closeQuietly(Reader reader) {
        if (reader == null)
            return;
        try {
            reader.close();
        } catch (IOException ignored) {
            // Intentionally ignored.
        }
    }
    
    public static void closeQuietly(Writer writer) {
        if (writer == null)
            return;
        try {
            writer.close();
        } catch (IOException ignored) {
            // Intentionally ignored.
        }
    }
    
    private IOUtils() {
        super();
    }

}
