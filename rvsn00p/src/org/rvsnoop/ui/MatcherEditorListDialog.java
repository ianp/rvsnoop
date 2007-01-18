/*
 * Class:     MatcherEditorListDialog
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2007-2007 Ian Phillips.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.matchers.DataAccessorFactory;
import org.rvsnoop.matchers.PredicateFactory;
import org.rvsnoop.matchers.RvSnoopMatcherEditor;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * A common dialog used for all of the search and filter functions.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.7
 */
public final class MatcherEditorListDialog extends JDialog {

    private final class AddMatcherAction extends AbstractAction {
        private static final long serialVersionUID = 3951802385139429614L;
        AddMatcherAction() {
            super(BUTTON_ADD);
        }
        public void actionPerformed(ActionEvent e) {
            final MatcherEditorDialog dialog =
                new MatcherEditorDialog(MatcherEditorListDialog.this, true);
            dialog.setVisible(true);
            final RvSnoopMatcherEditor me = dialog.getMatcherEditor();
            if (me == null) { return; }
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(DEBUG_ADDING, new Object[] { me }));
            }
            final Lock lock = copyOfEditors.getReadWriteLock().writeLock();
            lock.lock();
            try {
                copyOfEditors.add(me);
                editorsList.repaint();
                if (log.isDebugEnabled()) {
                    log.debug(MessageFormat.format(DEBUG_ADDED,
                            new Object[] { new Integer(editorsList.getModel().getSize()) }));
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private final class CancelAction extends AbstractAction {
        private static final long serialVersionUID = -7518441879401691997L;
        CancelAction() {
            super(BUTTON_CANCEL);
        }
        public void actionPerformed(ActionEvent e) {
            copyOfEditors = null;
            setVisible(false);
            dispose();
        }
    }

    private final class OKAction extends AbstractAction {
        private static final long serialVersionUID = -6650791986035600540L;
        OKAction() {
            super(BUTTON_OK);
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    private final class RemoveMatcherAction extends AbstractAction implements ListSelectionListener {
        private static final long serialVersionUID = 3319737916373900856L;
        RemoveMatcherAction() {
            super(BUTTON_REMOVE);
            setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) {
            final Lock lock = copyOfEditors.getReadWriteLock().writeLock();
            lock.lock();
            try {
                copyOfEditors.remove(editorsList.getSelectedIndex());
            } finally {
                lock.unlock();
            }
        }
        public void valueChanged(ListSelectionEvent e) {
            setEnabled(!editorsList.isSelectionEmpty());
        }
    }

    static String ADD_TITLE, ADD_MESSAGE;

    static String BUTTON_ADD, BUTTON_CANCEL, BUTTON_OK, BUTTON_REMOVE;

    static String DEBUG_ADDED, DEBUG_ADDING, TITLE;

    private static final Log log = LogFactory.getLog(MatcherEditorListDialog.class);

    private static final RvSnoopMatcherEditor PROTOTYPE =
        new RvSnoopMatcherEditor(
                DataAccessorFactory.getInstance().createSendSubjectAccessor(),
                PredicateFactory.getInstance().createStringStartsWithPredicate("PROTOTYPE", false));

    private static final long serialVersionUID = -642470709671436909L;

    static { NLSUtils.internationalize(MatcherEditorListDialog.class); }

    private EventList copyOfEditors;

    private final JList editorsList;

    private final FooterPanel footer;

    private final HeaderPanel header;

    public MatcherEditorListDialog(Frame parent, String title, String description, Image image, EventList editors) {
        super(parent, TITLE, true); // true == modal
        copyOfEditors = getDeepCopy(editors);
        final GhostGlassPane glasspane = new GhostGlassPane();
        setGlassPane(glasspane);
        header = new HeaderPanel(title, description, image);
        editorsList = new ReorderableList(copyOfEditors, glasspane);
        final JScrollPane scrollpane = new JScrollPane(editorsList);
        scrollpane.setBackground(editorsList.getBackground());
        scrollpane.setBorder(null);
        editorsList.setCellRenderer(new MatcherEditorListCellRenderer());
        if (copyOfEditors.size() > 0) {
            editorsList.setPrototypeCellValue(copyOfEditors.get(0));
        } else {
            editorsList.setPrototypeCellValue(PROTOTYPE);
        }
        AddMatcherAction add = new AddMatcherAction();
        CancelAction cancel = new CancelAction();
        OKAction ok = new OKAction();
        RemoveMatcherAction remove = new RemoveMatcherAction();
        editorsList.addListSelectionListener(remove);
        footer = new FooterPanel(ok, cancel, new Action[] { add, remove });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(scrollpane, BorderLayout.CENTER);
        getContentPane().add(footer, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Return the list of configured matchers.
     *
     * @return The matchers, or <code>null</code> if the dialog was cancelled.
     */
    public EventList getCopyOfEditors() {
        return copyOfEditors;
    }

    private EventList getDeepCopy(EventList editors) {
        final Lock lock = editors.getReadWriteLock().readLock();
        lock.lock();
        final EventList copy = new BasicEventList(editors.size());
        try {
            for (Iterator i = editors.iterator(); i.hasNext(); ) {
                copy.add(new RvSnoopMatcherEditor((RvSnoopMatcherEditor) i.next()));
            }
        } finally {
            lock.unlock();
        }
        return copy;
    }

}
