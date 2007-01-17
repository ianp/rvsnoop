/*
 * Class:     RvSnoopMatcherEditor
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * Base class for all of the matcher editors used in the application.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnoopMatcherEditor extends AbstractMatcherEditor {

    private DataAccessor dataAccessor;

    private Predicate predicate;

    /**
     * Create a new <code>RvSnoopMatcherEditor</code>.
     *
     * @param dataAccessor The data to search when looking for a match.
     * @param predicate What to search for when looking for a match.
     */
    public RvSnoopMatcherEditor(DataAccessor dataAccessor, Predicate predicate) {
        this.dataAccessor = dataAccessor;
        this.predicate = predicate;
        this.currentMatcher = new RvSnoopMatcher(dataAccessor, predicate);
    }

    /**
     * Copy constructor.
     *
     * @param copy The matcher editor to copy.
     */
    public RvSnoopMatcherEditor(RvSnoopMatcherEditor copy) {
        this.dataAccessor = copy.dataAccessor;
        this.predicate = copy.predicate;
        this.currentMatcher = copy.currentMatcher;
    }

    /**
     * Get the record data that this matcher should search in.
     *
     * @return The data accessor.
     */
    public DataAccessor getDataAccessor() {
        return dataAccessor;
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.matchers.AbstractMatcherEditor#getMatcher()
     */
    public final Matcher getMatcher() {
        return currentMatcher;
    }

    /**
     * Get the value that this matcher should match.
     *
     * @return The predicate.
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * Set the record data that this matcher should search in.
     *
     * @param value The data to search in.
     * @throws IllegalArgumentException If the argument is <code>null</code>.
     */
    public final void setDataAccessor(DataAccessor dataAccessor) {
        Validate.notNull(dataAccessor);
        if (this.dataAccessor.equals(dataAccessor)) { return; }
        this.dataAccessor = dataAccessor;
        currentMatcher = new RvSnoopMatcher(dataAccessor, predicate);
        fireChanged(currentMatcher);
    }

    /**
     * Set the value that this matcher should match.
     *
     * @param value The value to match.
     * @throws IllegalArgumentException If the argument is <code>null</code>.
     */
    public final void setPredicate(Predicate predicate) {
        // TODO add more specific methods that alter the current predicate and
        //      fire constrained/relaxed events
        Validate.notNull(predicate);
        if (this.predicate == predicate) { return; }
        this.predicate = predicate;
        currentMatcher = new RvSnoopMatcher(dataAccessor, predicate);
        fireChanged(currentMatcher);
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("dataAccessor", dataAccessor)
                .append("predicate", predicate).toString();
    }

}
