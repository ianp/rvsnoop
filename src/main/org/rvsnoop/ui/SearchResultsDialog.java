// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.ui;

import org.rvsnoop.Connections;
import org.rvsnoop.FilteredLedgerView;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.RecordLedger;
import rvsnoop.RecordTypes;
import rvsnoop.ui.UIUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A dialog for displaying search results.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class SearchResultsDialog extends JDialog {

    private final class FreezeAction extends AbstractAction {
        private static final long serialVersionUID = 2493033730732619900L;
        private boolean freeze = true;
        FreezeAction() {
            super(BUTTON_FREEZE);
            setEnabled(false);
        }
        public synchronized void actionPerformed(ActionEvent e) {
            putValue(Action.NAME, freeze ? BUTTON_UNFREEZE : BUTTON_FREEZE);
            results.setFrozen(freeze);
            freeze = !freeze;
        }
    }

    private final class OKAction extends AbstractAction {
        private static final long serialVersionUID = 7702235052544107033L;
        public OKAction() {
            super(BUTTON_OK);
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    static { NLSUtils.internationalize(SearchResultsDialog.class); }

    private static final long serialVersionUID = 5216848181389646587L;

    private static final ImageIcon BANNER = new ImageIcon("/resources/banners/searchResults.png");

    static String BUTTON_FREEZE, BUTTON_OK, BUTTON_UNFREEZE;
    static String DIALOG_TITLE, TITLE, DESCRIPTION, TOOLTIP_VISIBLE_COLUMNS;

    private final JPopupMenu columnsPopup = new JPopupMenu();

    private final FilteredLedgerView results;

    public SearchResultsDialog(Frame parent, RecordLedger results, Connections connections, RecordTypes recordTypes) {
        super(parent, DIALOG_TITLE, false); // false == non-modal
        if (results instanceof FilteredLedgerView) {
            this.results = (FilteredLedgerView) results;
        } else {
            this.results = FilteredLedgerView.newInstance(results, recordTypes, true);
        }

        final RecordLedgerTable table = new RecordLedgerTable(results, connections, recordTypes);
        columnsPopup.addPopupMenuListener(new VisibleColumnsMenuManager(table.getTableFormat()));
        final JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setBorder(BorderFactory.createEmptyBorder());
        scrollpane.getVerticalScrollBar().addAdjustmentListener(new TrackingAdjustmentListener());
        scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        final JButton colsButton = UIUtils.createSmallButton(
                new ImageIcon("/resources/icons/columns_corner_button.png"), TOOLTIP_VISIBLE_COLUMNS, null);
        colsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                columnsPopup.show(colsButton, e.getX(), e.getY());
            }
        });
        colsButton.setBorderPainted(false);
        scrollpane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, colsButton);

        final FreezeAction freeze = new FreezeAction();
        final OKAction ok = new OKAction();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new HeaderPanel(TITLE, DESCRIPTION, BANNER), BorderLayout.NORTH);
        getContentPane().add(scrollpane, BorderLayout.CENTER);
        getContentPane().add(new FooterPanel(ok, null, new Action[] { freeze }), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

}
