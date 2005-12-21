//:File:    RvSnooperRvTransportInputDialog.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import rvsn00p.util.rv.RvParameters;

/**
 * An input dialog for entry of Rendezvous parameters and subjects.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvSnooperRvTransportInputDialog extends RvSnooperDialog {

    private static final long serialVersionUID = 8834430809279507332L;

    private JTextField _tService = new JTextField();
    private JTextField _tNetwork = new JTextField();
    private JTextField _tDaemon  = new JTextField();
    private JTextField _tSubject = new JTextField();

    private boolean isOK;

    /**
     * Create a new instance of this class.
     * 
     * @param parent The parent component for the dialog (it is modal).
     * @param initial The initial parameter values to display.
     */
    public RvSnooperRvTransportInputDialog(JFrame parent, RvParameters initial) {
        super(parent, "New Listener", true);
        JPanel bottom = new JPanel(new FlowLayout());
        JPanel main = new JPanel(new GridLayout(8,0));

        createInputField("Service:", initial.getService(), _tService, main );
        createInputField("Daemon:", initial.getDaemon(), _tDaemon, main );
        createInputField("Subjects:", initial.getSubjectsAsString(), _tSubject, main );
        createInputField("Network:", initial.getNetwork(), _tNetwork, main);

        _tSubject.setToolTipText("Comma (,) separated list of subjects (subject1,subject2)");
        _tNetwork.setToolTipText("networkcardid;networkaddress");

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    hide();
                    setOK(true);
                }
            }
        });

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
                setOK(true);
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
                setOK(false);

            }
        });

        bottom.add(ok);
        bottom.add(cancel);
        getContentPane().add(main, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
        pack();
        centerOnScreen();
        setVisible(true);
    }

    public rvsn00p.util.rv.RvParameters getRvParameters() {

        RvParameters p = new RvParameters();

        p.setDaemon(_tDaemon.getText());
        p.setNetwork(_tNetwork.getText());
        p.setService(_tService.getText());
        String[] subjects = _tSubject.getText().split(",");
        for (int i = 0, imax = subjects.length; i < imax; ++i)
            p.addSubject(subjects[i]);
        return p;
    }

    public boolean isOK() {
        return isOK;
    }

   protected void createInputField(String label, String initial, JTextField field, JPanel panel){

        JLabel jl =  new JLabel(label);
        panel.add(jl);
        field.setText(initial);
        panel.add(field);
        jl.setLabelFor(field);

   }

    protected void setOK(boolean OK) {
        isOK = OK;
    }
}
