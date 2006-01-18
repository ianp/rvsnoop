//:File:    RvConnection.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsnoop;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvDispatcher;
import com.tibco.tibrv.TibrvErrorCallback;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvNetTransport;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvaTransport;
import com.tibco.tibrv.TibrvRvdTransport;
import com.tibco.tibrv.TibrvEvent;
import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Describes the set of parameters that an RV transport requires.
 * <p>
 * This class also describes a list of subjects to be subscribed to.
 * <p>
 * RV connections are ‘pseudo-immutable’ object, this means that they have a set
 * of properties which define their equality (and, incidentally, are also used
 * to generate their hash codes) and these cannot be altered after object
 * creation. All other properties may be changes freely at any time. For RV
 * connections the immutable properties are service, network, and daemon.
 * 
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvConnection {

    private static class AddRecordTask implements Runnable {
        private final Record record;

        public AddRecordTask(Record record) {
            this.record = record;
        }

        public void run() {
            SubjectHierarchy.INSTANCE.addRecord(record);
            MessageLedger.INSTANCE.addRecord(record);
            UIManager.INSTANCE.updateStatusLabel();
        }
    }

    /**
     * A list model that allows the list of connections to be observed in a
     * graphical component.
     */
    private static class ConnectionListModel implements ListModel {
        private final EventListenerList listenerList = new EventListenerList();
        ConnectionListModel() {
            super();
        }
        public void addListDataListener(ListDataListener listener) {
            listenerList.add(ListDataListener.class, listener);
        }

        void fireConnectionAdded(RvConnection connection) {
            final EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
            if (listeners == null || listeners.length == 0) return;
            final int index = allConnections.indexOf(connection);
            final ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
            for (int i = 0, imax = listeners.length; i < imax; ++i)
                ((ListDataListener) listeners[i]).intervalAdded(event);
        }

        void fireConnectionRemoved(int index) {
            final EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
            if (listeners == null || listeners.length == 0) return;
            final ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
            for (int i = 0, imax = listeners.length; i < imax; ++i)
                ((ListDataListener) listeners[i]).intervalRemoved(event);
        }
        
        void fireContentsChanged(RvConnection connection) {
            final EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
            if (listeners == null || listeners.length == 0) return;
            final int index = allConnections.indexOf(connection);
            final ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index);
            for (int i = 0, imax = listeners.length; i < imax; ++i)
                ((ListDataListener) listeners[i]).contentsChanged(event);
        }
        
        public Object getElementAt(int index) {
            return allConnections.get(index);
        }
        
        public int getSize() {
            return allConnections.size();
        }

        public void removeListDataListener(ListDataListener listener) {
            listenerList.remove(ListDataListener.class, listener);
        }
    }
    
    private static class ErrorCallback implements TibrvErrorCallback {
        ErrorCallback() {
            super();
        }
        private void invalidateListener(TibrvListener listener) {
            final String listenerSubject = listener.getSubject();
            final List toRemove = new ArrayList();
            for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
                final RvConnection connection = (RvConnection) i.next();
                for (final Iterator j = connection.subjects.keySet().iterator(); j.hasNext();) {
                    final String subject = (String) j.next();
                    if (subject.equals(listenerSubject))
                        toRemove.add(subject);
                }
                for (final Iterator j = toRemove.iterator(); j.hasNext();)
                    connection.removeSubject((String) j.next());
                toRemove.clear();
            }
        }

        private void invalidateTransport(TibrvRvaTransport transport) {
            for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
                final RvConnection connection = (RvConnection) i.next();
                if (transport.equals(connection.transport)) {
                    connection.stop();
                    return;
                }
            }
        }

        public void onError(Object tibrvObject, int errorCode, String message, Throwable cause) {
            final String msg = StringUtils.format(ERROR_RV, new Object[] { new Integer(errorCode), message });
            logger.error(msg, cause);
            if (tibrvObject instanceof TibrvRvaTransport)
                invalidateTransport((TibrvRvaTransport) tibrvObject);
            else if (tibrvObject instanceof TibrvListener)
                invalidateListener((TibrvListener) tibrvObject);
        }
    }
    
    private static class MsgCallback implements TibrvMsgCallback {

        MsgCallback() {
            super();
        }

        public void onMsg(TibrvListener listener, TibrvMsg message) {
            final Object closure = listener != null ? listener.getClosure() : null;
            final RvConnection conn = (RvConnection) (closure instanceof RvConnection ? closure : null);
            if (conn != null && conn.getState() == State.PAUSED)
                return;
            SwingUtilities.invokeLater(new AddRecordTask(new Record(conn, message)));
        }
    }

    private static class NullCallback implements TibrvMsgCallback {
        NullCallback() {
            super();
        }
        public void onMsg(TibrvListener listener, TibrvMsg message) {
            // Do nothing.
        }
    }

    private class PauseAction extends AbstractAction implements ListDataListener {
        private static final long serialVersionUID = 6173501893576290019L;
        PauseAction() {
            super("Pause", Icons.RVD_PAUSED);
            setEnabled(state == State.STARTED);
        }
        public void actionPerformed(ActionEvent e) {
            pause();
        }
        public void contentsChanged(ListDataEvent e) {
            setEnabled(state == State.STARTED);
        }
        public void intervalAdded(ListDataEvent e) {
            // Do nothing.
        }
        public void intervalRemoved(ListDataEvent e) {
            // Do nothing.
        }
    }
    
    private class StartAction extends AbstractAction implements ListDataListener {
        private static final long serialVersionUID = 705371092853316824L;
        StartAction() {
            super("Start", Icons.RVD_STARTED);
            setEnabled(state != State.STARTED);
        }
        public void actionPerformed(ActionEvent e) {
            start();
        }
        public void contentsChanged(ListDataEvent e) {
            setEnabled(state != State.STARTED);
        }
        public void intervalAdded(ListDataEvent e) {
            // Do nothing.
        }
        public void intervalRemoved(ListDataEvent e) {
            // Do nothing.
        }
    }

    private class StopAction extends AbstractAction implements ListDataListener {
        private static final long serialVersionUID = 7442348970606946704L;
        StopAction() {
            super("Stop", Icons.RVD_STOPPED);
            setEnabled(state != State.STOPPED);
        }
        public void actionPerformed(ActionEvent e) {
            stop();
        }
        public void contentsChanged(ListDataEvent e) {
            setEnabled(state != State.STOPPED);
        }
        public void intervalAdded(ListDataEvent e) {
            // Do nothing.
        }
        public void intervalRemoved(ListDataEvent e) {
            // Do nothing.
        }
    }

    private static final List allConnections = new ArrayList();
    
    private static int count;
    
    public static final String DEFAULT_DAEMON = "tcp:7500";
    
    public static final String DEFAULT_NETWORK = "";

    public static final String DEFAULT_SERVICE = "7500";
    
    private static final String DESCRIPTION = " (<a href=\"http://rvsn00p.sf.net\">" + Version.getAsStringWithName() + "</a>)";
      
    private static String ERROR_RV = "An internal Rendezvous error has been encountered, the error code is {0} and the reson given is: {1}.";

    private static String ERROR_RV_OPEN = "The RV libraries could not be loaded and started because: {0}. The Rendezvous error code that was reported was: {1}.";

    private static ConnectionListModel listModel;

    private static final Logger logger = Logger.getLogger(RvConnection.class);
    
    private static final TibrvMsgCallback MESSAGE_CALLBACK = new MsgCallback();

    /**
     * Property key for the description of this connection.
     */
    public static final String PROP_DESCRIPTION = "description";
    
    /**
     * Property key for the subjects that this connection subscribes to.
     */
    public static final String PROP_SUBJECTS = "subjects";

    private static TibrvQueue queue;
    
    private static TibrvListener queueLimitListener;
    
    public static List allConnections() {
        return Collections.unmodifiableList(allConnections);
    }

    /**
     * Factory method to create connections.
     * <p>
     * If a connection with the same parameters already exists then it it
     * returned, otherwise a new connection is created.
     * 
     * @param service The Rendezvous service parameter.
     * @param network The Rendezvous network parameter.
     * @param daemon The Rendezvous daemon parameter.
     * @throws NumberFormatException If service is not an integer (<code>null</code> is OK though).
     * @return The newly created or pre-existing connection.
     * @see #getConnection(String, String, String)
     */
    public static synchronized RvConnection createConnection(String service, String network, String daemon) {
        final RvConnection connection = new RvConnection(service, network, daemon);
        for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
            final Object next = i.next();
            if (connection.equals(next))
                return (RvConnection) next;
        }
        allConnections.add(connection);
        if (listModel != null) listModel.fireConnectionAdded(connection);
        return connection;
    }
    
    public static synchronized RvConnection createDefaultConnection() {
        return createConnection(DEFAULT_SERVICE, DEFAULT_NETWORK, DEFAULT_DAEMON);
    }
    
    public static synchronized void destroyConnection(RvConnection connection) {
        if (connection.state != State.STOPPED) connection.stop();
        final int index = allConnections.indexOf(connection);
        allConnections.remove(index);
        if (listModel != null) listModel.fireConnectionRemoved(index);
    }
    
    private static synchronized TibrvQueue ensureInitialized() {
        if (queue != null) return queue;
        try {
            Tibrv.open();
            Tibrv.setErrorCallback(new ErrorCallback());
            queue = new TibrvQueue();
            queue.setName("rvSnoop");
            // No need to keep references to the dispatchers.
            new TibrvDispatcher(Tibrv.defaultQueue());
            new TibrvDispatcher(queue);
        } catch (TibrvException e) {
            final String msg = StringUtils.format(ERROR_RV_OPEN, new Object[] { e.getLocalizedMessage(), new Integer(e.error) });
            logger.error(msg, e);
            try {
                // try to clean up.
                queue.destroy();
                queue = null;
                Tibrv.close();
            } catch (TibrvException te) {
                // Do nothing for now.
            }
        }
        return queue;
    }
    
    /**
     * Generate a new connection name.
     * 
     * @return The new connection name.
     */
    public static synchronized String gensym() {
        return "connection-" + count++;
    }
    
    /**
     * Get an existing connection.
     * 
     * @param service The Rendezvous service parameter.
     * @param network The Rendezvous network parameter.
     * @param daemon The Rendezvous daemon parameter.
     * @throws NumberFormatException If service is not an integer (<code>null</code> is OK though).
     * @return The existing connection, or <code>null</code> if it does not exist.
     * @see #createConnection(String, String, String)
     */
    public static RvConnection getConnection(String service, String network, String daemon) {
        final RvConnection connection = new RvConnection(service, network, daemon);
        for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
            final Object next = i.next();
            if (connection.equals(next))
                return (RvConnection) next;
        }
        return null;
    }

    public static synchronized ListModel getListModel() {
        if (listModel == null) listModel = new ConnectionListModel();
        return listModel;
    }
    
    /**
     * Add a message.
     * <p>
     * This method hooks into the same add message code as is used in the
     * regular Rendezvous listeners, but is intended for use in loading messages
     * from files or as a rsult of cut-and-paste actions.
     * 
     * @param message The message to add.
     */
    public static void internalOnMsg(TibrvMsg message) {
        MESSAGE_CALLBACK.onMsg(null, message);
    }
    
    public static synchronized void pauseQueue() throws TibrvException {
        if (queue != null) {
            queueLimitListener = new TibrvListener(queue, new NullCallback(), Tibrv.processTransport(), "_RV.WARN.SYSTEM.QUEUE.LIMIT_EXCEEDED", null);
            queue.setLimitPolicy(TibrvQueue.DISCARD_NEW, 1, 1);
        }
        for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
            final RvConnection connection = (RvConnection) i.next();
            if (connection.state == State.STARTED) connection.pause();
        }
    }
    
    public static synchronized void resumeQueue() throws TibrvException {
        ensureInitialized();
        if (queue != null) {
            queue.setLimitPolicy(TibrvQueue.DISCARD_NONE, 0, 0);
            if (queueLimitListener != null) {
                queueLimitListener.destroy();
                queueLimitListener = null;
            }
        }
        for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
            final RvConnection connection = (RvConnection) i.next();
            if (connection.state == State.PAUSED) connection.start();
        }
    }

    public static void shutdown() {
        if (queue == null) return;
        for (final Iterator i = allConnections.iterator(); i.hasNext(); ) {
            final RvConnection connection = (RvConnection) i.next();
            if (connection.state != State.STOPPED) connection.stop();
        }
        try {
            Tibrv.close();
        } catch (TibrvException e) {
            logger.error("There was a problem closing the Rendezvous library.", e);
        }
        queue = null;
    }

    /**
     * The Rendezvous daemon parameter.
     */
    private final String daemon;
    
    /**
     * A description of this connection.
     */
    private String description = "";
    
    /**
     * The cached hash code value.
     */
    private int hashCode;
    
    /**
     * The Rendezvous network parameter.
     */
    private final String network;
    
    /**
     * The Rendezvous service parameter.
     */
    private final String service;
    
    private Action startAction, stopAction, pauseAction;
    
    /**
     * The current state that the connection is in.
     */
    private State state = State.STOPPED;

    /**
     * The set of subjects to subscribe to.
     * <p>
     * The map values are the RV Listeners, which may be <code>null</code>.
     */
    private final Map subjects = new TreeMap();
    
    private TibrvNetTransport transport;

    /**
     * Create a new Rendezvous connection.
     * 
     * @param service The Rendezvous service parameter.
     * @param network The Rendezvous network parameter.
     * @param daemon The Rendezvous daemon parameter.
     * @throws NumberFormatException If service is not an integer (<code>null</code> is OK though).
     */
    private RvConnection(String service, String network, String daemon) {
        super();
        this.description = gensym();
        this.service = service != null ? service : DEFAULT_SERVICE;
        this.network = network != null ? network : DEFAULT_NETWORK;
        this.daemon  = daemon  != null ? daemon  : DEFAULT_DAEMON;
        Integer.parseInt(service);
    }
    
    public synchronized void addSubject(String subject) {
        if (subject == null) throw new NullPointerException();
        subject = subject.trim();
        if (subject.length() > 0 && !subjects.containsKey(subject))
            subjects.put(subject, createListener(subject));
        if (listModel != null) listModel.fireContentsChanged(this);
    }
    
    public synchronized void addSubjects(List subjects) {
        assert subjects != null;
        for (final Iterator i = subjects.iterator(); i.hasNext(); ) {
            final String subject = ((String) i.next()).trim();
            if (this.subjects.containsValue(subject)) continue;
            if (Logger.isDebugEnabled()) logger.debug("RvConnection.setSubjects putting '" + subject + "'");
            this.subjects.put(subject, createListener(subject));
        }
        if (listModel != null) listModel.fireContentsChanged(this);
    }
    
    private TibrvListener createListener(String subject) {
        if (state == State.STOPPED) return null;
        try {
            final TibrvQueue queue = ensureInitialized();
            if (Logger.isDebugEnabled()) logger.debug("Creating listener for '" + description + "' on subject '" + subject + "'.");
            return new TibrvListener(queue, MESSAGE_CALLBACK, transport, subject, this);
        } catch (TibrvException e) {
            logger.error("Could not create Rendezvous connection.", e);
            return null;
        }
    }
    
    private void createTransport() throws TibrvException {
        ensureInitialized();
        transport = new TibrvRvdTransport(service, network, daemon);
        transport.setDescription(description + DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RvConnection)) return false;
        final RvConnection that = (RvConnection) o;
        return service.equals(that.service)
            && network.equals(that.network)
            && daemon.equals(that.daemon);
    }
    
    /**
     * Get the Rendezvous daemon parameter.
     */
    public String getDaemon() {
        return daemon;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Get the Rendezvous network parameter.
     */
    public String getNetwork() {
        return network;
    }

    public int getNumSubjects() {
        return subjects.size();
    }

    /**
     * @return Returns the pauseAction.
     */
    public synchronized Action getPauseAction() {
        if (pauseAction == null) pauseAction = new PauseAction();
        return pauseAction;
    }

    /**
     * Get the Rendezvous service parameter.
     */
    public String getService() {
        return service;
    }
    
    /**
     * @return Returns the startAction.
     */
    public synchronized Action getStartAction() {
        if (startAction == null) startAction = new StartAction();
        return startAction;
    }

    public State getState() {
        return state;
    }

    /**
     * @return Returns the stopAction.
     */
    public synchronized Action getStopAction() {
        if (stopAction == null) stopAction = new StopAction();
        return stopAction;
    }

    /**
     * Get the set of subjects that this connection subscribes to.
     * 
     * @return A sorted (natural order) set of subjects. Elements may be cast to {@link String}.
     */
    public Set getSubjects(){
         return Collections.unmodifiableSet(subjects.keySet());
    }
    
    /**
     * Returns an hash code derived from the network, service and daemon attributes.
     * 
     * @see Object#hashCode()
     */
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = new StringBuffer()
                .append(network).append(service).append(daemon)
                .hashCode();
        }
        return hashCode;
    }

    private synchronized void pause() {
        assert state == State.STARTED : "Cannot pause if not started.";
        logger.info("Pausing connection: " + description);
        state = State.PAUSED;
        logger.info("Paused connection: " + description);
        if (listModel != null) listModel.fireContentsChanged(this);
    }

    public synchronized void publish(Record record) throws TibrvException {
        if (transport == null) createTransport();
        transport.send(record.getMessage());
    }
    
    public void removeAllSubbjects() {
        if (state != State.STOPPED)
            for (final Iterator i = this.subjects.values().iterator(); i.hasNext(); )
                ((TibrvEvent) i.next()).destroy();
        this.subjects.clear();
    }
    
    private void removeSubject(String subject) {
        if (subject == null) return;
        subject = subject.trim();
        if (subject.length() > 0) {
            final TibrvListener listener = (TibrvListener) subjects.remove(subject);
            if (listener != null) listener.destroy();
        }
        if (listModel != null) listModel.fireContentsChanged(this);
    }

    public void setDescription(String description) {
        try {
            if (transport != null)
                transport.setDescription(description + DESCRIPTION);
            this.description = description != null ? description : "";
            if (listModel != null) listModel.fireContentsChanged(this);
        } catch (TibrvException e) {
            logger.error("Could not set connection description.", e);
        }
    }

    public synchronized void start() {
        assert state != State.STARTED : "Cannot start if already started.";
        logger.info("Starting connection: " + description);
        if (state == State.PAUSED) {
            state = State.STARTED;
        } else {
            state = State.STARTED;
            try {
                createTransport();
                for (final Iterator i = subjects.keySet().iterator(); i.hasNext(); ) {
                    final String subject = (String) i.next();
                    subjects.put(subject, createListener(subject));
                }
                logger.info("Started connection: " + description);
            } catch (TibrvException e) {
                state = State.STOPPED;
                logger.error("The connection named " + description + " could not be started.", e);
            }
        }
        if (listModel != null) listModel.fireContentsChanged(this);
    }

    private synchronized void stop() {
        assert state != State.STOPPED : "Cannot stop if already stopped.";
        logger.info("Stopping connection: " + description);
        for (final Iterator i = subjects.values().iterator(); i.hasNext(); )
            ((TibrvEvent) i.next()).destroy();
        subjects.values().clear();
        transport.destroy();
        transport = null;
        state = State.STOPPED;
        logger.info("Stopped connection: " + description);
        if (listModel != null) listModel.fireContentsChanged(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("[RvConnection: description=").append(description);
        buffer.append(", service=").append(service);
        buffer.append(", network=").append(network);
        buffer.append(", daemon=").append(daemon).append("]");
        return buffer.toString();
    }

}
