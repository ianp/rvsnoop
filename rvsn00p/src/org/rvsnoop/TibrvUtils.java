/*
 * Class:     TibrvUtils
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgField;

/**
 * A collection of static utility methods for working with Rendezvous.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class TibrvUtils {

    /**
     * Returns an iterator that traverses all of the fields in a message in a depth first order.
     * <p>
     * Elements in the iterator mat safely be cast to {@link TibrvMsgField}.
     *
     * @param message The message to traverse.
     * @return The iterator.
     */
    public static Iterator depthFirstFieldIterator(TibrvMsg message) {
        return new DepthFirstFieldIterator(message);
    }

    private TibrvUtils() { throw new UnsupportedOperationException(); }

}

/** Used to hold a stack frame in the iterator. */
final class StackElement {
    final TibrvMsg message;
    final int position;
    StackElement(TibrvMsg message, int position) {
        this.message = message;
        this.position = position;
    }
}

/** Traverse the fields in a message in depth first order. */
final class DepthFirstFieldIterator implements Iterator {
    private final List stack = new ArrayList(1);
    private boolean hasNext;
    private TibrvMsg message;
    private int numFields;
    private int position;
    DepthFirstFieldIterator(TibrvMsg message) {
        this.message = message;
        numFields = message.getNumFields();
        hasNext = numFields > 0;
    }
    public boolean hasNext() {
        return hasNext;
    }
    public Object next() {
        if (!hasNext) { throw new NoSuchElementException(); }
        try {
            final TibrvMsgField next = message.getField(position++);
            if (next.type == TibrvMsg.MSG && ((TibrvMsg) next.data).getNumFields() > 0) {
                stack.add(new StackElement(message, position));
                message = (TibrvMsg) next.data;
                position = 0;
                numFields = message.getNumFields();
            }
            if (position == numFields) {
                if (stack.size() > 0) {
                    StackElement elt = (StackElement) stack.remove(stack.size() - 1);
                    message = elt.message;
                    position = elt.position;
                    numFields = message.getNumFields();
                } else {
                    hasNext = false;
                }
            }
            return next;
        } catch (TibrvException e) {
            throw new RuntimeException(e);
        }
    }
    public void remove() {
        throw new UnsupportedOperationException();
    }
}