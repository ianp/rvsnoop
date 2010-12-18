/*
 * Class:     RvSnoopMatcherEditor
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.matchers;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import com.google.common.base.Objects;
import com.google.inject.internal.ToStringBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

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
    @Override
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
     * @throws IllegalArgumentException If the argument is <code>null</code>.
     */
    public final void setDataAccessor(DataAccessor dataAccessor) {
        checkNotNull(dataAccessor);
        if (this.dataAccessor.equals(dataAccessor)) { return; }
        this.dataAccessor = dataAccessor;
        currentMatcher = new RvSnoopMatcher(dataAccessor, predicate);
        fireChanged(currentMatcher);
    }

    /**
     * Set the value that this matcher should match.
     *
     * @throws IllegalArgumentException If the argument is <code>null</code>.
     */
    public final void setPredicate(Predicate predicate) {
        // TODO add more specific methods that alter the current predicate and
        //      fire constrained/relaxed events
        checkNotNull(predicate);
        if (this.predicate == predicate) { return; }
        this.predicate = predicate;
        currentMatcher = new RvSnoopMatcher(dataAccessor, predicate);
        fireChanged(currentMatcher);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("dataAccessor", dataAccessor)
                .add("predicate", predicate).toString();
    }

}
