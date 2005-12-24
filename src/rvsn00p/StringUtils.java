//:File:    StringUtils.java
//:Created: Nov 25, 2005
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of static utility methods.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class StringUtils {

    private static final Object[] NO_FIELDS = new Object[0];
    
    private static final Pattern WHITESPACE = Pattern.compile("\\p{Space}"); //$NON-NLS-1$

    public static String format(String message, Object[] fields) {
        fields = fields != null ? fields : NO_FIELDS;
        for (int i = 0, size = fields.length; i < size; ++i)
            if (fields[i] == null)
                fields[i] = "null"; // $NON-NLS-1$
        return MessageFormat.format(message, fields);
    }

    /**
     * Replace all runs of whitespace in a given character sequence with a
     * single space.
     * <p>
     * For example, the sequence "space-space-A-B-C-tab-9-8" will be converted
     * to "space-A-B-C-space-9-8".
     * 
     * @param string
     * @return
     */
    public static String normalizeWhitespace(CharSequence string) {
        if (string == null) return null;
        return WHITESPACE.matcher(string).replaceAll(" "); //$NON-NLS-1$
    }

    /**
     * Remove all whitespace from a given character sequence.
     * 
     * @param string
     * @return
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
     * @param string
     * @param patter
     * @param objects
     * @return
     */
    public static String splice(String string, String pattern, Object[] params) {
        if (params == null) return string;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        StringBuffer buf = new StringBuffer((int) (string.length() * 1.1f));
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
     * @return
     */
    public static String[] split(String string, double maxLength, Font font, FontRenderContext frc) {
        Rectangle2D bounds = font.getStringBounds(string, frc);
        char[] chars = string.toCharArray();
        ArrayList lines = new ArrayList();
        int offset = 0, pos = chars.length - 1;
        while (bounds.getWidth() > maxLength) {
            bounds = font.getStringBounds(chars, offset, --pos, frc);
            if (bounds.getWidth() < maxLength) {
                lines.add(string.substring(0, pos));
                string = string.substring(pos);
                bounds = font.getStringBounds(string, frc);
            }
        }
        lines.add(string);
        return (String[]) lines.toArray(new String[lines.size()]);
    }

    private StringUtils() {
        // Do not instantiate.
    }

}
