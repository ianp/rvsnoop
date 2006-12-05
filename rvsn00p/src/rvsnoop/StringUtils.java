//:File:    StringUtils.java
//:Created: Nov 25, 2005
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of static utility methods.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class StringUtils {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

    //private static final Logger logger = Logger.getLogger(StringUtils.class);

    private static final Object[] NO_FIELDS = new Object[0];

    private static final String[] NO_LINES = new String[0];

    //private static final Pattern SPLIT_LINES = Pattern.compile("[\r|\r\n|\n]", Pattern.MULTILINE | Pattern.UNIX_LINES); //$NON-NLS-1$

    private static final Pattern WHITESPACE = Pattern.compile("\\p{Space}"); //$NON-NLS-1$

    public static String format(String message, Object[] fields) {
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
     * Replace all runs of whitespace in a given character sequence with a
     * single space.
     * <p>
     * For example, the sequence "space-space-A-B-C-tab-9-8" will be converted
     * to "space-A-B-C-space-9-8".
     *
     * @param string The string to normalize.
     * @return The normalized string.
     */
    public static String normalizeWhitespace(CharSequence string) {
        if (string == null) return null;
        return WHITESPACE.matcher(string).replaceAll(" "); //$NON-NLS-1$
    }

    /**
     * Remove all whitespace from a given character sequence.
     *
     * @param string The string to remove whitespace from.
     * @return The string with all whitespace removed.
     */
    public static String removeWhitespace(CharSequence string) {
        if (string == null) return null;
        return WHITESPACE.matcher(string).replaceAll(""); //$NON-NLS-1$
    }

    /**
     * Splice a parameter list into a string.
     * <p>
     * This will splice the values from <code>params</code> into
     * <code>string</code> with each occurrence of <code>pattern</code>
     * being replaced by the corresponding parameter.
     * <p>
     * For example, given the string
     * <code>select foo from bar where foo.id = ? and foo.baz = ?</code> and
     * the pattern <code>\\?</code> (note the double-escaping of the
     * backslash) and the params array <code>{ 27, 35, 78 }</code> this will
     * produce the string
     * <code>select foo from bar where foo.id = 27 and foo.baz = 35</code>.
     * The last parameter will be discarded.
     *
     * @param string The string to splice into.
     * @param pattern The pattern used to denote splice targets.
     * @param params The values to splice into the string.
     * @return The spliced string.
     */
    public static String splice(String string, String pattern, Object[] params) {
        if (params == null) return string;
        final Pattern p = Pattern.compile(pattern);
        final Matcher m = p.matcher(string);
        final StringBuffer buf = new StringBuffer((int) (string.length() * 1.1f));
        int i = 0, imax = params.length;
        while (m.find() && i < imax)
            m.appendReplacement(buf, params[i++].toString());
        m.appendTail(buf);
        return buf.toString();
    }

    /**
     * Split a string to fit within a given width.
     * <p>
     * The lines returned by this will all be created using
     * {@link String#substring(int, int)} and so will use the same backing
     * storage as the input string.
     *
     * @param string The string to split.
     * @param maxLength The maximum width of any single line.
     * @param font The font to use to calculate line widths.
     * @param frc The rendering context to use to calculate line widths.
     * @return The individual lines that make up the string.
     */
    public static String[] split(String string, double maxLength, Font font, FontRenderContext frc) {
        Rectangle2D bounds = font.getStringBounds(string, frc);
        final char[] chars = string.toCharArray();
        final List lines = new ArrayList();
        int pos = chars.length - 1;
        while (bounds.getWidth() > maxLength) {
            bounds = font.getStringBounds(chars, 0, --pos, frc);
            if (bounds.getWidth() < maxLength) {
                lines.add(string.substring(0, pos));
                string = string.substring(pos);
                bounds = font.getStringBounds(string, frc);
            }
        }
        lines.add(string);
        return (String[]) lines.toArray(new String[lines.size()]);
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
