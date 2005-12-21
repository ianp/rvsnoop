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
import javax.swing.JLabel;

import rvsn00p.util.DateFormatManager;
import rvsn00p.util.IOUtils;

/**
 * RvSnooperFileHandler
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public class RvSnooperFileHandler {

    /**
     * Saves the selected message to a text file
     * @param sSubject
     * @param sMsg
     * @param infostr
     * @param jdBase
     * @param statusLabel
     */
    static void saveMsgAsTextFile(String sSubject, String sMsg, String infostr, JFrame jdBase, JLabel statusLabel) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            FileDialog fd = new FileDialog(jdBase, "Save text File", FileDialog.SAVE);

            //guess the filname to be the last part of the subject (could be version in some cases)
            String sFileName;
            sSubject.lastIndexOf(".");

            sFileName = sSubject.substring(sSubject.lastIndexOf(".") + 1);
            fd.setFile(sFileName + ".txt");
            fd.show();
            String filename = fd.getDirectory() + fd.getFile();


            if (fd.getFile() != null) {
                f = new File(filename);
                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(sMsg);
                statusLabel.setText("Saved text file " + f.toString());
            }
        } catch (Exception ex) {
            new RvSnooperErrorDialog(
                    jdBase, "File save error " + ex.getMessage());
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
    static void saveTableToTextFile(String infostr, JFrame jdBase, JLabel statusLabel, LogTable table) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            FileDialog fd = new FileDialog(jdBase, "Save text File", FileDialog.SAVE);

            fd.setFile("*.txt");
            fd.show();
            String filename = fd.getDirectory() + fd.getFile();

            if (fd.getFile() != null) {

                f = new File(filename);

                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);

                buf_writer.write(table.getFilteredLogTableModel().createFilteredTextFromMsg().toString());
                statusLabel.setText("Saved text file " + f.toString());

            }
        } catch (Exception ex) {
            new RvSnooperErrorDialog(
                    jdBase, "File save error " + ex.getMessage());
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
    static void saveTableToHtml(String sVersion, String sURL, JFrame jfBase, JLabel statusLabel, LogTable table) {
        File f = null;
        FileWriter writer = null;
        BufferedWriter buf_writer = null;
        try {
            FileDialog fd = new FileDialog(jfBase, "Save HTML File", FileDialog.SAVE);

            fd.setFile("*.html");
            fd.show();
            String filename = fd.getDirectory() + fd.getFile();

            if (fd.getFile() != null) {

                f = new File(filename);

                f.createNewFile();

                writer = new FileWriter(f);
                buf_writer = new BufferedWriter(writer);
                DateFormatManager dfm = new DateFormatManager("yyyy-MM-dd HH:mm:ss.S");
                buf_writer.write("<html><head>\n");
                buf_writer.write("<title>RvSn00p HTML Output Page </title>\n");
                buf_writer.write("<META http-equiv=\"content-type\" content=\"text/html;\" charset="+System.getProperty("file.encoding")+"\">");
                buf_writer.write("\n<META NAME=\"description\" CONTENT=\"rvsn00p html output file.\">");
                buf_writer.write("\n<META NAME=\"keywords\" CONTENT=\"rvsn00p,tibco,rendezvous\">");
                buf_writer.write("\n<META NAME=\"Author\" CONTENT=\""+ System.getProperty("user.name","unknown")+ "\">");
                buf_writer.write("\n<META NAME=\"Creation_Date\" CONTENT=\"" + new Date() + "\">");
                buf_writer.write("</head>\n<body>\n");
                buf_writer.write("Generated by <a href=\"" + sURL + "\">" + sVersion + "</a> on " + new Date());

                buf_writer.write(table.getFilteredLogTableModel().createFilteredHTMLTable(dfm).toString());

                buf_writer.write("\n</body>\n</html>");
                statusLabel.setText("Saved HTML file " + f.toString());
            }
        } catch (Exception ex) {
            new RvSnooperErrorDialog(
                    jfBase, "File save error " + ex.getMessage());
        } finally {
            IOUtils.closeQuietly(buf_writer);
            IOUtils.closeQuietly(writer);
        }
    }

}
