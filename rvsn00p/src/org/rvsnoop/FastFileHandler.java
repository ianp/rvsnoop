/*
 * Class:     FastFileHandler
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * A <code>java.util.logging</code> handler to write to files.
 * <p>
 * This is similar to {@link java.util.logging.FileHandler} class but provides
 * some enhanced features and some simplifications.
 * File handling can be configured using the following options:
 * <dl>
 * <dt>org.rvsnoop.FileHandler.maxFiles</dt>
 * <dd>Specifies the maximum number of files to keep, the default is to keep an
 * unlimited number of files, equivalent to setting 0 or a negative value.</dd>
 * <dt>org.rvsnoop.FileHandler.maxFileSize</dt>
 * <dd>The maximum file size, as soon as a log entry is written which makes the
 * file exceed the size here the log is rotated. Sizes are specified in bytes
 * and the suffixes <em>K</em> for kilobytes and <em>M</em> for megabytes
 * are also supported.</dd>
 * <dt>org.rvsnoop.FileHandler.level</dt>
 * <dd>The default level for the handler, the default is
 * {@link Level#INFO}.</dd>
 * <dt>org.rvsnoop.FileHandler.filter</dt>
 * <dd>The name of a {@link Filter} to use, no filter is used by default.</dd>
 * <dt>org.rvsnoop.FileHandler.formatter</dt>
 * <dd>The name of a {@link Formatter} to use, the default is to use a
 * {@link SimpleFormatter}.</dd>
 * <dt>org.rvsnoop.FileHandler.encoding</dt>
 * <dd>The character set encoding to use, the default is to use UTF-8.</dd>
 * <dt>org.rvsnoop.FileHandler.compressOldFiles</dt>
 * <dd>Should log files be compressed when they are closed, the default is
 * <code>false</code>.</dd>
 * </dl>
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public class FastFileHandler extends StreamHandler {

    /**
     * Compress an old log file on a background thread.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    private final class CompressFileTask implements Runnable {
        /** The file to compress. */
        private final File file;
        /** @param file The file to compress. */
        private CompressFileTask(File file) {
            this.file = file;
        }
        public void run() {
            final String basename = FilenameUtils.removeExtension(file
                    .getName());
            final File compressed = new File(logDirectory, basename + ".gz");
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(file);
                out = new GZIPOutputStream(new FileOutputStream(compressed));
                IOUtils.copy(in, out);
                in.close();
                out.close();
            } catch (IOException e) {
                reportError("Error compressing olg log file after file rotation",
                        e, ErrorManager.GENERIC_FAILURE);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
            Collections.replaceAll(files, file, compressed);
        }
    }

    /**
     * Keep track of how many bytes have been written to a stream.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    private final class CountedOutputStream extends OutputStream {
        private int count;
        private OutputStream stream;
        CountedOutputStream(OutputStream out) {
            this.stream = out;
        }
        public void close() throws IOException {
            stream.close();
        }
        public void flush() throws IOException {
            stream.flush();
        }
        int getCount() {
            return count;
        }
        public void write(byte buff[]) throws IOException {
            stream.write(buff);
            count += buff.length;
        }
        public void write(byte buff[], int off, int len) throws IOException {
            stream.write(buff, off, len);
            count += len;
        }
        public void write(int b) throws IOException {
            stream.write(b);
            count++;
        }
    }

    private final class FileRotator implements PrivilegedAction {

        /* (non-Javadoc)
         * @see java.security.PrivilegedAction#run()
         */
        public Object run() {
            rotate();
            return null;
        }
        
    }

    private static final Level DEFAULT_LEVEL = Level.INFO;

    private static final MessageFormat fileNamePattern =
        new MessageFormat("rvsnoop-{0,time,yyyyMMddHHmmss}.log");
    
    private static final File logDirectory;
    
    private static final int MIN_FILE_SIZE = 64 * 1024;

    static {
        String path = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            path = SystemUtils.USER_HOME + "/Application Data/Logs/RvSnoop";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            path = SystemUtils.USER_HOME + "/Library/Logs/RvSnoop";
        } else {
            path = SystemUtils.USER_HOME + "/.rvsnoop";
        }
        logDirectory = new File(path);
        try {
            FileUtils.forceMkdir(logDirectory);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private boolean compressOldFiles;
    private LinkedList files = new LinkedList();
    private int maxFiles;
    private int maxFileSize;

    private CountedOutputStream output;

    /**
     * Create a new <code>FastFileHandler</code>.
     * <p>
     * The new handler will be configured entirely from {@link LogManager}
     * properties (or their default values).
     *
     * @exception IOException if there are IO problems opening the files.
     */
    public FastFileHandler() throws IOException, SecurityException {
        final LogManager manager = LogManager.getLogManager();
        final String cname = FastFileHandler.class.getName();
        initMaxFiles(manager, cname);
        initMaxFileSize(manager, cname);
        initLevel(manager, cname);
        initFilter(manager, cname);
        initFormatter(manager, cname);
        initEncoding(manager, cname);
        initCompressOldFiles(manager, cname);
        rotate();
    }

    /**
     * Compress the log file.
     * <p>
     * This also changes the extension from <em>log</em> to <em>gz</em>. The
     * compression is handled on a background thread with low priority.
     *
     * @param file The file to compress.
     */
    private void compress(final File file) {
        final Thread thread = new Thread(new CompressFileTask(file));
        final int priority = (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2;
        thread.setPriority(priority);
        thread.start();
    }

    private File getLogFile() {
        final Date now = new Date();
        final StringBuffer result = new StringBuffer();
        fileNamePattern.format(new Date[] { now }, result, null);
        return new File(logDirectory, result.toString());
    }

    private void initCompressOldFiles(final LogManager manager, final String cname) {
        try {
            String s = manager.getProperty(cname + ".compressOldFiles");
            compressOldFiles = BooleanUtils.toBoolean(s);
        } catch (Exception e) {
            // Do not compress by default.
        }
    }

    private void initEncoding(final LogManager manager, final String cname) {
        try {
            setEncoding(manager.getProperty(cname + ".encoding"));
        } catch (Exception e) {
            try {
                setEncoding(null);
            } catch (Exception shouldNeverHappen) {
                assert false : "Calling setEncoding(null) should never fail!";
            }
        }
    }

    private void initFilter(final LogManager manager, final String cname) {
        try {
            Class clazz = Class.forName(manager.getProperty(cname + ".filter"));
            setFilter((Filter) clazz.newInstance());
        } catch (Exception e) {
            // Do not set a filter by default.
        }
    }

    private void initFormatter(final LogManager manager, final String cname) {
        Formatter formatter = null;
        try {
            Class clazz = Class.forName(manager.getProperty(cname + ".formatter"));
            formatter = (Formatter) clazz.newInstance();
        } catch (Exception e) {
            formatter = new SimpleFormatter();
        }
        setFormatter(formatter);
    }

    private void initLevel(final LogManager manager, final String cname) {
        try {
            setLevel(Level.parse(manager.getProperty(cname + ".level")));
        } catch (Exception e) {
            setLevel(DEFAULT_LEVEL);
        }
    }

    private void initMaxFiles(final LogManager manager, final String cname) {
        try {
            final String propname = cname + ".maxFiles";
            final int maxFiles = Integer.parseInt(manager.getProperty(propname));
            if (maxFiles > 0) { this.maxFiles = maxFiles; }
        } catch (Exception e) {
            // Use the default value, 0.
        }
    }

    private void initMaxFileSize(final LogManager manager, final String cname) {
        int maxFileSize = 0;
        try {
            final String propname = cname + ".maxFileSize";
            final String s =
                manager.getProperty(propname).trim().toUpperCase(Locale.ENGLISH);
            final char suffix = s.charAt(s.length() - 1);
            if (suffix == 'K') {
                maxFileSize = Integer.parseInt(s.substring(0, s.length() - 1));
                maxFileSize *= 1024;
            } else if (suffix == 'M') {
                maxFileSize = Integer.parseInt(s.substring(0, s.length() - 1));
                maxFileSize *= 1024 * 1024;
            } else {
                maxFileSize = Integer.parseInt(s);
            }
        } catch (Exception e) {
            // Use the default value, 0.
        }
        // If maxFileSize is negative, stick with the default.
        if (maxFileSize > 0) {
            this.maxFileSize = Math.max(maxFileSize, MIN_FILE_SIZE);
        }
    }
    
    private void open(File file) throws IOException {
        BufferedOutputStream stream =
            new BufferedOutputStream(new FileOutputStream(file.toString()));
        output = new CountedOutputStream(stream);
        setOutputStream(output);
        files.add(file);
    }

    /**
     * Format and publish a {@link LogRecord}.
     * 
     * @param record The record describing the logged event.
     */
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) { return; }
        super.publish(record);
        flush();
        if (maxFileSize > 0 && output.getCount() >= maxFileSize) {
            AccessController.doPrivileged(new FileRotator());
        }
    }

    /** Rotate the log files. */
    private synchronized void rotate() {
        // Temporarily disable logging while the files are rotated. This may not
        // be necessary since both methods are synchronized, but the JDK version
        // of this class does it so there may be a need for it.
        final Level oldLevel = getLevel();
        setLevel(Level.OFF);
        super.close();
        final File newLogFile = getLogFile();
        try {
            open(newLogFile);
            if (files.size() > maxFiles) {
                final File toRemove = (File) files.removeFirst();
                if (!toRemove.delete()) { toRemove.deleteOnExit(); }
            }
            if (compressOldFiles && files.size() > 1) {
                compress((File) files.get(files.size() - 2));
            }
        } catch (IOException e) {
            reportError("Error opening new log file after file rotation",
                    e, ErrorManager.OPEN_FAILURE);
        }
        setLevel(oldLevel);
    }

}
