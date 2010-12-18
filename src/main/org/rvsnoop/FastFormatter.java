// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple formatter that is both faster and less memory intensive than the
 * supplied {@link java.util.logging.SimpleFormatter}.
 */
public final class FastFormatter extends Formatter {

    private static Appendable padLeft(int value, int width, Appendable appendable) throws IOException {
        String s = Integer.toString(value);
        for (int i = s.length(); i < width; ++i) {
            appendable.append('0');
        }
        return appendable.append(s);
    }

    private final StringWriter writer = new StringWriter();
    private final Calendar calendar = Calendar.getInstance();

    @Override
    public synchronized String format(LogRecord record) {
        try {
            calendar.setTimeInMillis(record.getMillis());
            writer.append(SystemUtils.LINE_SEPARATOR);
            padLeft(calendar.get(Calendar.YEAR), 4, writer).append('-');
            padLeft(calendar.get(Calendar.MONTH), 2, writer).append('-');
            padLeft(calendar.get(Calendar.DAY_OF_MONTH), 2, writer).append(' ');
            padLeft(calendar.get(Calendar.HOUR_OF_DAY), 2, writer).append(':');
            padLeft(calendar.get(Calendar.MINUTE), 2, writer).append(':');
            padLeft(calendar.get(Calendar.SECOND), 2, writer).append(',');
            padLeft(calendar.get(Calendar.MILLISECOND), 3, writer).append(' ');
            writer.append(record.getLevel().getName()).append(' ');
            final String logger = record.getLoggerName();
            writer.append(logger != null ? logger : "unknown_logger").append(' ');
            final String message = record.getMessage();
            if (message != null) { writer.append(message); }
            final Throwable thrown = record.getThrown();
            if (thrown != null) {
                writer.append(SystemUtils.LINE_SEPARATOR);
                // Use an auto-flushing writer that appends directly to the builder,
                // so no need to worry about cleaning up or catching IOExceptions.
                thrown.printStackTrace(new PrintWriter(writer, true));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String formatted = writer.toString();
        writer.getBuffer().setLength(0);
        return formatted;
    }

}
