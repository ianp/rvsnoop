//:File:    FontChooser.java
//:Created: Dec 27, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import rvsnoop.Logger;

import com.jgoodies.forms.factories.ButtonBarFactory;

/**
 * A font chooser, because one is inexplicably missing from Swing!
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class FontChooser extends JComponent {

    private class CancelAction extends AbstractAction {
        private static final long serialVersionUID = 439955168973577220L;

        private final JDialog dialog;

        CancelAction(JDialog dialog) {
            super("Cancel");
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            dialogResultValue = CANCEL_OPTION;
            dialog.dispose();
        }
    }

    private class CancelledWindowAdapter extends WindowAdapter {
        CancelledWindowAdapter() {
            super();
        }
        public void windowClosing(WindowEvent e) {
            dialogResultValue = CANCEL_OPTION;
        }
    }

    private class ListDocumentHandler implements DocumentListener {
        public class ListSelector implements Runnable {
            private final int index;

            public ListSelector(int index) {
                this.index = index;
            }

            public void run() {
                list.setSelectedIndex(this.index);
            }
        }

        final JList list;

        public ListDocumentHandler(JList list) {
            this.list = list;
        }

        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent event) {
            String newValue = "";
            try {
                final Document doc = event.getDocument();
                newValue = doc.getText(0, doc.getLength());
            } catch (BadLocationException e) {
                if (Logger.isWarnEnabled())
                logger.warn("Bad document location.", e);
            }
            if (newValue.length() > 0) {
                int index = list.getNextMatch(newValue, 0, Position.Bias.Forward);
                if (index < 0) index = 0;
                list.ensureIndexIsVisible(index);
                final String matchedName = list.getModel().getElementAt(index).toString();
                if (newValue.equalsIgnoreCase(matchedName) && index != list.getSelectedIndex())
                        SwingUtilities.invokeLater(new ListSelector(index));
            }
        }
    }

    private class ListSelectionHandler implements ListSelectionListener {
        private final JTextComponent text;

        ListSelectionHandler(JTextComponent text) {
            this.text = text;
        }

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                final JList list = (JList) e.getSource();
                final String newName = (String) list.getSelectedValue();
                final String oldName = text.getText();
                text.setText(newName);
                if (!oldName.equalsIgnoreCase(newName)) {
                    text.selectAll();
                    text.requestFocus();
                }
                updateSampleFont();
            }
        }
    }

    private class OKAction extends AbstractAction {
        private static final long serialVersionUID = -4759591501364546868L;

        private final JDialog dialog;

        OKAction(JDialog dialog) {
            super("OK");
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e) {
            dialogResultValue = OK_OPTION;
            dialog.dispose();
        }
    }

    private class TextFieldFocusHandler extends FocusAdapter {
        private final JTextComponent text;

        public TextFieldFocusHandler(JTextComponent text) {
            this.text = text;
        }

        public void focusGained(FocusEvent e) {
            text.selectAll();
        }

        public void focusLost(FocusEvent e) {
            text.select(0, 0);
            updateSampleFont();
        }
    }

    private static class UpDownKeyHandler extends KeyAdapter {
        private final JList list;

        public UpDownKeyHandler(JList list) {
            this.list = list;
        }

        public void keyPressed(KeyEvent e) {
            int i = list.getSelectedIndex();
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                --i;
                if (i < 0) i = 0;
                list.setSelectedIndex(i);
                break;
            case KeyEvent.VK_DOWN:
                final int listSize = list.getModel().getSize();
                ++i;
                if (i >= listSize) i = listSize - 1;
                list.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Return value from {@link #showDialog(Component)}.
     */
    private static final int CANCEL_OPTION = 1;

    private static final Font DEFAULT_SELECTED_FONT = new Font("Serif", Font.PLAIN, 12);

    /**
     * Return value from {@link #showDialog(Component)}.
     */
    private static final int ERROR_OPTION = -1;

    private static final String[] FONT_SIZE_LIST = { "8", "9", "10", "11", "12", "14", "16", "18" };

    private static final int[] FONT_STYLE_CODES = { Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC };

    private static final String[] FONT_STYLE_NAMES = { "Plain",  "Bold",  "Italic",  "Bold Italic"};

    private static final Border MATTE_BORDER = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY);
    
    private static final Border PADDED_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    private static final Border TEXT_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);

    private static final Logger logger = Logger.getLogger(FontChooser.class);
    
    /**
     * Return value from {@link #showDialog(Component)}.
     */
    public static final int OK_OPTION = 0;

    private static final int PREF_NAME_WIDTH = 180;
    private static final int PREF_HEIGHT = 130;
    private static final int PREF_SIZE_WIDTH = 70;
    private static final int PREF_STYLE_WIDTH = 140;
    private static final int PREF_SAMPLE_WIDTH = 300;
    private static final int PREF_SAMPLE_HEIGHT = 100;

    private static final long serialVersionUID = 2636494627988477508L;

    private int dialogResultValue = ERROR_OPTION;

    private String[] fontFamilyNames = null;

    private JTextField fontFamilyTextField = null;

    private JList fontNameList = null;

    private JList fontSizeList = null;

    private JTextField fontSizeTextField = null;

    private JList fontStyleList = null;

    private JTextField fontStyleTextField = null;

    private JTextField sampleText = null;

    public FontChooser() {
        super();
        final JPanel selectPanel = new JPanel();
        selectPanel.setBorder(BorderFactory.createEmptyBorder());
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.add(createFontNamePanel());
        selectPanel.add(createFontStylePanel());
        selectPanel.add(createFontSizePanel());
        final JPanel contentsPanel = new JPanel(new GridLayout(2, 1));
        contentsPanel.setBorder(PADDED_BORDER);
        contentsPanel.add(selectPanel, BorderLayout.NORTH);
        contentsPanel.add(createSamplePanel(), BorderLayout.CENTER);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(contentsPanel);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setSelectedFont(DEFAULT_SELECTED_FONT);
    }

    private JDialog createDialog(Component parent) {
        final Frame frame = parent instanceof Frame
            ? (Frame) parent
            : (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        final JDialog dialog = new JDialog(frame, "Select Font", true);

        final Action ok = new OKAction(dialog);
        final Action cancel = new CancelAction(dialog);
        final JPanel buttons = ButtonBarFactory.buildOKCancelBar(new JButton(ok), new JButton(cancel));
        buttons.setBorder(PADDED_BORDER);
        UIUtils.configureOKAndCancelButtons(buttons, ok, cancel);
        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    private JPanel createFontNamePanel() {
        final JPanel fontNamePanel = new JPanel(new BorderLayout());
        fontNamePanel.setBorder(BorderFactory.createEmptyBorder());
        fontNamePanel.setPreferredSize(new Dimension(PREF_NAME_WIDTH, PREF_HEIGHT));

        final JScrollPane scrollPane = new JScrollPane(getFontNameList());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setFocusable(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JPanel p = new JPanel();
        p.setBorder(MATTE_BORDER);
        p.setLayout(new BorderLayout());
        p.add(getFontNameTextField(), BorderLayout.NORTH);
        p.add(scrollPane, BorderLayout.CENTER);

        final JLabel label = new JLabel("Name");
        label.setBorder(BorderFactory.createEmptyBorder());
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setLabelFor(getFontNameTextField());
        label.setDisplayedMnemonic('N');

        fontNamePanel.add(label, BorderLayout.NORTH);
        fontNamePanel.add(p, BorderLayout.CENTER);
        return fontNamePanel;
    }
    
    private JPanel createFontSizePanel() {
        final JPanel fontSizePanel = new JPanel(new BorderLayout());
        fontSizePanel.setBorder(BorderFactory.createEmptyBorder());
        fontSizePanel.setPreferredSize(new Dimension(PREF_SIZE_WIDTH, PREF_HEIGHT));

        final JScrollPane scrollPane = new JScrollPane(getFontSizeList());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setFocusable(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JPanel p = new JPanel(new BorderLayout());
        p.setBorder(MATTE_BORDER);
        p.add(getFontSizeTextField(), BorderLayout.NORTH);
        p.add(scrollPane, BorderLayout.CENTER);

        final JLabel label = new JLabel("Size");
        label.setBorder(BorderFactory.createEmptyBorder());
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setLabelFor(getFontSizeTextField());
        label.setDisplayedMnemonic('S');

        fontSizePanel.add(label, BorderLayout.NORTH);
        fontSizePanel.add(p, BorderLayout.CENTER);
        return fontSizePanel;
    }

    private JPanel createFontStylePanel() {
        final JPanel fontStylePanel = new JPanel(new BorderLayout());
        fontStylePanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        fontStylePanel.setPreferredSize(new Dimension(PREF_STYLE_WIDTH, PREF_HEIGHT));

        final JScrollPane scrollPane = new JScrollPane(getFontStyleList());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setFocusable(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        final JPanel p = new JPanel(new BorderLayout());
        p.setBorder(MATTE_BORDER);
        p.add(getFontStyleTextField(), BorderLayout.NORTH);
        p.add(scrollPane, BorderLayout.CENTER);

        final JLabel label = new JLabel("Style");
        label.setBorder(BorderFactory.createEmptyBorder());
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setLabelFor(getFontStyleTextField());
        label.setDisplayedMnemonic('Y');

        fontStylePanel.add(label, BorderLayout.NORTH);
        fontStylePanel.add(p, BorderLayout.CENTER);
        return fontStylePanel;
    }

    private JPanel createSamplePanel() {
        final JPanel samplePanel = new JPanel(new BorderLayout());
        samplePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 0, 0, 0), // Outer
            MATTE_BORDER)); // Inner
        samplePanel.add(getSampleTextField(), BorderLayout.CENTER);
        return samplePanel;
    }

    private JList getFontNameList() {
        if (fontNameList == null) {
            fontNameList = new JList(getFontNames());
            fontNameList.setBorder(BorderFactory.createEmptyBorder());
            fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontNameList.addListSelectionListener(new ListSelectionHandler(getFontNameTextField()));
            fontNameList.setSelectedIndex(0);
            fontNameList.setFocusable(false);
        }
        return fontNameList;
    }

    private String[] getFontNames() {
        if (fontFamilyNames == null) {
            final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilyNames = env.getAvailableFontFamilyNames();
        }
        return fontFamilyNames;
    }

    private JTextField getFontNameTextField() {
        if (fontFamilyTextField == null) {
            fontFamilyTextField = new JTextField();
            fontFamilyTextField.setBorder(TEXT_BORDER);
            fontFamilyTextField.addFocusListener(new TextFieldFocusHandler(fontFamilyTextField));
            fontFamilyTextField.addKeyListener(new UpDownKeyHandler(getFontNameList()));
            fontFamilyTextField.getDocument().addDocumentListener(new ListDocumentHandler(getFontNameList()));
        }
        return fontFamilyTextField;
    }

    private JList getFontSizeList() {
        if (fontSizeList == null) {
            fontSizeList = new JList(FONT_SIZE_LIST);
            fontSizeList.setBorder(BorderFactory.createEmptyBorder());
            fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontSizeList.addListSelectionListener(new ListSelectionHandler(getFontSizeTextField()));
            fontSizeList.setSelectedIndex(0);
            fontSizeList.setFocusable(false);
        }
        return fontSizeList;
    }
    private JTextField getFontSizeTextField() {
        if (fontSizeTextField == null) {
            fontSizeTextField = new JTextField();
            fontSizeTextField.setBorder(TEXT_BORDER);
            fontSizeTextField.addFocusListener(new TextFieldFocusHandler(fontSizeTextField));
            fontSizeTextField.addKeyListener(new UpDownKeyHandler(getFontSizeList()));
            fontSizeTextField.getDocument().addDocumentListener(new ListDocumentHandler(getFontSizeList()));
        }
        return fontSizeTextField;
    }

    private JList getFontStyleList() {
        if (fontStyleList == null) {
            fontStyleList = new JList(FONT_STYLE_NAMES);
            fontStyleList.setBorder(BorderFactory.createEmptyBorder());
            fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontStyleList.addListSelectionListener(new ListSelectionHandler(getFontStyleTextField()));
            fontStyleList.setSelectedIndex(0);
            fontStyleList.setFocusable(false);
        }
        return fontStyleList;
    }

    private JTextField getFontStyleTextField() {
        if (fontStyleTextField == null) {
            fontStyleTextField = new JTextField();
            fontStyleTextField.setBorder(TEXT_BORDER);
            fontStyleTextField.addFocusListener(new TextFieldFocusHandler(fontStyleTextField));
            fontStyleTextField.addKeyListener(new UpDownKeyHandler(getFontStyleList()));
            fontStyleTextField.getDocument().addDocumentListener(new ListDocumentHandler(getFontStyleList()));
        }
        return fontStyleTextField;
    }

    private JTextField getSampleTextField() {
        if (sampleText == null) {
            sampleText = new JTextField("SampleString");
            sampleText.setBorder(BorderFactory.createEmptyBorder());
            sampleText.setPreferredSize(new Dimension(PREF_SAMPLE_WIDTH, PREF_SAMPLE_HEIGHT));
        }
        return sampleText;
    }

    public Font getSelectedFont() {
        return new Font(getSelectedFontFamily(), FONT_STYLE_CODES[getFontStyleList().getSelectedIndex()], getSelectedFontSize());
    }

    private String getSelectedFontFamily() {
        return (String) getFontNameList().getSelectedValue();
    }

    private int getSelectedFontSize() {
        try {
            return Integer.parseInt(getFontSizeTextField().getSelectedText());
        } catch (NumberFormatException e) {
            getFontSizeTextField().setText("10");
            return 10;
        }
    }

    public void setSelectedFont(Font font) {
        setSelectedFontFamily(font.getFamily());
        setSelectedFontStyle(font.getStyle());
        setSelectedFontSize(font.getSize());
    }

    private void setSelectedFontFamily(String name) {
        final String[] names = getFontNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(name)) {
                getFontNameList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    private void setSelectedFontSize(int size) {
        final String sizeString = String.valueOf(size);
        for (int i = 0; i < FONT_SIZE_LIST.length; i++) {
            if (FONT_SIZE_LIST[i].equals(sizeString)) {
                getFontSizeList().setSelectedIndex(i);
                break;
            }
        }
        getFontSizeTextField().setText(sizeString);
        updateSampleFont();
    }

    private void setSelectedFontStyle(int style) {
        for (int i = 0; i < FONT_STYLE_CODES.length; i++) {
            if (FONT_STYLE_CODES[i] == style) {
                getFontStyleList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    /**
     * Show font selection dialog.
     * 
     * @param parent Dialog's Parent component.
     * @return OK_OPTION or CANCEL_OPTION
     */
    public int showDialog(Component parent) {
        dialogResultValue = ERROR_OPTION;
        final JDialog dialog = createDialog(parent);
        dialog.addWindowListener(new CancelledWindowAdapter());
        dialog.setVisible(true);
        dialog.dispose();
        return dialogResultValue;
    }

    private void updateSampleFont() {
        getSampleTextField().setFont(getSelectedFont());
    }
}
