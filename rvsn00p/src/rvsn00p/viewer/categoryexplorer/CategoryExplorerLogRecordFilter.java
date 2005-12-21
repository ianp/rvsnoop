//:File:    CategoryExplorerLogRecordFilter.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer.categoryexplorer;

import java.util.Enumeration;

import rvsn00p.LogRecord;
import rvsn00p.LogRecordFilter;

/**
 * An implementation of LogRecordFilter based on a CategoryExplorerModel
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class CategoryExplorerLogRecordFilter implements LogRecordFilter {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------

    protected CategoryExplorerModel _model;

    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    public CategoryExplorerLogRecordFilter(CategoryExplorerModel model) {
        _model = model;
    }

    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------

    /**
     * @return true if the CategoryExplorer model specified at construction
     * is accepting the category of the specified LogRecord.  Has a side-effect
     * of adding the CategoryPath of the LogRecord to the explorer model
     * if the CategoryPath is new.
     */
    public boolean passes(LogRecord record) {
        CategoryPath path = new CategoryPath(record.getSubject());
        return _model.isCategoryPathActive(path);
    }

    /**
     * Resets the counters for the contained CategoryNodes to zero.
     */
    public void reset() {
        resetAllNodes();
    }

    //--------------------------------------------------------------------------
    //   Protected Methods:
    //--------------------------------------------------------------------------

    protected void resetAllNodes() {
        Enumeration nodes = _model.getRootCategoryNode().depthFirstEnumeration();
        CategoryNode current;
        while (nodes.hasMoreElements()) {
            current = (CategoryNode) nodes.nextElement();
            current.resetNumberOfContainedRecords();
            _model.nodeChanged(current);
        }
    }
    //--------------------------------------------------------------------------
    //   Private Methods:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces
    //--------------------------------------------------------------------------
}

