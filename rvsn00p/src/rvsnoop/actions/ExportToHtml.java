//:File:    ExportToHtml.java
//:Created: Jan 2, 2006
//:Legal:   Copyright © 2006-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2006-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.filechooser.FileFilter;

import org.znerd.xmlenc.XMLOutputter;

import rvsnoop.Marshaller;
import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.StringUtils;
import rvsnoop.Version;

/**
 * Export the current ledger selction to XHTML.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
final class ExportToHtml extends ExportToFile {

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

    private static String NAME = "HTML Report";

    private static final long serialVersionUID = -483492422948058345L;

    private static String TOOLTIP = "Export the selected records to HTML";

    private XMLOutputter out;

    public ExportToHtml() {
        super(ID, NAME, null, new HTMLFileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeHeader(int)
     */
    protected void writeFooter() throws IOException {
        out.close();
    }

    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeHeader(int)
     */
    protected void writeHeader(int numberOfRecords) throws IOException {
        out = new XMLOutputter(new OutputStreamWriter(stream), "UTF-8");
        final String title = Version.getAsStringWithName() + " HTML Report (" + Marshaller.getImplementationName() + " marshaller)";
        out.declaration();
        out.dtd("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        out.startTag("html");
        out.attribute("xmlns", "http://www.w3.org/1999/xhtml");
        out.startTag("head");
        writeTagged("title", title);
        writeHttpEquiv("Content-Type", "application/xhtml+xml; charset=utf-8");
        writeHttpEquiv("Content-Language", "en-uk");
        writeMeta("generator", Version.getAsStringWithName());
        out.endTag(); // head
        out.startTag("body");
        writeTagged("h1", title);
        out.startTag("table");
        out.startTag("tr");
        writeTagged("th", "Timestamp");
        writeTagged("th", "Sequence Number");
        writeTagged("th", "Type");
        writeTagged("th", "Send Subject");
        writeTagged("th", "Tracking ID");
        writeTagged("th", "Message");
        out.endTag(); // tr
    }

    private void writeHttpEquiv(String equiv, String content) throws IOException {
        out.startTag("meta");
        out.attribute("http-equiv", equiv);
        out.attribute("content", content);
        out.endTag();
    }

    private void writeMeta(String name, String content) throws IOException {
        out.startTag("meta");
        out.attribute("name", name);
        out.attribute("content", content);
        out.endTag();
    }


    /* (non-Javadoc)
     * @see rvsnoop.actions.ExportToFile#writeRecord(rvsnoop.Record, int)
     */
    protected void writeRecord(Record record, int index) throws IOException {
        out.startTag("tr");
        writeTagged("td", StringUtils.format(new Date(record.getTimestamp())));
        writeTagged("td", Long.toString(record.getSequenceNumber()));
        writeTagged("td", RecordTypes.getInstance().getFirstMatchingType(record).getName());
        writeTagged("td", record.getSendSubject());
        writeTagged("td", record.getTrackingId());
        out.startTag("td");
        writeTagged("pre", Marshaller.marshal("", record.getMessage()));
        out.endTag(); // td
        out.endTag(); // tr
    }

    private void writeTagged(String tag, String pcdata) throws IOException {
        out.startTag(tag);
        out.pcdata(pcdata != null ? pcdata : "");
        out.endTag();
    }

}
