/*
 * Class:     SynchronizedIterator
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import java.util.Iterator;

/**
 * A wrapper that synchronizes accesses to an iterator.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class SynchronizedIterator implements Iterator {

    private final Iterator iterator;

    private final Object mutex;

    /**
     * Create a new synchronized iterator.
     *
     * @param iterator The iterator to wrap.
     * @param mutex The object to synchronize on.
     */
    public SynchronizedIterator(final Iterator iterator, final Object mutex) {
        super();
        this.iterator = iterator;
        this.mutex = mutex;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        synchronized (mutex) {
            return iterator.hasNext();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next() {
        synchronized (mutex) {
            return iterator.next();
        }
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        synchronized (mutex) {
            iterator.remove();
        }
    }

}
