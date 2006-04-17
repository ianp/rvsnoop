//:File:    UIUtils.java
//:Created: Dec 23, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
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

import com.tibco.tibrv.TibrvException;

/**
 * A collection of static utility methods for working with Swing.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class UIUtils {
    
    private static String CONFIRM_TITLE = "Confirm";
    
    private static String ERROR_TITLE = "Error";

    private static String INFORMATION_TITLE = "Information";

    public static boolean askForConfirmation(Object message, Icon icon) {
        return JOptionPane.YES_NO_OPTION
            == JOptionPane.showConfirmDialog(UIManager.INSTANCE.getFrame(), message, CONFIRM_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
    }
    
    public static void centerWindowOnScreen(final Window window) {
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = window.getSize();
        size.width = Math.min(size.width, screen.width);
        size.height = Math.min(size.height, screen.height);
        window.setSize(size);
        window.setLocation((screen.width - size.width) / 2,
                           (screen.height - size.height) / 2);
    }
    
    public static void configureOKAndCancelButtons(JPanel panel, Action ok, Action cancel) {
        final ActionMap actionMap = panel.getActionMap();
        actionMap.put(cancel.getValue(Action.ACTION_COMMAND_KEY), cancel);
        actionMap.put(ok.getValue(Action.ACTION_COMMAND_KEY), ok);
        final InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancel.getValue(Action.ACTION_COMMAND_KEY));
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ok.getValue(Action.ACTION_COMMAND_KEY));
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
    
    public static GradientPaint paintGradient(Graphics2D g, int w, int h, Color start, Color end, GradientPaint paint) {
        if (paint == null)
            paint = new GradientPaint(0, 0, start, 0, h, end);
        g.setPaint(paint);
        g.fillRect(0, 0, w, h);
        return paint;
    }
    
    public static void showError(String message, Throwable exception) {
        final String title = exception instanceof TibrvException ? "Rendezvous Error" : ERROR_TITLE;
        final String[] m = new String[3];
        final String s = exception.getLocalizedMessage();
        m[0] = message;
        m[1] = s != null ? s : "";
        m[2] = exception instanceof TibrvException ? "Rendezvous Error Code: " + ((TibrvException) exception).error : "";
        JOptionPane.showMessageDialog(UIManager.INSTANCE.getFrame(), m, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void showInformation(Object message) {
        JOptionPane.showMessageDialog(UIManager.INSTANCE.getFrame(), message, INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showInformation(Object message, Icon icon) {
        JOptionPane.showMessageDialog(UIManager.INSTANCE.getFrame(), message, INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE, icon);
    }
    
    /**
     * Do not instantiate.
     */
    private UIUtils() {
        throw new UnsupportedOperationException();
    }

}
