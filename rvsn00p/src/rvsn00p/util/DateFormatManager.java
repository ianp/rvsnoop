/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;

/**
 * Date format manager.
 * Utility class to help manage consistent date formatting and parsing.
 * It may be advantageous to have multiple DateFormatManagers per
 * application.  For example, one for handling the output (formatting) of
 * dates, and another one for handling the input (parsing) of dates.
 *
 * Time Format Syntax:
 * To specify the time format use a time pattern string.
 * In this pattern, all ASCII letters are reserved as pattern letters, which are defined as the following:
 *  Symbol   Meaning                 Presentation        Example
 *  ------   -------                 ------------        -------
 *  G        era designator          (Text)
 *  AD y        year                    (Number)            1996
 *  M        month in year           (Text & Number)     July & 07
 *  d        day in month            (Number)            10
 *  h        hour in am/pm (1~12)    (Number)            12
 *  H        hour in day (0~23)      (Number)            0
 *  m        minute in hour          (Number)            30
 *  s        second in minute        (Number)            55
 *  S        millisecond             (Number)            978
 *  E        day in week             (Text)              Tuesday
 *  D        day in year             (Number)            189
 *  F        day of week in month    (Number)            2 (2nd Wed in July)
 *  w        week in year            (Number)            27
 *  W        week in month           (Number)            2
 *  a        am/pm marker            (Text)              PM
 *  k        hour in day (1~24)      (Number)            24
 *  K        hour in am/pm (0~11)    (Number)            0
 *  z        time zone               (Text)              Pacific Standard Time
 *  '        escape for text         (Delimiter)
 *  ''       single quote            (Literal)           '
 *
 * @author orjan Lundberg
 * @author Robert Shaw
 * @author Michael J. Sikorsky
 * Contributed by ThoughtWorks Inc.
 */
public class DateFormatManager {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------
    private TimeZone _timeZone = null;
    private Locale _locale = null;

    private String _pattern = null;
    private DateFormat _dateFormat = null;
    final private FieldPosition _fieldPosition = new FieldPosition(0);


    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------
    public DateFormatManager() {
        super();
        configure();
    }

    public DateFormatManager(TimeZone timeZone) {
        super();

        _timeZone = timeZone;
        configure();
    }

    public DateFormatManager(Locale locale) {
        super();

        _locale = locale;
        configure();
    }

    public DateFormatManager(String pattern) {
        super();

        _pattern = pattern;
        configure();
    }

    public DateFormatManager(TimeZone timeZone, Locale locale) {
        super();

        _timeZone = timeZone;
        _locale = locale;
        configure();
    }

    public DateFormatManager(TimeZone timeZone, String pattern) {
        super();

        _timeZone = timeZone;
        _pattern = pattern;
        configure();
    }

    public DateFormatManager(Locale locale, String pattern) {
        super();

        _locale = locale;
        _pattern = pattern;
        configure();
    }

    public DateFormatManager(TimeZone timeZone, Locale locale, String pattern) {
        super();

        _timeZone = timeZone;
        _locale = locale;
        _pattern = pattern;
        configure();
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    public synchronized TimeZone getTimeZone() {
        if (_timeZone == null) {
            return TimeZone.getDefault();
        } else {
            return _timeZone;
        }
    }

    public synchronized void setTimeZone(TimeZone timeZone) {
        timeZone = timeZone;
        configure();
    }

    public synchronized Locale getLocale() {
        if (_locale == null) {
            return Locale.getDefault();
        } else {
            return _locale;
        }
    }

    public synchronized void setLocale(Locale locale) {
        _locale = locale;
        configure();
    }

    public synchronized String getPattern() {
        return _pattern;
    }

    /**
     * Set the pattern. i.e. "EEEEE, MMMMM d, yyyy hh:mm aaa"
     */
    public synchronized void setPattern(String pattern) {
        _pattern = pattern;
        configure();
    }


    /**
     * This method has been deprecated in favour of getPattern().
     * @deprecated Use getPattern().
     */
    public synchronized String getOutputFormat() {
        return _pattern;
    }

    /**
     * This method has been deprecated in favour of setPattern().
     * @deprecated Use setPattern().
     */
    public synchronized void setOutputFormat(String pattern) {
        _pattern = pattern;
        configure();
    }

    public synchronized DateFormat getDateFormatInstance() {
        return _dateFormat;
    }

    public synchronized void setDateFormatInstance(DateFormat dateFormat) {
        _dateFormat = dateFormat;
        // No reconfiguration necessary!
    }

    public String format(Date date) {
        return getDateFormatInstance().format(date);
    }

    public StringBuffer format(Date date,  StringBuffer toAppendTo) {
       return getDateFormatInstance().format(date,toAppendTo,_fieldPosition );
    }

    public String format(Date date, String pattern) {
        DateFormat formatter = null;
        formatter = getDateFormatInstance();
        if (formatter instanceof SimpleDateFormat) {
            formatter = (SimpleDateFormat) (formatter.clone());
            ((SimpleDateFormat) formatter).applyPattern(pattern);
        }
        return formatter.format(date);
    }

    /**
     * @throws java.text.ParseException
     */
    public Date parse(String date) throws ParseException {
        return getDateFormatInstance().parse(date);
    }

    /**
     * @throws java.text.ParseException
     */
    public Date parse(String date, String pattern) throws ParseException {
        DateFormat formatter = null;
        formatter = getDateFormatInstance();
        if (formatter instanceof SimpleDateFormat) {
            formatter = (SimpleDateFormat) (formatter.clone());
            ((SimpleDateFormat) formatter).applyPattern(pattern);
        }
        return formatter.parse(date);
    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------
    private synchronized void configure() {
        _dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL,
                                                           DateFormat.FULL,
                                                           getLocale());
        _dateFormat.setTimeZone(getTimeZone());

        if (_pattern != null) {
            ((SimpleDateFormat) _dateFormat).applyPattern(_pattern);
        }

    }


    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

}
