/*
 * Class:     RecordTypesDialog
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.rvsnoop.NLSUtils;

import rvsnoop.RecordType;
import rvsnoop.RecordTypes;

/**
 * A dialog to allow the record types in a project to be customized.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class RecordTypesDialog extends JDialog {

    private final class AddTypeAction extends AbstractAction {
        private static final long serialVersionUID = 5919428615655075591L;
        AddTypeAction() {
            super(BUTTON_ADD);
        }
        public void actionPerformed(ActionEvent e) {
            types.createType();
        }
    }

    private final class OKAction extends AbstractAction {
        private static final long serialVersionUID = 1366301664349178313L;
        OKAction() {
            super(BUTTON_OK);
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    private final class RemoveTypeAction extends AbstractAction implements ListSelectionListener {
        private static final long serialVersionUID = 4827030412184767246L;
        RemoveTypeAction() {
            super(BUTTON_REMOVE);
            setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) {
            types.removeType((RecordType) typesList.getSelectedValue());
        }
        public void valueChanged(ListSelectionEvent e) {
            setEnabled(!typesList.isSelectionEmpty());
        }
    }
    
    static { NLSUtils.internationalize(RecordTypesDialog.class); }

    private static final long serialVersionUID = -2564241831430080435L;

    private static final Image BANNER = ImageFactory.getInstance().getBannerImage("editRecordTypes");

    static String DIALOG_TITLE, TITLE, DESCRIPTION;
    static String BUTTON_ADD, BUTTON_OK, BUTTON_REMOVE;

    private final FooterPanel footer;

    private final HeaderPanel header;
    
    private final RecordTypes types;
    
    private final JList typesList;

    /**
     * Creates a new <code>RecordTypesDialog</code>.
     *
     * @param parent The parent frame, may be <code>null</code>.
     * @param types The types to display.
     */
    public RecordTypesDialog(Frame parent, RecordTypes types) {
        super(parent, DIALOG_TITLE, true); // true == modal
        this.types = types;
        final GhostGlassPane glasspane = new GhostGlassPane();
        setGlassPane(glasspane);
        header = new HeaderPanel(TITLE, DESCRIPTION, BANNER);
        typesList = new ReorderableList(types.getEventList(), glasspane);
        final JScrollPane scrollpane = new JScrollPane(typesList);
        scrollpane.setBackground(typesList.getBackground());
        scrollpane.setBorder(null);
        typesList.setCellRenderer(new RecordTypesListCellRenderer());
        typesList.setPrototypeCellValue(RecordTypes.DEFAULT);
        AddTypeAction add = new AddTypeAction();
        OKAction ok = new OKAction();
        RemoveTypeAction remove = new RemoveTypeAction();
        typesList.addListSelectionListener(remove);
        footer = new FooterPanel(ok, null, new Action[] { add, remove });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(scrollpane, BorderLayout.CENTER);
        getContentPane().add(footer, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

}
