/*
 * Class:     TrackingAdjustmentListener
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

/**
 * A listener which ensures that an adjustable tracks it's maximum position.
 * <p>
 * For example when a vertical scroll bar is at its bottom anchor this listener
 * will force the scroll bar to remain "glued" at the bottom, when the vertical
 * scroll bar is at any other location, then no tracking will happen.
 * <p>
 * An instance of this class should only listen to one Adjustable as it retains
 * state information about the Adjustable it listens to.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class TrackingAdjustmentListener implements AdjustmentListener {

    private int previousMaximum = -1;

    public TrackingAdjustmentListener() {
        super();
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        final Adjustable a = e.getAdjustable();
        final int maximum = a.getMaximum();
        if (maximum == previousMaximum)
            return;
        final int b = a.getValue() + a.getVisibleAmount() + a.getUnitIncrement();
        if (b >= previousMaximum)
            a.setValue(maximum);
        previousMaximum = maximum;
    }

}
