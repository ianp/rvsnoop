// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.rvsnoop.Application;
import org.rvsnoop.Logger;
import org.rvsnoop.NLSUtils;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.RvConnection.AddRecordTask;

import com.tibco.tibrv.TibrvException;

/**
 * Paste the contents of the system clipboard to the message ledger.
 */
public final class Paste extends RvSnoopAction {

    static { NLSUtils.internationalize(Paste.class); }

    private static final long serialVersionUID = -8506730607421335573L;

    private static final Logger logger = Logger.getLogger();

    public static final String COMMAND = "paste";
    static String ACCELERATOR, MNEMONIC, NAME, TOOLTIP;
    static String ERROR_CLIPBOARD_LOST, ERROR_IO, ERROR_RV, INFO_BAD_CLIP_DATA;

    public Paste(Application application) {
        super(NAME, application);
        putValue(Action.ACTION_COMMAND_KEY, COMMAND);
        putSmallIconValue(COMMAND);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putMnemonicValue(MNEMONIC);
        putAcceleratorValue(ACCELERATOR);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable clipboardData = clipboard.getContents(this);
        if (!clipboardData.isDataFlavorSupported(RecordSelection.BYTES_FLAVOUR)
                && !clipboardData.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            logger.warn(INFO_BAD_CLIP_DATA);
            return;
        }
        try {
            final Record[] records = RecordSelection.read(clipboardData, application.getConnections());
            for (int i = 0, imax = records.length; i < imax; ++i) {
                SwingUtilities.invokeLater(new AddRecordTask(records[i]));
            }
        } catch (TibrvException e) {
            logger.error(e, ERROR_RV, e.error);
        } catch (IOException e) {
            logger.error(e, ERROR_IO);
        } catch (UnsupportedFlavorException e) {
            logger.error(e, ERROR_CLIPBOARD_LOST);
        }
    }

}
