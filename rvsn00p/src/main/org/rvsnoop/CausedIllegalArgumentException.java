/*
 * Class:     CausedIllegalArgumentException
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;


/**
 * An IllegalArgumentException with an underlying cause.
 * <p>
 * The standard {@link IllegalArgumentException} class does not provide a
 * constructor to allow the cause to be set, this class overrides the
 * {@link #getCause()} method to allow a cause to be accessed.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class CausedIllegalArgumentException extends IllegalArgumentException {

    private static final long serialVersionUID = -2232264521304477496L;

    private final Throwable cause;

    /**
     * @param message
     * @param cause
     */
    public CausedIllegalArgumentException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

}
