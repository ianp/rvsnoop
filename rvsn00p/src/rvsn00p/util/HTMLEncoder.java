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
    public static final String NON_BREAKING_SPACE_HTML_STRING = "&nbsp;";

    public static final char AMP_CHAR = '&';
    public static final char BR_CHAR = '\n';
    public static final char GT_CHAR = '>';
    public static final char LT_CHAR = '<';
    public static final char NON_BREAKING_CHAR = ' ';


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

     public static void encodeStringBuffer(StringBuffer sEncode) {
        if (sEncode == null) {
            return;
        }
        for (int i = 0; i < sEncode.length(); ++i) {
            String enc = encodeChar(sEncode.charAt(i) );
            if (enc != null) {
                sEncode.replace(i,i+1,enc);
                i+=enc.length();
                --i;
            }
        }
    }

    protected static String encodeChar(char c) {
        switch (c) {
            case NON_BREAKING_CHAR:
                return NON_BREAKING_SPACE_HTML_STRING;
            case GT_CHAR:
                return GT_HTML_STRING;
            case LT_CHAR:
                return LT_HTML_STRING;
            case AMP_CHAR:
                return AMP_HTML_STRING;
            case BR_CHAR:
                return BR_HTML_STRING;
            default:
                return null;
        }

    }
}
