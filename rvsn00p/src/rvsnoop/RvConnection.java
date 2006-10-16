//:File:    RvConnection.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:FileID:  $Id$
package rvsnoop;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import rvsnoop.ui.Icons;
import rvsnoop.ui.UIManager;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvDispatcher;
import com.tibco.tibrv.TibrvErrorCallback;
import com.tibco.tibrv.TibrvEvent;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvNetTransport;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvaTransport;
import com.tibco.tibrv.TibrvRvdTransport;

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
public final class RvConnection implements TibrvMsgCallback {

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

    private static class ErrorCallback implements TibrvErrorCallback {
        ErrorCallback() {
            super();
        }
        private void invalidateListener(TibrvListener listener) {
            final String listenerSubject = listener.getSubject();
            final List toRemove = new ArrayList();
            RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
            for (int i = 0, imax = conns.length; i < imax; ++i) {
                RvConnection conn = conns[i];
                for (final Iterator j = conn.subjects.keySet().iterator(); j.hasNext();) {
                    final String subject = (String) j.next();
                    if (subject.equals(listenerSubject))
                        toRemove.add(subject);
                }
                for (final Iterator j = toRemove.iterator(); j.hasNext();)
                    conn.removeSubject((String) j.next());
                toRemove.clear();
            }
        }

