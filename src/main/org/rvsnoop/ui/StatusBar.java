// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)
package org.rvsnoop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.rvsnoop.Application;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * A status bar capable of displaying notifications to the user.
 * <p>
 * The message area should be used for general status messages.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.4
 */
public final class StatusBar extends JPanel {

    private final class FilterListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            setCountFiltered(listChanges.getSourceList().size());
        }
    }

    private final class LedgerListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            setCountAll(listChanges.getSourceList().size());
        }
    }

    private final class MessageClearer implements ActionListener {
        final String messageToClear;
        MessageClearer(String messageToClear) {
            this.messageToClear = messageToClear;
        }
        public void actionPerformed(ActionEvent e) {
            if (messageToClear.equals(message.getText())) {
                message.setText(" ");
                message.setToolTipText(" ");
            }
        }
    }

    private static final Color bgEnd = new Color(0xE0, 0xE0, 0xE0);
    private static final Color bgStart = Color.WHITE;

    private static final int MESSAGE_DISPLAY_MILLIS = 5000;

    private static final long serialVersionUID = -8192322965748166492L;

    private GradientPaint backgroundGradient;

    private final JLabel count = new JLabel("0/0");

    private int countAll, countFiltered;

    private final StringBuilder countBuilder = new StringBuilder("0/0");

    private final JLabel encoding = new JLabel();

    /**
     * The font that will be used should be small enough to fit into a 16 pixel
     * high status bar.
     */
    private Font font;

    private final JLabel message = new JLabel(" ");

    /**
     * Create a new <code>StatusBar</code>.
     */
    public StatusBar(Application application) {
        super();
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        count.setIcon(new ImageIcon("/resources/icons/statusBarCount.png"));
        count.setOpaque(false);
        count.setBorder(new MatteBorder(0, 1, 0, 1, Color.GRAY));
        encoding.setIcon(new ImageIcon("/resources/icons/statusBarEncoding.png"));
        encoding.setText(Charset.defaultCharset().displayName());
        encoding.setToolTipText(Locale.getDefault().getDisplayName());
        encoding.setOpaque(false);
        if (AppHelper.getPlatform() == PlatformType.OS_X) {
            // Leave engough room for the grow box.
            encoding.setBorder(new EmptyBorder(0, 0, 0, 18));
        }
        message.setOpaque(false);
        setOpaque(false); // Manually paint the background gradient.

        final GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup hgp = layout.createSequentialGroup();
        layout.setHorizontalGroup(hgp);
        hgp.addComponent(message, 1, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)
            .addComponent(count, 1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(encoding, 1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);
        GroupLayout.ParallelGroup vgp = layout.createParallelGroup();
        layout.setVerticalGroup(vgp);
        vgp.addComponent(message, 1, 16, 16)
            .addComponent(count, 1, 16, 16)
            .addComponent(encoding, 1, 16, 16);
        final Dimension d = getPreferredSize();
        d.height = 16;
        setPreferredSize(d);

        application.getLedger().addListEventListener(new LedgerListener());
        application.getFilteredLedger().addListEventListener(new FilterListener());
    }

    /**
     * Make sure that the font is correctly sized and then call the super
     * implementation.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D gg = (Graphics2D) g;
        if (font == null) { resizeFont(gg.getFontRenderContext()); }
        final int w = getWidth(), h = getHeight();
        if (backgroundGradient == null) {
            backgroundGradient = new GradientPaint(0, 0, bgStart, 0, h, bgEnd);
        }
        final Paint storedPaint = gg.getPaint();
        gg.setPaint(backgroundGradient);
        gg.fillRect(0, 0, w, h);
        gg.setPaint(storedPaint);
        super.paintComponent(g);
    }

    private void resizeFont(FontRenderContext frc) {
        font = getFont();
        // Just use some sample letters to resize the font, this isn't ideal from an
        // I18N perspective, but it's better than relying on a user generated string
        // which may be empty.
        while (font.getLineMetrics("ABCDEFgHIjKLMNOPqRSTUVWXyZ", frc).getHeight() >= 14.0f) { //$NON-NLS-1$
            font = font.deriveFont(font.getSize2D() - 1.0f);
        }
        setFont(font);
        count.setFont(font);
        encoding.setFont(font);
        message.setFont(font);
    }

    public synchronized void setCountAll(int all) {
        countBuilder.setLength(0);
        countBuilder.append(countFiltered).append('/').append(countAll = all);
        count.setText(countBuilder.toString());
    }

    public synchronized void setCountFiltered(int filtered) {
        countBuilder.setLength(0);
        countBuilder.append(countFiltered = filtered).append('/').append(countAll);
        count.setText(countBuilder.toString());
    }

    public void setMessage(String text) {
        if (text != null && text.length() > 0) {
            message.setText(text);
            message.setToolTipText(text);
            new Timer(MESSAGE_DISPLAY_MILLIS, new MessageClearer(text)).start();
        } else {
            message.setText(" ");
            message.setToolTipText(null);
        }
    }

}
