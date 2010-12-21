// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import rvsnoop.SubjectElement;
import rvsnoop.SubjectHierarchy;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Handles all of the project specific settings.
 */
public final class Project {

    private static final String NAMESPACE = "http://rvsnoop.org/ns/rvsnoop/1";

    private final File projectDirectory;

    public Project(File file) {
        this.projectDirectory = file;
    }

    public File getDirectory() {
        return projectDirectory;
    }

    public void store(Application application) throws IOException {
        storeMetadata();
        storeSubjects(application.getSubjectHierarchy(),
                application.getFrame().getSubjectExplorer());
    }

    private void storeMetadata() throws IOException {
        final File file = new File(projectDirectory, "RvSnoop Project.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            new XMLBuilder(stream, NAMESPACE)
                .startTag("project", NAMESPACE)
                    .startTag("creationDate", NAMESPACE)
                        .pcdata(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()))
                        .endTag()
                    .startTag("ledger", NAMESPACE)
                        .attribute("class", InMemoryLedger.class.getName())
                        .endTag()
                .endTag().close();
        } finally {
            closeQuietly(stream);
        }
    }

    private void storeSubject(SubjectElement element, XMLBuilder builder, JTree tree) throws IOException {
        boolean expanded = tree == null || tree.isExpanded(new TreePath(element.getPath()));
        builder.startTag("subject", XMLBuilder.NS_CONNECTIONS)
            .attribute("name", element.getElementName())
            .attribute("selected", Boolean.toString(element.isSelected()))
            .attribute("expanded", Boolean.toString(expanded));
        for (Enumeration e = element.children(); e.hasMoreElements(); ) {
            storeSubject((SubjectElement) e.nextElement(), builder, tree);
        }
        builder.endTag();
    }

    public void storeSubjects(SubjectHierarchy subjects, JTree tree) throws IOException {
        final File file = new File(projectDirectory, "Subjects.xml");
        final OutputStream stream = new FileOutputStream(file);
        try {
            final XMLBuilder builder = new XMLBuilder(stream, NAMESPACE)
                .namespace(XMLBuilder.PREFIX_RENDEZVOUS, XMLBuilder.NS_RENDEZVOUS)
                .startTag("subjects", NAMESPACE);
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) subjects.getRoot();
        for (Enumeration e = root.children(); e.hasMoreElements(); ) {
            storeSubject((SubjectElement) e.nextElement(), builder, tree);
        }
        builder.endTag().close();
        } finally {
            closeQuietly(stream);
        }
    }

}
