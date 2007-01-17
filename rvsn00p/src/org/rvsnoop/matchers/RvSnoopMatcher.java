/*
 * Class:     RvSnoopMatcher
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import java.util.Iterator;

import rvsnoop.Record;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * A matcher that uses data accessors and predicates to perform it's tests.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnoopMatcher implements Matcher {

    private final DataAccessor dataAccessor;

    private final Predicate predicate;

    /**
     * Create a new <code>RvSnoopMatcher</code>.
     *
     * @param dataAccessor The data to search when looking for a match.
     * @param predicate What to search for when looking for a match.
     */
    RvSnoopMatcher(DataAccessor dataAccessor, Predicate predicate) {
        this.dataAccessor = dataAccessor;
        this.predicate = predicate;
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.matchers.Matcher#matches(java.lang.Object)
     */
    public final boolean matches(Object item) {
        if (!(item instanceof Record)) { return false; }
        final Iterator i = dataAccessor.getDataElement((Record) item);
        while (i.hasNext()) {
            if (predicate.matches(i.next())) { return true; }
        }
        return false;
    }

}