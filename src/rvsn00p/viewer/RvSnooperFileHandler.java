/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.viewer;

import rvsn00p.util.DateFormatManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;


/**
 *
 * RvSnooperFileHandler
 * @author �rjan Lundberg
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
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    jdBase, "File save error " + ex.getMessage());
        } finally {

            if (buf_writer != null) {
                try {
                    buf_writer.close();
                } catch (IOException e1) {
                }
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }

            if (f != null) {
                f = null;
            }

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
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    jdBase, "File save error " + ex.getMessage());
        } finally {

            if (buf_writer != null) {
                try {
                    buf_writer.close();
                } catch (IOException e1) {
                }
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }


            if (f != null) {
                f = null;
            }

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
            RvSnooperErrorDialog error = new RvSnooperErrorDialog(
                    jfBase, "File save error " + ex.getMessage());
        } finally {

            if (buf_writer != null) {
                try {
                    buf_writer.close();
                } catch (IOException e1) {
                }
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }


            if (f != null) {
                f = null;
            }

        }
    }


}
