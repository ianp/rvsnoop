//:File:    MsgType.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The MsgType class defines a set of standard logging levels.
 * <p>
 * The msg type objects are ordered and are specified by ordered
 * integers. Enabling logging at a given level also enables logging at all
 * higher levels.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class MsgType implements Serializable {

    private static final long serialVersionUID = -5892612808722256638L;
    public final static MsgType ERROR = new MsgType("ERROR", 0);
    public final static MsgType WARN = new MsgType("WARN", 1);
    public final static MsgType SYSTEM = new MsgType("SYSTEM",2);
    public final static MsgType UNKNOWN = new MsgType("UNKNOWN",3);

    protected String _label;
    protected int _precedence;

    private static MsgType[] _allDefaultLevels;
    private static Map _msgTypeMap;
    private static Map _msgTypeColorMap;
    private static Map _registeredMsgTypeMap = new HashMap();

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
        super();
        _label = label;
        _precedence = precedence;
    }

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
            final StringBuffer buf = new StringBuffer();
            buf.append("Error while trying to parse (").append(level).append(") into");
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

    public static void register(List types) {
        if (types != null)
            for (final Iterator i = types.iterator(); i.hasNext();)
                register((MsgType) i.next());
    }

    public boolean equals(Object o) {
        return o instanceof MsgType && getPrecedence() == ((MsgType) o).getPrecedence();
    }

    public int hashCode() {
        return _label.hashCode();
    }

    public String toString() {
        return _label;
    }

    // set a text color for a specific log level
    public static void setLogLevelColorMap(MsgType level, Color color) {
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

    protected int getPrecedence() {
        return _precedence;
    }

}



