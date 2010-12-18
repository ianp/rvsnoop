// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package rvsnoop.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.rvsnoop.ui.MainFrame;

import com.tibco.tibrv.TibrvException;

/**
 * A collection of static utility methods for working with Swing.
 */
public final class UIUtils {

    private static String ERROR_TITLE = "Error";

    public static void configureOKAndCancelButtons(JPanel panel, Action ok, Action cancel) {
        final ActionMap actionMap = panel.getActionMap();
        final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (cancel != null) {
            actionMap.put(cancel.getValue(Action.ACTION_COMMAND_KEY), cancel);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancel.getValue(Action.ACTION_COMMAND_KEY));
        }
        if (ok != null) {
            actionMap.put(ok.getValue(Action.ACTION_COMMAND_KEY), ok);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ok.getValue(Action.ACTION_COMMAND_KEY));
        }
    }

    public static JButton createSmallButton(Icon icon, String tooltip, ActionListener listener) {
        final JButton btn = new JButton(icon);
        btn.setBorder(BorderFactory.createEmptyBorder());
        final Dimension size = new Dimension(16, 16);
        btn.setMinimumSize(size);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMargin(new Insets(0, 0, 0, 0));
        if (listener != null)
            btn.addActionListener(listener);
        btn.setToolTipText(tooltip);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        return btn;
    }

    public static void showError(String message, Throwable exception) {
        final String title = exception instanceof TibrvException ? "Rendezvous Error" : ERROR_TITLE;
        final String[] m = new String[3];
        final String s = exception.getLocalizedMessage();
        m[0] = message;
        m[1] = s != null ? s : "";
        m[2] = exception instanceof TibrvException ? "Rendezvous Error Code: " + ((TibrvException) exception).error : "";
        JOptionPane.showMessageDialog(MainFrame.INSTANCE, m, title, JOptionPane.ERROR_MESSAGE);
    }

    private UIUtils() {}

}
