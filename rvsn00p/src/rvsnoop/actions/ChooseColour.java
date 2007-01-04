/*
 * Class:     ChooseColour
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.colorchooser.ColorSelectionModel;

import rvsnoop.ui.UIUtils;

import com.jgoodies.binding.adapter.ColorSelectionAdapter;
import com.jgoodies.binding.value.ValueModel;

/**
 * Allow the user to select a colour.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.6
 */
public final class ChooseColour extends AbstractAction {

    private class CancelListener implements ActionListener {
        final Color original = (Color) valueModel.getValue();
        CancelListener() {
            super();
        }
        public void actionPerformed(ActionEvent e) {
            valueModel.setValue(original);
        }
    }

    private class OKListener implements ActionListener {
        OKListener() {
            super();
        }
        public void actionPerformed(ActionEvent e) {
            putValue(Action.SMALL_ICON, UIUtils.createSolidColorIcon(14, 14,
                    (Color) valueModel.getValue(), Color.BLACK));
        }
    }

    private static final String ID = "chooseColour";

    private static String NAME = "Colour";

    static final long serialVersionUID = -4352462710896790116L;

    private static String TITLE = "Choose Colour";

    private static String TOOLTIP = "Choose a colour";

    private final ValueModel valueModel;

    public ChooseColour(ValueModel valueModel) {
        super(NAME);
        this.valueModel = valueModel;
        putValue(Action.SMALL_ICON, UIUtils.createSolidColorIcon(14, 14,
                (Color) valueModel.getValue(), Color.BLACK));
        putValue(Action.ACTION_COMMAND_KEY, ID);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        final ColorSelectionModel model = new ColorSelectionAdapter(valueModel);
        final Object source = event.getSource();
        final Component c = source instanceof Component ? (Component) source : null;
        JColorChooser.createDialog(c, TITLE, false, new JColorChooser(model),
                new OKListener(), new CancelListener()).setVisible(true);
    }

}
