/*
 * Class:     RecordSubjectMatcher
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import rvsnoop.Record;
import rvsnoop.SubjectElement;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * A matcher for records with a specific subject.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class RecordSubjectMatcher implements Matcher {

    private final SubjectElement subject;

    private final boolean descendants;

    public RecordSubjectMatcher(SubjectElement subject, boolean descendants) {
        this.descendants = descendants;
        this.subject = subject;
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.matchers.Matcher#matches(java.lang.Object)
     */
    public boolean matches(Object item) {
        if (!(item instanceof Record)) { return false; }
        final SubjectElement s = ((Record) item).getSubject();
        return descendants ? subject.isNodeDescendant(s) : subject.equals(s);
    }

}
