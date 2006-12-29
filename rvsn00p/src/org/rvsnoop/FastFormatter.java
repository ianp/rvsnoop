/*
 * Class:     FastFormatter
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrBuilder;

/**
 * A simple formatter that is both faster and less memory intensive than the
 * supplied {@link java.util.logging.SimpleFormatter}.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class FastFormatter extends Formatter {
    
    private static class StrBuilderWriter extends Writer {
        private final StrBuilder builder;
        StrBuilderWriter(StrBuilder builder) {
            this.builder = builder;
            lock = builder;
        }
        public void close() throws IOException { /* NO-OP */ }
        public void flush() throws IOException { /* NO-OP */ }
        public void write(char[] cbuf, int off, int len) throws IOException {
            builder.append(cbuf, off, len);
        }
        public void write(char[] cbuf) throws IOException {
            builder.append(cbuf);
        }
        public void write(int c) throws IOException {
            builder.append(c);
        }
        public void write(String str, int off, int len) throws IOException {
            builder.append(str, off, len);
        }
        public void write(String str) throws IOException {
            builder.append(str);
        }
    }

    private final StrBuilder builder = new StrBuilder();
    private final Calendar calendar = Calendar.getInstance();

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public synchronized String format(LogRecord record) {
        calendar.setTimeInMillis(record.getMillis());
        builder.append(SystemUtils.LINE_SEPARATOR)
            .append(calendar.get(Calendar.YEAR)).append('-')
            .appendFixedWidthPadLeft(calendar.get(Calendar.MONTH), 2, '0').append('-')
            .appendFixedWidthPadLeft(calendar.get(Calendar.DAY_OF_MONTH), 2, '0').append(' ')
            .appendFixedWidthPadLeft(calendar.get(Calendar.HOUR_OF_DAY), 2, '0').append(':')
            .appendFixedWidthPadLeft(calendar.get(Calendar.MINUTE), 2, '0').append(':')
            .appendFixedWidthPadLeft(calendar.get(Calendar.SECOND), 2, '0').append(',')
            .appendFixedWidthPadLeft(calendar.get(Calendar.MILLISECOND), 3, '0').append(' ');
        builder.append(record.getLevel().getName()).append(' ');
        final String logger = record.getLoggerName();
        builder.append(logger != null ? logger : "unknown_logger").append(' ');
        final String message = record.getMessage();
        if (message != null) { builder.append(message); }
        final Throwable thrown = record.getThrown();
        if (thrown != null) {
            // Use an auto-flushing writer that appends directly to the builder,
            // so no need to worry about cleaning up or catching IOExceptions.
            thrown.printStackTrace(new PrintWriter(new StrBuilderWriter(builder), true));
        }
        final String formatted = builder.toString();
        builder.setLength(0);
        return formatted;
    }

}
