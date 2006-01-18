//:File:    ExportToHtml.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop.actions;

import org.znerd.xmlenc.XMLOutputter;
import rvsnoop.IOUtils;
import rvsnoop.Logger;
import rvsnoop.Marshaller;
import rvsnoop.RecordType;
import rvsnoop.Record;
import rvsnoop.StringUtils;
import rvsnoop.Version;
import rvsnoop.ui.UIManager;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Export the current ledger selction to XHTML.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToHtml extends LedgerSelectionAction {
    private static class HTMLFileFilter extends FileFilter {
        HTMLFileFilter() {
            super();
        }
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".html");
        }

        public String getDescription() {
            return "HTML Files";
        }
    }

    private static final String ID = "exportToHtml";

    private static final Logger logger = Logger.getLogger(ExportToHtml.class);
    
    private static String NAME = "HTML Report";
    
    private static final long serialVersionUID = -483492422948058345L;
    
    private static String TOOLTIP = "Export the selected records to HTML";
    
    public ExportToHtml() {
        super(ID, NAME, null);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected void actionPerformed(List selected) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new HTMLFileFilter());
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(UIManager.INSTANCE.getFrame()))
            return;
        final File file = chooser.getSelectedFile();
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            final XMLOutputter xmlout = new XMLOutputter(bw, "UTF-8");
            writeHeader(xmlout);
            for (int i = 0, imax = selected.size(); i < imax; ++i)
                writeRecord(xmlout, (Record) selected.get(i));
            xmlout.close();
            logger.info("Written HTML report to " + file.getName());
        } catch (IOException e) {
            logger.error("There was a problem writing the HTML report.", e);
        } finally {
            IOUtils.closeQuietly(bw);
            IOUtils.closeQuietly(fw);
        }
    }

    private static void writeHeader(XMLOutputter out) throws IllegalStateException, IllegalArgumentException, IOException {
        final String title = Version.getAsStringWithName() + " HTML Report (" + Marshaller.getImplementationName() + " marshaller)";
        out.declaration();
        out.dtd("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        out.startTag("html");
        out.attribute("xmlns", "http://www.w3.org/1999/xhtml");
        out.startTag("head");
        writeTagged(out, "title", title);
        writeHttpEquiv(out, "Content-Type", "application/xhtml+xml; charset=utf-8");
        writeHttpEquiv(out, "Content-Language", "en-uk");
        writeMeta(out, "generator", Version.getAsStringWithName());
        out.endTag(); // head
        out.startTag("body");
        writeTagged(out, "h1", title);
        out.startTag("table");
        out.startTag("tr");
        writeTagged(out, "th", "Timestamp");
        writeTagged(out, "th", "Sequence Number");
        writeTagged(out, "th", "Type");
        writeTagged(out, "th", "Send Subject");
        writeTagged(out, "th", "Tracking ID");
        writeTagged(out, "th", "Message");
        out.endTag(); // tr
    }

    private static void writeHttpEquiv(XMLOutputter out, String equiv, String content) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag("meta");
        out.attribute("http-equiv", equiv);
        out.attribute("content", content);
        out.endTag();
    }

    private static void writeMeta(XMLOutputter out, String name, String content) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag("meta");
        out.attribute("name", name);
        out.attribute("content", content);
        out.endTag();
    }
    
    private static void writeRecord(XMLOutputter out, Record record) throws IllegalStateException, IOException {
        out.startTag("tr");
        writeTagged(out, "td", StringUtils.format(new Date(record.getTimestamp())));
        writeTagged(out, "td", Long.toString(record.getSequenceNumber()));
        writeTagged(out, "td", RecordType.getFirstMatchingType(record).getName());
        writeTagged(out, "td", record.getSendSubject());
        writeTagged(out, "td", record.getTrackingId());
        out.startTag("td");
        writeTagged(out, "pre", Marshaller.marshal("", record.getMessage()));
        out.endTag();
        out.endTag();
    }
    
    private static void writeTagged(XMLOutputter out, String tag, String pcdata) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag(tag);
        out.pcdata(pcdata);
        out.endTag();
    }

}
