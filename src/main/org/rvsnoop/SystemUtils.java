// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SystemUtils {

    public static final boolean IS_OS_MAC;
    public static final boolean IS_OS_WINDOWS;

    public static final String LINE_SEPARATOR;

    static {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        IS_OS_MAC = os.startsWith("mac");
        IS_OS_WINDOWS = os.startsWith("win");
        LINE_SEPARATOR = IS_OS_WINDOWS ? "\r\n" : "\n";
    }

    private SystemUtils() {}

    public static boolean isJavaVersionAtLeast(int major, int minor) {
        try {
            String[] parts = Pattern.compile("^(\\d+)\\.(\\d+)").split(System.getProperty("java.version"));
            return Integer.parseInt(parts[0]) <= major && Integer.parseInt(parts[1]) <= minor;
        } catch (Exception e) {
            // be optimistic
            return true;
        }
    }
}
