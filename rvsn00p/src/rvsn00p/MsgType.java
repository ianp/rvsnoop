/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The MsgType class defines a set of standard logging levels.
 *
 * The msg type objects are ordered and are specified by ordered
 * integers. Enabling logging at a given level also enables logging at all
 * higher levels.
 *
 * @author Örjan Lundberg
 *
 * Based on Logfactor5 By
 *
 * @author Michael J. Sikorsky
 * @author Robert Shaw
 * @author Brent Sprecher
 * @author Richard Hurst
 * @author Brad Marlborough
 */

// Contributed by ThoughtWorks Inc.

public class MsgType implements java.io.Serializable {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    // messages.
    public final static MsgType ERROR = new MsgType("ERROR", 0);
    public final static MsgType WARN = new MsgType("WARN", 1);
    public final static MsgType SYSTEM = new MsgType("SYSTEM",2);
    public final static MsgType UNKNOWN = new MsgType("UNKNOWN",3);


    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected String _label;
    protected int _precedence;
    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private static MsgType[] _allDefaultLevels;
    private static Map _msgTypeMap;
    private static Map _msgTypeColorMap;
    private static Map _registeredMsgTypeMap = new HashMap();

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    static {
        _allDefaultLevels = new MsgType[]{ ERROR, WARN, SYSTEM, UNKNOWN};

        _msgTypeMap = new HashMap();
        for (int i = 0; i < _allDefaultLevels.length; ++i) {
            _msgTypeMap.put(_allDefaultLevels[i].getLabel(), _allDefaultLevels[i]);
        }

        // prepopulate map with levels and text color of black
        _msgTypeColorMap = new HashMap();
        for (int i = 0; i < _allDefaultLevels.length; ++i) {
            _msgTypeColorMap.put(_allDefaultLevels[i], Color.black);
        }
    }

    public MsgType(String label, int precedence) {
        _label = label;
        _precedence = precedence;
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    /**
     * Return the Label of the MsgType.
     */
    public String getLabel() {
        return _label;
    }

    /**
     * Returns true if the type supplied is encompassed by this level.
     * For example, MsgType.SEVERE encompasses no other MsgTypes and
     * MsgType.UNKNOWN encompasses all other MsgTypes.  By definition,
     * a MsgType encompasses itself.
     */
    public boolean encompasses(MsgType level) {
        if (level.getPrecedence() <= getPrecedence()) {
            return true;
        }

        return false;
    }

    /**
     * Convert a log level label into a MsgType object.
     *
     * @param level The label of a level to be converted into a MsgType.
     * @return MsgType The MsgType with a label equal to level.
     * @throws MsgTypeFormatException Is thrown when the level can not be
     *         converted into a MsgType.
     */
    public static MsgType valueOf(String level)
            throws MsgTypeFormatException {
        MsgType logLevel = null;
        if (level != null) {
            level = level.trim().toUpperCase();
            logLevel = (MsgType) _msgTypeMap.get(level);
        }

        // Didn't match, Check for registered MsgTypes
        if (logLevel == null && _registeredMsgTypeMap.size() > 0) {
            logLevel = (MsgType) _registeredMsgTypeMap.get(level);
        }

        if (logLevel == null) {
            StringBuffer buf = new StringBuffer();
            buf.append("Error while trying to parse (" + level + ") into");
            buf.append(" a MsgType.");
            throw new MsgTypeFormatException(buf.toString());
        }
        return logLevel;
    }

    /**
     * Registers a used defined MsgType.
     *
     * @param logLevel The log level to be registered. Cannot be a default MsgType
     * @return MsgType The replaced log level.
     */
    public static MsgType register(MsgType logLevel) {
        if (logLevel == null) return null;

        // ensure that this is not a default log level
        if (_msgTypeMap.get(logLevel.getLabel()) == null) {
            return (MsgType) _registeredMsgTypeMap.put(logLevel.getLabel(), logLevel);
        }

        return null;
    }

    public static void register(MsgType[] MsgTypes) {
        if (MsgTypes != null) {
            for (int i = 0; i < MsgTypes.length; ++i) {
                register(MsgTypes[i]);
            }
        }
    }

    public static void register(List MsgTypes) {
        if (MsgTypes != null) {
            Iterator it = MsgTypes.iterator();
            while (it.hasNext()) {
                register((MsgType) it.next());
            }
        }
    }

    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof MsgType) {
            if (this.getPrecedence() ==
                    ((MsgType) o).getPrecedence()) {
                equals = true;
            }

        }

        return equals;
    }

    public int hashCode() {
        return _label.hashCode();
    }

    public String toString() {
        return _label;
    }

    // set a text color for a specific log level
    public void setLogLevelColorMap(MsgType level, Color color) {
        // remove the old entry
        _msgTypeColorMap.remove(level);
        // add the new color entry
        if (color == null) {
            color = Color.black;
        }
        _msgTypeColorMap.put(level, color);
    }

    public static void resetLogLevelColorMap() {
        // empty the map
        _msgTypeColorMap.clear();

        // repopulate map and reset text color black
        for (int i = 0; i < _allDefaultLevels.length; ++i) {
            _msgTypeColorMap.put(_allDefaultLevels[i], Color.black);
        }
    }


    public static List getAllDefaultLevels() {
        return Arrays.asList(_allDefaultLevels);
    }

    public static Map getLogLevelColorMap() {
        return _msgTypeColorMap;
    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    protected int getPrecedence() {
        return _precedence;
    }

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}






