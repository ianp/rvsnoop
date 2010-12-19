// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop.ui;

import org.jdesktop.application.ApplicationContext;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.concurrent.Future;

/**
 * A runnable that is used to update the UI with the results of a {@link Future}.
 */
public abstract class SwingRunnable<V> implements Runnable {

    private final Future<V> future;
    private final ApplicationContext context;

    public SwingRunnable(Future<V> future, ApplicationContext context) {
        this.future = future;
        this.context = context;
    }

    public final void run() {
        if (future.isDone()) {
            try {
                onSuccess(future.get());
            } catch (Exception e) {
                onError(((RvSnoopApplication) context.getApplication()).getMainFrame(), e);
            }
        } else if (!future.isCancelled()) {
            SwingUtilities.invokeLater(this);
        }
    }

    protected abstract void onSuccess(V value);

    protected void onError(JFrame frame, Exception exception) {
        // hook for subclasses.
    }

}
