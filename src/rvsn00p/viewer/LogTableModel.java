//:File:    LogTableModel.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import javax.swing.table.DefaultTableModel;

/**
 * LogTableModel
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class LogTableModel extends DefaultTableModel {

    private static final long serialVersionUID = -6187899669853182462L;

    public LogTableModel(Object[] colNames, int numRows) {
        super(colNames, numRows);
    }

}
