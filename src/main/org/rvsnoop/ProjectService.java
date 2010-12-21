// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.rvsnoop.event.MessageReceivedEvent;
import org.rvsnoop.event.ProjectClosingEvent;
import org.rvsnoop.event.ProjectOpenedEvent;
import rvsnoop.RecordType;
import rvsnoop.RvConnection;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.prefs.Preferences;

import static com.google.common.collect.Iterables.transform;

/**
 * The project service manages all accesses to the project data.
 */
public final class ProjectService {

    private static final String KEY_RECENT_PROJECTS = "recentProjects";
    private ObjectContainer db;

    private File projectFile;

    private LinkedList<File> recentProjectFiles = new LinkedList<File>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Preferences preferences;

    @Inject
    public ProjectService(Preferences preferences) {
        this.preferences = preferences;
        String recentProjects = preferences.get(KEY_RECENT_PROJECTS, "");
        if (!recentProjects.isEmpty()) {
            for (String s : Splitter.on(' ').omitEmptyStrings().split(recentProjects)) {
                try {
                    File f = new File(new URI(s));
                    if (f.isFile() && f.canRead()) {
                        recentProjectFiles.add(f);
                    }
                } catch (URISyntaxException e) {
                    // ignore the entry if it isn't well formed
                }
            }
        }
        AnnotationProcessor.process(this);
    }

    private <T> Future<List<T>> getAll(final Class<T> clazz) {
        Callable<List<T>> callable = new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return db.query(clazz);
            }
        };
        return executorService.submit(callable);
    }

    public Future<List<RvConnection>> getConnections() {
        return getAll(RvConnection.class);
    }

    public Future<List<RecordType>> getRecordTypes() {
        return getAll(RecordType.class);
    }

    public File getProjectFile() {
        return projectFile;
    }

    public ImmutableList<File> getRecentProjectFiles() {
        return ImmutableList.copyOf(recentProjectFiles);
    }

    public void openProject(final File file) {
        EventBus.publish(new ProjectClosingEvent(this));
        executorService.submit(new Runnable() {
            public void run() {
                while (!db.close()) {}
            }
        });
        executorService.submit(new Runnable() {
            public void run() {
                db = Db4oEmbedded.openFile(file.getPath());
                projectFile = file;
                EventBus.publish(new ProjectOpenedEvent(ProjectService.this));

                recentProjectFiles.remove(file);
                recentProjectFiles.addFirst(file);
                while (recentProjectFiles.size() > 5) { recentProjectFiles.removeLast(); }
                preferences.put(KEY_RECENT_PROJECTS,
                        Joiner.on(' ').join(transform(recentProjectFiles, new Function<File, String>() {
                            public String apply(File input) {
                                if (input == null) {
                                    return "";
                                }
                                return input.toURI().toASCIIString();
                            }
                        })));
            }
        });
    }

    @EventSubscriber
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (db == null) { return; }
        executorService.submit(new Runnable() {
            public void run() {
                db.store(event.getSource());
            }
        });
    }

}
