//:File:    ExportToText.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

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

import rvsn00p.IOUtils;
import rvsn00p.Record;
import rvsn00p.ui.Icons;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.rv.MarshalRvToString;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Export the current ledger selction to XHTML.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToText extends LedgerSelectionAction {

    private static final String ID = "exportToHtml";

    static String NAME = "Export to Text...";
    
    private static final long serialVersionUID = -483492422948058345L;
    
    static String TOOLTIP = "Export the selected records to Text";
    
    public ExportToText() {
        super(ID, NAME, Icons.EXPORT);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
    }

    /* (non-Javadoc)
     * @see rvsn00p.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected void actionPerformed(List selected) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt");
            }
            public String getDescription() {
                return "Text Files";
            }
        });
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(RvSnooperGUI.getAppFrame()))
            return;
        final File file = chooser.getSelectedFile();
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            for (int i = 0, imax = selected.size(); i < imax; ++i)
                bw.write(MarshalRvToString.marshal("", ((Record) selected.get(i)).getMessage()));
            RvSnooperGUI.setStatusBarMessage("Written text report to " + file.getName());
        } catch (IOException e) {
            UIUtils.showError("There was a problem writing the HTML report.", e);
        } finally {
            IOUtils.closeQuietly(bw);
            IOUtils.closeQuietly(fw);
        }
    }

}
