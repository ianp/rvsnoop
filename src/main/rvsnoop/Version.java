// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Version information.
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

