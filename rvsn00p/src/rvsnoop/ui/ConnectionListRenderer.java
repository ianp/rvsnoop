//:File:    ConnectionListRenderer.java
//:Created: Jan 4, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import rvsnoop.RvConnection;
import rvsnoop.State;

/**
 * A list renderer for connection objects.
 * <p>
 * This will render the name of the connection and, in a smaller and lighter
 * type face, any non standard service, network, and/or damon parameters.
 * 
 * @author Ian Phillips (<a href="mailto:ianp {at} ianp {dot} org">ianp {at}
 *         ianp {dot} org</a>)
 * @version $Revision$, $Date$
 */
public final class ConnectionListRenderer extends JPanel implements ListCellRenderer {

    private class ToolTipLabel extends JLabel {
        private static final long serialVersionUID = -7051868410968909731L;
        ToolTipLabel() {
            super();
        }
        public final JToolTip createToolTip() {
            return tooltip;
        }
        public void repaint(long tm, int x, int y, int width, int height) {
            // Do nothing.
        }
        public void repaint(Rectangle r) {
            // Do nothing.
        }
    }

    private static final long serialVersionUID = -8798068540266967995L;
    
    final StringBuffer buffer = new StringBuffer();
    
    private final JLabel daemon = new ToolTipLabel();
    
    private final JLabel description = new ToolTipLabel();

    private final boolean isHidingNonDefaults;
    
    private final JLabel network = new ToolTipLabel();
    
    private final JLabel service = new ToolTipLabel();

    private final JToolTip tooltip = new MultiLineToolTip();

    public ConnectionListRenderer(boolean isHidingNonDefaults) {
        super();
        this.isHidingNonDefaults = isHidingNonDefaults;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // Indent the labels a little.
        final Border border = BorderFactory.createEmptyBorder(1, 24, 1, 0);
        service.setBorder(border);
        network.setBorder(border);
        daemon.setBorder(border);
        // Make the font a bit smaller.
        final Font font = getFont().deriveFont(getFont().getSize() - 2.0f);
        service.setFont(font);
        network.setFont(font);
        daemon.setFont(font);
        // Make the font a bit lighter.
        final Color foreground = getForeground().brighter();
        service.setForeground(foreground);
        network.setForeground(foreground);
        daemon.setForeground(foreground);
        // Make them transparent.
        description.setOpaque(false);
        service.setOpaque(false);
        network.setOpaque(false);
        daemon.setOpaque(false);
        setOpaque(false);
        // Add them.
        add(description);
        add(service);
        add(network);
        add(daemon);
    }
    
    private void configureDetails(final RvConnection connection) {
        final String s = connection.getService();
        if (isHidingNonDefaults && RvConnection.DEFAULT_SERVICE.equals(s)) {
            service.setVisible(false);
        } else {
            service.setVisible(true);
            service.setText("Service: " + s);
        }
        final String n = connection.getNetwork();
        if (isHidingNonDefaults && RvConnection.DEFAULT_NETWORK.equals(n)) {
            network.setVisible(false);
        } else {
            network.setVisible(true);
            network.setText("Network: " + n);
        }
        final String d = connection.getDaemon();
        if (isHidingNonDefaults && RvConnection.DEFAULT_DAEMON.equals(d)) {
            daemon.setVisible(false);
        } else {
            daemon.setVisible(true);
            daemon.setText("Daemon: " + d);
        }
    }

    private void configureTooltip(final RvConnection connection) {
        for (final Iterator i = connection.getSubjects().iterator(); i.hasNext(); )
            buffer.append(i.next()).append("\n");
        if (buffer.length() == 0)
            buffer.append("No subjects subscribed to");
        else
            buffer.setLength(buffer.length() - 1);
        setToolTipText(buffer.toString());
        buffer.setLength(0);
    }

    public JToolTip createToolTip() {
        return tooltip;
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        final RvConnection connection = (RvConnection) value;
        description.setText(connection.getDescription());
        final State state = connection.getState();
        if (state == State.STARTED)
            description.setIcon(Icons.RVD_STARTED);
        else if (state == State.STOPPED)
            description.setIcon(Icons.RVD_STOPPED);
        else if (state == State.PAUSED)
            description.setIcon(Icons.RVD_PAUSED);
        configureDetails(connection);
        configureTooltip(connection);
        return this;
    }

    /**
     * Overridden for performance reasons.
     * <p>
     * As per the standard cell renderers the two repaint methods are overridden
     * to avoid taking unnecessary code paths as they are called many times.
     * Unlike the standard classes however, we still need to call the
     * <code>validate</code> methods as the check box and label need to be
     * laid out.
     */
    public void repaint(long tm, int x, int y, int width, int height) {
        // Do nothing.
    }

    /**
     * Overridden for performance reasons.
     * <p>
     * As per the standard cell renderers the two repaint methods are overridden
     * to avoid taking unnecessary code paths as they are called many times.
     * Unlike the standard classes however, we still need to call the
     * <code>validate</code> methods as the check box and label need to be
     * laid out.
     */
    public void repaint(Rectangle r) {
        // Do nothing.
    }

}
