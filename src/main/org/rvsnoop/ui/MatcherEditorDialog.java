// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.ui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.rvsnoop.NLSUtils;
import org.rvsnoop.matchers.DataAccessor;
import org.rvsnoop.matchers.DataAccessorFactory;
import org.rvsnoop.matchers.Predicate;
import org.rvsnoop.matchers.PredicateFactory;
import org.rvsnoop.matchers.RvSnoopMatcherEditor;

/**
 * A dialog for configuring matcher editors.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class MatcherEditorDialog extends JDialog {

    private final class CancelAction extends AbstractAction {
        private static final long serialVersionUID = 2450670826543886600L;
        CancelAction() {
            super(BUTTON_CANCEL);
        }
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    private final class OKAction extends AbstractAction {
        private static final long serialVersionUID = -6170094544483082456L;
        OKAction() {
            super(BUTTON_OK);
        }
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            if (matcherEditor == null) {
                createMatcherEditor();
            } else {
                updateMatcherEditor();
            }
            setVisible(false);
            dispose();
        }
    }

    static String BUTTON_CANCEL, BUTTON_OK, IGNORE_CASE;
    static String TITLE_ADD, TITLE_EDIT;

    private static final long serialVersionUID = 1043976701683179413L;

    static { NLSUtils.internationalize(MatcherEditorDialog.class); }

    private final JComboBox dataAccessor =
        new JComboBox(DataAccessorFactory.getInstance().getDisplayNames());

    private RvSnoopMatcherEditor matcherEditor;

    private final JTextField predicateArgument = new JTextField();

    private final JCheckBox predicateIgnoreCase = new JCheckBox(IGNORE_CASE);

    private final JComboBox predicateType =
        new JComboBox(PredicateFactory.getInstance().getDisplayNames());

    public MatcherEditorDialog(Dialog parent, boolean modal) {
        super(parent, TITLE_ADD, modal);
        configureComponentsAndLayout(parent);
    }

    public MatcherEditorDialog(Dialog parent, boolean modal, RvSnoopMatcherEditor matcherEditor) {
        super(parent, TITLE_EDIT, modal);
        configureComponentsAndLayout(parent);
        configureMatcherEditor(matcherEditor);
    }

    public MatcherEditorDialog(Frame parent, boolean modal) {
        super(parent, TITLE_ADD, modal);
        configureComponentsAndLayout(parent);
    }

    public MatcherEditorDialog(Frame parent, boolean modal, RvSnoopMatcherEditor matcherEditor) {
        super(parent, TITLE_EDIT, modal);
        configureComponentsAndLayout(parent);
        configureMatcherEditor(matcherEditor);
    }

    private void configureMatcherEditor(RvSnoopMatcherEditor matcherEditor) {
        this.matcherEditor = matcherEditor;
        final DataAccessor da = matcherEditor.getDataAccessor();
        dataAccessor.setSelectedItem(da.getDisplayName());
        final Predicate p = matcherEditor.getPredicate();
        predicateType.setSelectedItem(p.getDisplayName());
        predicateArgument.setText(p.getArgument());
        predicateIgnoreCase.setSelected(p.isIgnoringCase());
    }

    private void configureComponentsAndLayout(Window parent) {
        final JButton leading = new JButton(AppHelper.getPlatform() == PlatformType.WINDOWS ? new OKAction() : new CancelAction());
        final JButton trailing = new JButton(AppHelper.getPlatform() == PlatformType.WINDOWS ? new CancelAction() : new OKAction());
        predicateArgument.setColumns(20);
        // Layout
        final Container contents = getContentPane();
        final GroupLayout layout = new GroupLayout(contents);
        contents.setLayout(layout);
        layout.setAutoCreateGaps(true);
        final int pref = GroupLayout.PREFERRED_SIZE;
        // Horizontal group
        final GroupLayout.ParallelGroup hgp = layout.createParallelGroup();
        layout.setHorizontalGroup(hgp);
        hgp.addGroup(layout.createSequentialGroup()
                .addComponent(dataAccessor, 1, pref, pref)
                .addComponent(predicateType, 1, pref, pref)
                .addComponent(predicateArgument, pref, pref, Integer.MAX_VALUE)
                .addComponent(predicateIgnoreCase))
            .addGroup(layout.createSequentialGroup()
                .addGap(1, 1, Integer.MAX_VALUE)
                .addComponent(leading)
                .addComponent(trailing));
        // Vertical group
        final GroupLayout.SequentialGroup vgp = layout.createSequentialGroup();
        layout.setVerticalGroup(vgp);
        vgp.addGroup(layout.createBaselineGroup(true, false)
                .addComponent(dataAccessor)
                .addComponent(predicateType)
                .addComponent(predicateArgument)
                .addComponent(predicateIgnoreCase))
            .addGroup(layout.createParallelGroup()
                .addComponent(leading)
                .addComponent(trailing));
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void createMatcherEditor() {
        matcherEditor = new RvSnoopMatcherEditor(getDataAccessor(), getPredicate());
    }

    private DataAccessor getDataAccessor() {
        return DataAccessorFactory.getInstance().createFromDisplayName(
                (String) dataAccessor.getSelectedItem());
    }

    public RvSnoopMatcherEditor getMatcherEditor() {
        assert matcherEditor != null;
        return matcherEditor;
    }

    private Predicate getPredicate() {
        return PredicateFactory.getInstance().createFromDisplayName(
                (String) predicateType.getSelectedItem(),
                predicateArgument.getText(),
                predicateIgnoreCase.isSelected());
    }

    private void updateMatcherEditor() {
        matcherEditor.setDataAccessor(getDataAccessor());
        matcherEditor.setPredicate(getPredicate());
    }

}
