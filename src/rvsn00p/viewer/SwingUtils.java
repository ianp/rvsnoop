//:File:    SwingUtils.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * A collection of static utility methods for working with Swing classes.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class SwingUtils {

    /**
     * Selects a the specified row in the specified JTable and scrolls
     * the specified JScrollpane to the newly selected row. More importantly,
     * the call to repaint() delayed long enough to have the table
     * properly paint the newly selected row which may be offscre
     * @param table should belong to the specified JScrollPane
     */
    public static void selectRow(int row, final JTable table, JScrollPane pane) {
        try {
            if (row < table.getModel().getRowCount()) {
                // First we post some requests to the AWT event loop...
                pane.getVerticalScrollBar().setValue(row * table.getRowHeight());
                table.getSelectionModel().setSelectionInterval(row, row);
                // ...then queue a repaint to run once they have completed.
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        table.repaint();
                    }
                });
            }
        } catch (NullPointerException ignored) {
            // Deliberately ignored, method parameters may be null.
        }
    }

}

