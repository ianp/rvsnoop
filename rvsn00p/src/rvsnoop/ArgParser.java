//:File:    ArgParser.java
//:Created: Jan 12, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple parser for <acronym>GNU</acronym> style argument lists.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ArgParser {

    private static class Argument {
        final String description;
        final boolean isBoolean;
        final String longForm;
        final char shortForm;
        Object value;
        Argument(char shortForm, String longForm, boolean isBoolean, String description) {
            super();
            this.shortForm = shortForm;
            this.longForm = longForm;
            this.isBoolean = isBoolean;
            this.description = description;
            if (isBoolean) value = Boolean.FALSE;
        }
    }

    private static final String INDENT = "  ";

    private final String appName;

    private final Map args = new LinkedHashMap();

    private int longestArg;

    public ArgParser(String appName) {
        super();
        this.appName = appName;
    }

    public void addArgument(char s, String l, boolean isBoolean, String description) {
        if (s == '-') throw new IllegalArgumentException("Unsupported short argument.");
        final Argument a = new Argument(s, l, isBoolean, description);
        int length = 1; // space for the trailing colon.
        if (s != 0) {
            args.put(new Character(s), a);
            length += 2; // initial hyphen and argument letter.
            if (isBoolean) length += 7; // "<value>" string.
            if (l != null) length += 2; // comma and space between args.
        }
        if (l != null) {
            args.put(l, a);
            length += 2 + l.length(); // initial hyphens and argument.
            if (isBoolean) length += 8; // "=<value>" string.
        }
        longestArg = Math.max(length, longestArg);
    }

    public boolean getBooleanArg(String name) {
        return ((Boolean) ((Argument) args.get(name)).value).booleanValue();
    }

    public String getStringArg(String name) {
        return (String) ((Argument) args.get(name)).value;
    }

    private void pad(StringBuffer buffer) {
        final int indents = longestArg - INDENT.length() - buffer.length();
        for (int i = 0; i < indents; ++i) buffer.append(" ");
    }

    public void parseArgs(String[] args) {
        for (int i = 0, imax = args.length; i < imax; ++i) {
            final String arg = args[i];
            if (arg.startsWith("--"))
                parseLongArg(arg);
            else if (arg.charAt(0) == '-')
                parseShortArg(arg);
        }
    }

    private void parseLongArg(String arg) {
        final int index = arg.indexOf('=');
        final String v = index == -1 ? null : arg.substring(index + 1);
        arg = index == -1 ? arg.substring(2) : arg.substring(2, index);
        final Argument argument = (Argument) args.get(arg);
        if (argument == null) return;
        if (argument.isBoolean)
            argument.value = Boolean.TRUE;
        else
            argument.value = v;
    }

    private void parseShortArg(String arg) {
        final Argument argument = (Argument) args.get(new Character(arg.charAt(1)));
        if (argument == null) return;
        if (argument.isBoolean)
            argument.value = Boolean.TRUE;
        else
            argument.value = arg.substring(2);
    }

    public void printUsage(PrintStream out) {
        final StringBuffer buffer = new StringBuffer();
        out.println(appName);
        out.println();
        Argument prev = null;
        for (final Iterator i = args.values().iterator(); i.hasNext(); ) {
            final Argument next = (Argument) i.next();
            if (prev == null || !prev.equals(next)) {
                buffer.setLength(0);
                buffer.append(INDENT);
                if (next.shortForm != 0) {
                    buffer.append("-").append(next.shortForm);
                    if (!next.isBoolean) buffer.append("<value>");
                    if (next.longForm != null) buffer.append(", ");
                }
                if (next.longForm != null) {
                    buffer.append("--").append(next.longForm);
                    if (!next.isBoolean) buffer.append("=<value>");
                }
                pad(buffer);
                buffer.append(": ").append(next.description);
                out.println(buffer.toString());
            }
            prev = next;
        }
    }
}
