//:File:    ExportToHtml.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.actions;

import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.znerd.xmlenc.XMLOutputter;

import rvsn00p.IOUtils;
import rvsn00p.Record;
import rvsn00p.StringUtils;
import rvsn00p.Version;
import rvsn00p.ui.Icons;
import rvsn00p.ui.UIUtils;
import rvsn00p.util.rv.MarshalRvToString;
import rvsn00p.viewer.RvSnooperGUI;

/**
 * Export the current ledger selction to XHTML.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToHtml extends LedgerSelectionAction {

    private static final String ID = "exportToHtml";

    static String NAME = "Export to HTML...";
    
    private static final long serialVersionUID = -483492422948058345L;
    
    static String TOOLTIP = "Export the selected records to HTML";
    
    public ExportToHtml() {
        super(ID, NAME, Icons.EXPORT);
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
    }

    /* (non-Javadoc)
     * @see rvsn00p.actions.LedgerSelectionAction#actionPerformed(java.util.List)
     */
    protected void actionPerformed(List selected) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase(Locale.ENGLISH).endsWith(".html");
            }
            public String getDescription() {
                return "HTML Files";
            }
        });
        if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(RvSnooperGUI.getAppFrame()))
            return;
        final File file = chooser.getSelectedFile();
        FileWriter fw = null;
        BufferedWriter bw = null;
        XMLOutputter xmlout = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            xmlout = new XMLOutputter(bw, "UTF-8");
            writeHeader(xmlout);
            for (int i = 0, imax = selected.size(); i < imax; ++i)
                writeRecord(xmlout, (Record) selected.get(i));
            writeFooter(xmlout);
            RvSnooperGUI.setStatusBarMessage("Written HTML report to " + file.getName());
        } catch (IOException e) {
            UIUtils.showError("There was a problem writing the HTML report.", e);
        } finally {
            IOUtils.closeQuietly(xmlout);
            IOUtils.closeQuietly(bw);
            IOUtils.closeQuietly(fw);
        }
    }

    private void writeFooter(XMLOutputter out) throws IllegalStateException, IOException {
        out.endTag(); // table
        out.endTag(); // body
        out.endTag(); // html
        out.endDocument();
    }

    private void writeHeader(XMLOutputter out) throws IllegalStateException, IllegalArgumentException, IOException {
        final String title = Version.getAsStringWithName() + " HTML Report (" + MarshalRvToString.getImplementationName() + " marshaller)";
        out.startTag("html");
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

    private void writeHttpEquiv(XMLOutputter out, String equiv, String content) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag("meta");
        out.attribute("http-equiv", equiv);
        out.attribute("content", content);
        out.endTag();
    }

    private void writeMeta(XMLOutputter out, String name, String content) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag("meta");
        out.attribute("name", name);
        out.attribute("content", content);
        out.endTag();
    }
    
    private void writeRecord(XMLOutputter out, Record record) throws IllegalStateException, IOException {
        out.startTag("tr");
        writeTagged(out, "td", StringUtils.format(new Date(record.getTimestamp())));
        writeTagged(out, "td", Long.toString(record.getSequenceNumber()));
        writeTagged(out, "td", record.getType().getLabel());
        writeTagged(out, "td", record.getSendSubject());
        writeTagged(out, "td", record.getTrackingId());
        out.startTag("td");
        writeTagged(out, "code", MarshalRvToString.marshal("", record.getMessage()));
        out.endTag();
        out.endTag();
    }
    
    private void writeTagged(XMLOutputter out, String tag, String pcdata) throws IllegalStateException, IllegalArgumentException, IOException {
        out.startTag(tag);
        out.pcdata(pcdata);
        out.endTag();
    }

}
