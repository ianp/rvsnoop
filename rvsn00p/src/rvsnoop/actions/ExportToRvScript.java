//:File:    ExportToRvScript.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;

import rvsnoop.Logger;
import rvsnoop.Marshaller;
import rvsnoop.Record;
import rvsnoop.ui.UIManager;

/**
 * Export the current ledger selction to an RvScript format message file.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToRvScript extends LedgerSelectionAction {
    private static class RvScriptMessagesFileFilter extends FileFilter {
        RvScriptMessagesFileFilter() {
            super();
        }
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".rvm");
        }

        public String getDescription() {
            return "RvScript Messages Files";
        }
    }

    private static final String ID = "exportToRvScript";

    private static final Logger logger = Logger.getLogger(ExportToRvScript.class);

    private static String NAME = "RvScript Message File";

    private static final long serialVersionUID = -483492422948058345L;

    private static String TOOLTIP = "Export the current ledger selction to an RvScript format messages file";

    private final Marshaller.Implementation marshaller = Marshaller.getImplementation(Marshaller.IMPL_RVSCRIPT);

    public ExportToRvScript() {
        super(ID, NAME, null);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        setEnabled(marshaller != null);
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected void actionPerformed(List selected) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new RvScriptMessagesFileFilter());
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(UIManager.INSTANCE.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0, imax = selected.size(); i < imax; ++i)
                bw.write(marshaller.marshal("", ((Record) selected.get(i)).getMessage()));
            logger.info("Written RvScript messages file to " + file.getName());
        } catch (IOException e) {
            logger.error("There was a problem writing the RvScript messages file.", e);
        } finally {
            IOUtils.closeQuietly(bw);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled && marshaller != null);
    }
}
