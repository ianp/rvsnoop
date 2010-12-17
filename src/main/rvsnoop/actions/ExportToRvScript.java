/*
 * Class:     ExportToRvScript
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.filechooser.FileFilter;

import org.rvsnoop.Application;
import org.rvsnoop.actions.ExportToFile;

import rvsnoop.Marshaller;
import rvsnoop.Record;

/**
 * Export the current ledger selction to an RvScript format message file.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class ExportToRvScript extends ExportToFile {
    private static class RvScriptMessagesFileFilter extends FileFilter {
        RvScriptMessagesFileFilter() {
            super();
        }
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".rvm");
        }

        @Override
        public String getDescription() {
            return "RvScript Messages Files";
        }
    }

    public static final String COMMAND = "exportToRvScript";

    private static String NAME = "RvScript Message File";

    private static final long serialVersionUID = -483492422948058345L;

    private static String TOOLTIP = "Export the current ledger selction to an RvScript format messages file";

    private final Marshaller.Implementation marshaller = Marshaller.getImplementation(Marshaller.IMPL_RVSCRIPT);

    public ExportToRvScript(Application application) {
        super(application, COMMAND, NAME, new RvScriptMessagesFileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        setEnabled(marshaller != null);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled && marshaller != null);
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeRecord(rvsnoop.Record, int)
     */
    @Override
    protected void writeRecord(Record record, int index) {
        marshaller.marshal("", record.getMessage());
    }

}
