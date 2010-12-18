/*
 * Class:     Paste
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.Application;
import org.rvsnoop.NLSUtils;

import rvsnoop.Record;
import rvsnoop.RecordSelection;
import rvsnoop.RvConnection.AddRecordTask;

import com.tibco.tibrv.TibrvException;

/**
 * Paste the contents of the system clipboard to the message ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class Paste extends RvSnoopAction {

    static { NLSUtils.internationalize(Paste.class); }

    private static final long serialVersionUID = -8506730607421335573L;

    private static final Log log = LogFactory.getLog(Paste.class);

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
            if (log.isWarnEnabled()) { log.warn(INFO_BAD_CLIP_DATA); }
            return;
        }
        try {
            final Record[] records = RecordSelection.read(clipboardData, application.getConnections());
            for (int i = 0, imax = records.length; i < imax; ++i) {
                SwingUtilities.invokeLater(new AddRecordTask(records[i]));
            }
        } catch (TibrvException e) {
            if (log.isErrorEnabled()) {
                log.error(MessageFormat.format(ERROR_RV, e.error), e);
            }
        } catch (IOException e) {
            if (log.isErrorEnabled()) { log.error(ERROR_IO, e); }
        } catch (UnsupportedFlavorException e) {
            if (log.isErrorEnabled()) { log.error(ERROR_CLIPBOARD_LOST, e); }
        }
    }

}
