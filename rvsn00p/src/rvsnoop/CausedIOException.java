//:File:    CausedIOException.java
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.io.IOException;

/**
 * An IOException with an underlying cause.
 * <p>
 * The standard {@linkplain IOException} class does not provide a constructor to
 * allow the cause to be set, this class overrides the {@linkplain #getCause()}
 * method to allow a cause to be accessed.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
final class CausedIOException extends IOException {

    static final long serialVersionUID = -4467829647590753023L;

    private final Throwable cause;

    /**
     * @param message
     * @param cause
     */
    public CausedIOException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    public Throwable getCause() {
        return cause;
    }

}
