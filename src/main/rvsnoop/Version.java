/*
 * Class:     Version
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Version information.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Version {

    private static final Properties props = new Properties();

    static {
        InputStream stream = null;
        try {
            stream = ClassLoader.getSystemResourceAsStream("META-INF/build.properties");
            props.load(stream);
        } catch (IOException e) {
            ExceptionInInitializerError error =  new ExceptionInInitializerError("Error loading build properties from system classpath.");
            error.initCause(e);
            throw error;
        } finally {
            closeQuietly(stream);
        }
    }

    public static Date getBuildDate() {
        try {
            final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return format.parse(props.getProperty("build.date"));
        } catch (ParseException e) {
            return new Date(0L);
        }
    }

    public static int getMajor() {
        return Integer.parseInt(getAsString().split("\\.")[0]);
    }

    public static int getMinor() {
        return Integer.parseInt(getAsString().split("\\.")[1]);
    }

    public static int getPatch() {
        return Integer.parseInt(getAsString().split("\\.")[2]);
    }

    public static String getAsString() {
        return props.getProperty("build.version", "0.0.0");
    }

    public static String getAsStringWithName() {
        return "RvSnoop " + getAsString();
    }

    /** Private constructor. Do not instantiate. */
    private Version() {
        throw new UnsupportedOperationException();
    }

}

