// Copyright: Copyright © 2006-2010 Ian Phillips and Örjan Lundberg.
// License:   Apache Software License (Version 2.0)

package org.rvsnoop;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.rvsnoop.event.MessageReceivedEvent;
import org.rvsnoop.event.ProjectClosingEvent;
import org.rvsnoop.event.ProjectOpenedEvent;
import rvsnoop.RecordType;
import rvsnoop.RvConnection;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The project service manages all accesses to the project data.
 */
public final class ProjectService {

    private ObjectContainer db;

    private File projectFile;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ProjectService() {
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
