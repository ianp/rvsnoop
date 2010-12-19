// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package rvsnoop.actions;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.filechooser.FileFilter;

import nu.xom.Attribute;
import nu.xom.DocType;
import nu.xom.Element;
import nu.xom.Text;
import org.rvsnoop.Application;
import org.rvsnoop.actions.ExportToFile;

import org.rvsnoop.io.StreamSerializer;
import rvsnoop.Marshaller;
import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.Version;

/**
 * Export the current ledger selction to XHTML.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.5
 */
public final class ExportToHtml extends ExportToFile {

    private static class HTMLFileFilter extends FileFilter {
        HTMLFileFilter() {
            super();
        }
        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".html");
        }

        @Override
        public String getDescription() {
            return "HTML Files";
        }
    }

    public static final String COMMAND = "exportToHtml";

    private static String NAME = "HTML Report";

    private static final long serialVersionUID = -483492422948058345L;

    private static String TOOLTIP = "Export the selected records to HTML";

    private static final String NSURI = "http://www.w3.org/1999/xhtml";

    private static final Element HTML_ELT = new Element("html", NSURI);
    private static final Element HEAD_ELT = new Element("head", NSURI);
    private static final Element TITLE_ELT = new Element("title", NSURI);
    private static final Element META_ELT = new Element("meta", NSURI);
    private static final Element BODY_ELT = new Element("body", NSURI);
    private static final Element TABLE_ELT = new Element("table", NSURI);
    private static final Element TR_ELT = new Element("tr", NSURI);
    private static final Element TH_ELT = new Element("th", NSURI);
    private static final Element TD_ELT = new Element("td", NSURI);
    private static final Element PRE_ELT = new Element("pre", NSURI);
    private static final Element H1_ELT = new Element("h1", NSURI);

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");

    private StreamSerializer serializer;

    public ExportToHtml(Application application) {
        super(application, COMMAND, NAME, new HTMLFileFilter());
        putValue(Action.SHORT_DESCRIPTION, TOOLTIP);
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
    }

    @Override
    protected void writeFooter() throws IOException {
        serializer.writeEndTag(BODY_ELT);
        serializer.writeEndTag(HTML_ELT);
        serializer.flush();
    }

    @Override
    protected void writeHeader(int numberOfRecords) throws IOException {
        serializer = new StreamSerializer(stream);
        final String title = Version.getAsStringWithName() + " HTML Report (" + Marshaller.getImplementationName() + " marshaller)";
        serializer.writeXMLDeclaration();
        serializer.write(new DocType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"));
        serializer.writeStartTag(HTML_ELT);
        serializer.writeNamespaceDeclaration("", NSURI);
        serializer.writeStartTag(HEAD_ELT);
        writeTagged(TITLE_ELT, title);
        writeHttpEquiv("Content-Type", "application/xhtml+xml; charset=utf-8");
        writeHttpEquiv("Content-Language", "en-uk");
        writeMeta("generator", Version.getAsStringWithName());
        serializer.writeEndTag(HEAD_ELT);
        serializer.writeStartTag(BODY_ELT);
        writeTagged(H1_ELT, title);
        serializer.writeStartTag(TABLE_ELT);
        serializer.writeStartTag(TR_ELT);
        writeTagged(TH_ELT, "Timestamp");
        writeTagged(TH_ELT, "Sequence Number");
        writeTagged(TH_ELT, "Type");
        writeTagged(TH_ELT, "Send Subject");
        writeTagged(TH_ELT, "Tracking ID");
        writeTagged(TH_ELT, "Message");
        serializer.writeEndTag(TR_ELT);
    }

    private void writeHttpEquiv(String equiv, String content) throws IOException {
        serializer.writeStartTag(META_ELT);
        serializer.write(new Attribute("http-equiv", NSURI, equiv));
        serializer.write(new Attribute("content", NSURI, content));
        serializer.writeEndTag(META_ELT);
    }

    private void writeMeta(String name, String content) throws IOException {
        serializer.writeStartTag(META_ELT);
        serializer.write(new Attribute("name", NSURI, name));
        serializer.write(new Attribute("content", NSURI, content));
        serializer.writeEndTag(META_ELT);
    }

    @Override
    protected void writeRecord(Record record, int index) throws IOException {
        serializer.writeStartTag(TR_ELT);
        writeTagged(TD_ELT, DATE_FORMATTER.format(new Date(record.getTimestamp())));
        writeTagged(TD_ELT, Long.toString(record.getSequenceNumber()));

        // FIXME: add type back in to export
        //writeTagged(TD_ELT, RecordTypes.getInstance().getFirstMatchingType(record).getName());
        writeTagged(TD_ELT, "[NOT IMPLEMENTED]");

        writeTagged(TD_ELT, record.getSendSubject());
        writeTagged(TD_ELT, record.getTrackingId());
        serializer.writeStartTag(TD_ELT);
        writeTagged(PRE_ELT, Marshaller.marshal("", record.getMessage()));
        serializer.writeEndTag(TD_ELT);
        serializer.writeEndTag(TR_ELT);
    }

    private void writeTagged(Element tag, String pcdata) throws IOException {
        if (pcdata == null) {
            serializer.writeEmptyElementTag(tag);
        } else {
            serializer.writeStartTag(tag);
            serializer.write(new Text(pcdata));
            serializer.writeEndTag(tag);
        }
    }

}
