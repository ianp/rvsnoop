/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util;

public class HTMLEncoder {

    public static final String AMP_HTML_STRING = "&amp;";
    public static final String GT_HTML_STRING = "&gt;";
    public static final String LT_HTML_STRING = "&lt;";
    public static final String BR_HTML_STRING = "<BR>";

    public static final char AMP_CHAR = '&';
    public static final char BR_CHAR = '\n';
    public static final char GT_CHAR = '>';
    public static final char LT_CHAR = '<';


    public static String encodeString(String sEncode) {
        if (sEncode == null) {
            return null;
        }
        char[] c = sEncode.toCharArray();
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < c.length; ++i) {
            String enc = encodeChar(c[i]);
            if (enc != null) {
                out.append(enc);
            } else {
                out.append(c[i]);
            }
        }
        return out.toString();
    }

    protected static String encodeChar(char c) {
        switch (c) {
            case AMP_CHAR:
                return AMP_HTML_STRING;
            case GT_CHAR:
                return GT_HTML_STRING;
            case LT_CHAR:
                return LT_HTML_STRING;
            case BR_CHAR:
                return BR_HTML_STRING;
            default:
                return null;
        }

    }
}
