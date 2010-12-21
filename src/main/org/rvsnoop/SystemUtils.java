// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;

import java.util.regex.Pattern;

public final class SystemUtils {

    public static final String LINE_SEPARATOR;

    static {
        LINE_SEPARATOR = AppHelper.getPlatform() == PlatformType.WINDOWS ? "\r\n" : "\n";
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
