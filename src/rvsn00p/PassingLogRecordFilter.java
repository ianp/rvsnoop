//:File:    PassingLogRecordFilter.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p;

/**
 * An implementation of LogRecordFilter which always returns true.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class PassingLogRecordFilter implements LogRecordFilter {

    public PassingLogRecordFilter() {
        super();
    }
    
    /**
     * @return Always returns <code>true</code>.
     */
    public boolean passes(Record record) {
        return true;
    }

}
