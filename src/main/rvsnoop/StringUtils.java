// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * A collection of static utility methods.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class StringUtils {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

    private static final Object[] NO_FIELDS = new Object[0];

    private static final String[] NO_LINES = new String[0];

    public static String format(String message, Object... fields) {
        fields = fields != null ? fields : NO_FIELDS;
        for (int i = 0, size = fields.length; i < size; ++i)
            if (fields[i] == null)
                fields[i] = "null"; // $NON-NLS-1$
        if (message == null) {
            final StringBuffer buffer = new StringBuffer();
            buffer.append("Null pattern passed to format! Fields: { ");
            for (int i = 0, size = fields.length; i < size; ++i)
                buffer.append('\'').append(fields[i]).append("', ");
            buffer.setLength(buffer.length() - 2);
            return buffer.append(" }").toString();
        }
        return MessageFormat.format(message, fields);
    }

    /**
     * Format a date using the current pattern setting.
     *
     * @param date The date to format.
     * @return The formatted date.
     */
    public static String format(Date date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * Split a string into multiple lines (on newlines).
     *
     * @param string The string to split.
     * @return The split lines.
     */
    public static String[] split(String string) {
        if (string == null || string.length() == 0) return NO_LINES; //NOPMD NO_LINES is zero length.
        final StringTokenizer stok = new StringTokenizer(string, "\n");
        final String[] lines = new String[stok.countTokens()];
        int i = 0;
        while(stok.hasMoreTokens())
            lines[i++] = stok.nextToken();
        return lines;
    }

    /**
     * Do not instantiate.
     */
    private StringUtils() {
        throw new UnsupportedOperationException();
    }

}