        private void invalidateTransport(TibrvRvaTransport transport) {
            RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
            for (int i = 0, imax = conns.length; i < imax; ++i) {
                if (transport.equals(conns[i].transport)) {
                    conns[i].stop();
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

    private static class NullCallback implements TibrvMsgCallback {
        NullCallback() {
            super();
        }
        public void onMsg(TibrvListener listener, TibrvMsg message) {
            // Do nothing.
        }
    }

    private class PauseAction extends AbstractAction {
        static final long serialVersionUID = 6173501893576290019L;
        PauseAction() {
            super("Pause", Icons.RVD_PAUSED);
        }
        public void actionPerformed(ActionEvent e) {
            pause();
        }
        public boolean isEnabled() {
            return getState() == State.STARTED;
        }
    }

    private class RemoveAction extends AbstractAction {
        static final long serialVersionUID = 6348774322798989737L;
        RemoveAction() {
            super("Remove");
        }
        public void actionPerformed(ActionEvent e) {
            synchronized (RvConnection.this) {
                stop();
                Connections.getInstance().remove(RvConnection.this);
            }
        }
    }

    private class StartAction extends AbstractAction {
        static final long serialVersionUID = 705371092853316824L;
        StartAction() {
            super("Start", Icons.RVD_STARTED);
        }
        public void actionPerformed(ActionEvent e) {
            start();
        }
        public boolean isEnabled() {
            return getState() != State.STARTED;
        }
    }

    private class StopAction extends AbstractAction {
        static final long serialVersionUID = 7442348970606946704L;
        StopAction() {
            super("Stop", Icons.RVD_STOPPED);
        }
        public void actionPerformed(ActionEvent e) {
            stop();
        }
        public boolean isEnabled() {
            return getState() != State.STOPPED;
        }
    }

    private static int count;

    public static final String DEFAULT_DAEMON = "tcp:7500";

    public static final String DEFAULT_NETWORK = "";

    public static final String DEFAULT_SERVICE = "7500";

    private static final String DESCRIPTION = " (<a href=\"http://rvsnoop.org/\">" + Version.getAsStringWithName() + "</a>)";

    private static String ERROR_RV = "An internal Rendezvous error has been encountered, the error code is {0} and the reson given is: {1}.";

    private static String ERROR_RV_OPEN = "The RV libraries could not be loaded and started because: {0}. The Rendezvous error code that was reported was: {1}.";

    private static final Logger logger = Logger.getLogger(RvConnection.class);

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

    private static synchronized void ensureInitialized() {
        if (queue != null) return;
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
     * Add a message.
     * <p>
     * This method hooks into the same add message code as is used in the
     * regular Rendezvous listeners, but is intended for use in loading messages
     * from files or as a result of cut-and-paste actions.
     *
     * @param message The message to add.
     */
    public static void internalOnMsg(TibrvMsg message) {
        SwingUtilities.invokeLater(new AddRecordTask(new Record(null, message)));
    }

    /**
     * Pause the Rendezvous message queue.
     * <p>
     * All of the Rendezvous connections are backed by a single message queue,
     * this method will pause the queue. This means that the queue is told to
     * discard all new messages and a listener is installed which suppresses the
     * advisory that is generated for discarded messages.
     * <p>
     * Additionally, all connections are moved to the paused state.
     *
     * @throws TibrvException
     */
    public static synchronized void pauseQueue() throws TibrvException {
        if (queue != null) {
            queueLimitListener = new TibrvListener(queue, new NullCallback(), Tibrv.processTransport(), "_RV.WARN.SYSTEM.QUEUE.LIMIT_EXCEEDED", null);
            queue.setLimitPolicy(TibrvQueue.DISCARD_NEW, 1, 1);
        }
        RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
        for (int i = 0, imax = conns.length; i < imax; ++i) {
            if (conns[i].getState() == State.STARTED)
                conns[i].pause();
        }
    }

    /**
     * Resume the Rendezvous message queue.
     * <p>
     *
     *
     * @throws TibrvException
     */
    public static synchronized void resumeQueue() throws TibrvException {
        ensureInitialized();
        if (queue != null) {
            queue.setLimitPolicy(TibrvQueue.DISCARD_NONE, 0, 0);
            if (queueLimitListener != null) {
                queueLimitListener.destroy();
                queueLimitListener = null;
            }
        }
        RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
        for (int i = 0, imax = conns.length; i < imax; ++i) {
            if (conns[i].getState() == State.PAUSED)
                conns[i].start();
        }
    }

    public static synchronized void shutdown() {
        if (queue == null) return;
        RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
        for (int i = 0, imax = conns.length; i < imax; ++i)
            conns[i].stop();
        try {
            Tibrv.close();
        } catch (TibrvException e) {
            logger.error("There was a problem closing the Rendezvous library.", e);
        }
        queue = null;
    }

    /** The Rendezvous daemon parameter. */
    private final String daemon;

    /** A description of this connection. */
    private String description = "";

    /** The cached hash code value. */
    private int hashCode;

    /** The Rendezvous network parameter. */
    private final String network;

    /** The Rendezvous service parameter. */
    private final String service;

    /**
     * The 'parent' list for this connection.
     * <p>
     * Connections can not be activated (started, paused, etc.) until they
     * have been added to a parent list, this is because there must be a UI
     * based way to access the connection before it is allowed to consume
     * resources.
     */
    private Connections parentList;

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private Action startAction, stopAction, pauseAction, removeAction;

    /** The current state that the connection is in. */
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
    public RvConnection(String service, String network, String daemon) {
        super();
        this.description = gensym();
        this.service = service != null ? service : DEFAULT_SERVICE;
        this.network = network != null ? network : DEFAULT_NETWORK;
        this.daemon  = daemon  != null ? daemon  : DEFAULT_DAEMON;
        Integer.parseInt(this.service);
    }

    public RvConnection() {
        this(DEFAULT_SERVICE, DEFAULT_NETWORK, DEFAULT_DAEMON);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public synchronized void addSubject(String subject) {
        if (subject == null) throw new NullPointerException("Cannot set null subject.");
        subject = subject.trim();
        if (subject.length() == 0) throw new IllegalArgumentException("Cannot set empty subject name.");
        if (!subjects.containsKey(subject))
            subjects.put(subject, createListener(subject));
        // TODO: Update this to use fireIndexedPropertyChange in SE 5.0.
        changeSupport.firePropertyChange(PROP_SUBJECTS, null, null);
    }

    private synchronized TibrvListener createListener(String subject) {
        if (state == State.STOPPED) return null;
        try {
            ensureInitialized();
            if (Logger.isDebugEnabled()) logger.debug("Creating listener for '" + description + "' on subject '" + subject + "'.");
            return new TibrvListener(queue, this, transport, subject, this);
        } catch (TibrvException e) {
            logger.error("Could not create Rendezvous connection.", e);
            return null;
        }
    }

    private synchronized void createTransport() throws TibrvException {
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
     * Get an action that pauses this connection when invoked.
     *
     * @return the action.
     */
    public synchronized Action getPauseAction() {
        if (pauseAction == null) pauseAction = new PauseAction();
        return pauseAction;
    }

    /**
     * Get an action that removes this connection when invoked.
     *
     * @return the action.
     */
    public synchronized Action getRemoveAction() {
        if (removeAction == null) removeAction = new RemoveAction();
        return removeAction;
    }

    /**
     * Get the Rendezvous service parameter.
     */
    public String getService() {
        return service;
    }

    /**
     * Get an action that starts this connection when invoked.
     *
     * @return the action.
     */
    public synchronized Action getStartAction() {
        if (startAction == null) startAction = new StartAction();
        return startAction;
    }

    public synchronized State getState() {
        return state;
    }

    /**
     * Get an action that stops this connection when invoked.
     *
     * @return the action.
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
    public Set getSubjects() {
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

    public void onMsg(TibrvListener listener, TibrvMsg message) {
        final Object closure = listener != null ? listener.getClosure() : null;
        final RvConnection conn = (RvConnection) (closure instanceof RvConnection ? closure : null);
        if (conn != null && conn.getState() == State.PAUSED)
            return;
        SwingUtilities.invokeLater(new AddRecordTask(new Record(conn, message)));
    }

    public synchronized void pause() {
        if (state != State.STARTED) throw new IllegalStateException("Cannot pause if not started.");
        logger.info("Pausing connection: " + description);
        final State oldState = state;
        state = State.PAUSED;
        logger.info("Paused connection: " + description);
        changeSupport.firePropertyChange(State.PROP_STATE, oldState, state);
    }

    public synchronized void publish(Record record) throws TibrvException {
        if (transport == null) createTransport();
        transport.send(record.getMessage());
    }

    public synchronized void removeAllSubbjects() {
        if (state != State.STOPPED)
            for (final Iterator i = subjects.values().iterator(); i.hasNext(); )
                ((TibrvEvent) i.next()).destroy();
        subjects.clear();
        // TODO: Update this to use fireIndexedPropertyChange in SE 5.0.
        changeSupport.firePropertyChange(PROP_SUBJECTS, null, null);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    private synchronized void removeSubject(String subject) {
        if (subject == null) return;
        subject = subject.trim();
        if (subject.length() > 0) {
            final TibrvListener listener = (TibrvListener) subjects.remove(subject);
            if (listener != null) listener.destroy();
        }
        // TODO: Update this to use fireIndexedPropertyChange in SE 5.0.
        changeSupport.firePropertyChange(PROP_SUBJECTS, null, null);
    }

    public synchronized void setDescription(String description) {
        try {
            if (transport != null)
                transport.setDescription(description + DESCRIPTION);
            final String oldDescription = description;
            this.description = description != null ? description : "";
            changeSupport.firePropertyChange(PROP_DESCRIPTION, oldDescription, this.description);
        } catch (TibrvException e) {
            logger.error("Could not set connection description.", e);
        }
    }

    /**
     * This is called by the connections list when this connection is added to
     * it.
     * <p>
     * A connection which is not a member of a connection list may not be
     * started. Further, the connection list will ensure that the connection is
     * stopped before it is removed from the list.
     *
     * @param connection The connection list.
     */
    protected void setParentList(Connections connection) {
        if (connection == null && state != State.STOPPED)
            throw new IllegalStateException("Connection not stopped!");
        parentList = connection;
    }

    public synchronized void start() {
        if (parentList == null) throw new IllegalStateException("Not added to a connection list!");
        if (state == State.STARTED) throw new IllegalStateException("Cannot start if already started.");
        logger.info("Starting connection: " + description);
        final State oldState = state;
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
        changeSupport.firePropertyChange(State.PROP_STATE, oldState, state);
    }

    public synchronized void stop() {
        if (state == State.STOPPED) return;
        logger.info("Stopping connection: " + description);
        for (final Iterator i = subjects.values().iterator(); i.hasNext(); )
            ((TibrvEvent) i.next()).destroy();
        subjects.values().clear();
        transport.destroy();
        transport = null;
        final State oldState = state;
        state = State.STOPPED;
        logger.info("Stopped connection: " + description);
        changeSupport.firePropertyChange(State.PROP_STATE, oldState, state);
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
