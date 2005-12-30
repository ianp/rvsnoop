//:File:    RvSnooperFileHandler.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.viewer;

import java.awt.FileDialog;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import javax.swing.JFrame;

import rvsn00p.IOUtils;
import rvsn00p.Version;
import rvsn00p.ui.UIUtils;

/**
 * RvSnooperFileHandler
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvSnooperFileHandler {

    private static final String TAG_END = "\">";

    /**
     * Saves the selected message to a text file
     * @param sSubject
     * @param sMsg
     * @param infostr
     * @param jdBase
     * @param statusLabel
     */
    static void saveMsgAsTextFile(String sSubject, String sMsg, String infostr, JFrame jdBase) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            final FileDialog fd = new FileDialog(jdBase, "Save text File", FileDialog.SAVE);

            //guess the filname to be the last part of the subject (could be version in some cases)
            String sFileName;
            sSubject.lastIndexOf(".");

            sFileName = sSubject.substring(sSubject.lastIndexOf(".") + 1);
            fd.setFile(sFileName + ".txt");
            fd.show();
            final String filename = fd.getDirectory() + fd.getFile();


            if (fd.getFile() != null) {
                f = new File(filename);
                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(sMsg);
                RvSnooperGUI.setStatusBarMessage("Saved message to " + f.toString());
            }
        } catch (Exception e) {
            UIUtils.showError("There was an error whilst saving the message.", e);
        } finally {
            IOUtils.closeQuietly(buf_writer);
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * Saves table to rvscript message.
     * @param infostr
     * @param jdBase
     * @param statusLabel
     */
    static void saveTableToTextFile(String infostr, JFrame jdBase, LogTable table) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            final FileDialog fd = new FileDialog(jdBase, "Save text File", FileDialog.SAVE);

            fd.setFile("*.txt");
            fd.show();
            final String filename = fd.getDirectory() + fd.getFile();

            if (fd.getFile() != null) {

                f = new File(filename);

                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);

                buf_writer.write(table.getFilteredLogTableModel().createFilteredTextFromMsg().toString());
                RvSnooperGUI.setStatusBarMessage("Saved ledger as text to " + f.toString());

            }
        } catch (Exception e) {
            UIUtils.showError("There was an error whilst saving the text file.", e);
        } finally {
            IOUtils.closeQuietly(buf_writer);
            IOUtils.closeQuietly(writer);
        }
    }

    /**
     * saveTableToHtml
     * Saves table to .
     * @param sVersion  version
     * @param sURL     url
     */
    static void saveTableToHtml(String sVersion, JFrame jfBase, LogTable table) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            final FileDialog fd = new FileDialog(jfBase, "Save HTML File", FileDialog.SAVE);

            fd.setFile("*.html");
            fd.show();
            final String filename = fd.getDirectory() + fd.getFile();

            if (fd.getFile() != null) {

                f = new File(filename);

                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write("<html><head>\n");
                buf_writer.write("<title>RvSn00p HTML Output Page </title>\n");
                buf_writer.write("<META http-equiv=\"content-type\" content=\"text/html;\" charset=" + System.getProperty("file.encoding") + TAG_END);
                buf_writer.write("\n<META NAME=\"description\" CONTENT=\"rvsn00p html output file.\">");
                buf_writer.write("\n<META NAME=\"keywords\" CONTENT=\"rvsn00p,tibco,rendezvous\">");
                buf_writer.write("\n<META NAME=\"Author\" CONTENT=\""+ System.getProperty("user.name","unknown") + TAG_END);
                buf_writer.write("\n<META NAME=\"Creation_Date\" CONTENT=\"" + new Date() + TAG_END);
                buf_writer.write("</head>\n<body>\n");
                buf_writer.write("Generated by <a href=\"http://rvsn00p.sf.net\">" + Version.getAsStringWithName() + "</a> on " + new Date());

                buf_writer.write(table.getFilteredLogTableModel().createFilteredHTMLTable().toString());

                buf_writer.write("\n</body>\n</html>");
                RvSnooperGUI.setStatusBarMessage("Saved ledger as HTML to " + f.toString());
            }
        } catch (Exception e) {
            UIUtils.showError("There was an error whilst saving the HTML file.", e);
        } finally {
            IOUtils.closeQuietly(buf_writer);
            IOUtils.closeQuietly(writer);
        }
    }
    
    /**
     * Do not instantiate.
     */
    private RvSnooperFileHandler() {
        throw new UnsupportedOperationException();
    }

}
