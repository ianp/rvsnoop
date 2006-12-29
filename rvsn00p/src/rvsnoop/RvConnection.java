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

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

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

    public static class AddRecordTask implements Runnable {
        private final Record record;

        public AddRecordTask(Record record) {
            this.record = record;
        }

        public void run() {
            SubjectHierarchy.INSTANCE.addRecord(record);
            MessageLedger.RECORD_LEDGER.add(record);
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
            final RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
            for (int i = 0, imax = conns.length; i < imax; ++i) {
                final RvConnection conn = conns[i];
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
            final RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
            for (int i = 0, imax = conns.length; i < imax; ++i) {
                if (transport.equals(conns[i].transport)) {
                    conns[i].stop();
                    return;
                }
            }
        }

        public void onError(Object tibrvObject, int errorCode, String message, Throwable cause) {
            final String msg = StringUtils.format(ERROR_RV, new Object[] { new Integer(errorCode), message });
            log.error(msg, cause);
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

    private static final Log log = LogFactory.getLog(RvConnection.class);

    /**
     * Property key for the RV daemon setting of this connection.
     */
    public static final String PROP_DAEMON = "daemon";

    /**
     * Property key for the description of this connection.
     */
    public static final String PROP_DESCRIPTION = "description";

    /**
     * Property key for the RV network setting of this connection.
     */
    public static final String PROP_NETWORK = "network";

    /**
     * Property key for the RV service setting of this connection.
     */
    public static final String PROP_SERVICE = "service";

    /**
     * Property key for the subjects that this connection subscribes to.
     */
    public static final String PROP_SUBJECTS = "subjects";

    public static final String XML_ELEMENT = "connection";
    public static final String XML_NS = "http://rvsnoop.org/ns/connection/rv/1";
    private static final String XML_SUBJECT = "subject";

    private static TibrvQueue queue;

    private static TibrvListener queueLimitListener;

    private static synchronized void ensureInitialized() {
        if (queue != null) return;
        try {
            Tibrv.open();
            Tibrv.setErrorCallback(new ErrorCallback());
            queue = new TibrvQueue();
            queue.setName("RvSnoop");
            // No need to keep references to the dispatchers.
            new TibrvDispatcher(Tibrv.defaultQueue());
            new TibrvDispatcher(queue);
        } catch (TibrvException e) {
            final String msg = StringUtils.format(ERROR_RV_OPEN, new Object[] { e.getLocalizedMessage(), new Integer(e.error) });
            log.error(msg, e);
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
     * Constructs a new RvConnection from information contained in an XML fragment.
     *
     * @param element The element that represents the connection.
     * @return The connection.
     */
    public static RvConnection fromXml(Element element) {
        Validate.isTrue(XML_ELEMENT.equals(element.getLocalName()), "The element’s localname must be " + XML_ELEMENT + '.');
        Validate.isTrue(XML_NS.equals(element.getNamespaceURI()), "The element must be in the namespace " + XML_NS + '.');
        final String service = element.getAttributeValue(PROP_SERVICE);
        final String network = element.getAttributeValue(PROP_NETWORK);
        final String daemon = element.getAttributeValue(PROP_DAEMON);
        final RvConnection conn = new RvConnection(service, network, daemon);
        conn.setDescription(element.getAttributeValue(PROP_DESCRIPTION));
        final Elements subjects = element.getChildElements(XML_SUBJECT, XML_NS);
        for (int i = 0, imax = subjects.size(); i < imax; ++i)
            conn.addSubject(subjects.get(i).getValue());
        return conn;
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
        final RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
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
        final RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
        for (int i = 0, imax = conns.length; i < imax; ++i) {
            if (conns[i].getState() == State.PAUSED)
                conns[i].start();
        }
    }

    public static synchronized void shutdown() {
        if (queue == null) return;
        final RvConnection[] conns = (RvConnection[]) Connections.getInstance().toArray();
        for (int i = 0, imax = conns.length; i < imax; ++i)
            conns[i].stop();
        try {
            Tibrv.close();
        } catch (TibrvException e) {
            log.error("There was a problem closing the Rendezvous library.", e);
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
        Validate.notNull(subject, "Subject cannot be null.");
        subject = subject.trim();
        Validate.isTrue(subject.length() > 0, "Subject cannot be empty.");
        if (subjects.containsKey(subject)) return;
        subjects.put(subject, createListener(subject));
        // TODO: Update this to use fireIndexedPropertyChange in SE 5.0.
        changeSupport.firePropertyChange(PROP_SUBJECTS, null, null);
    }

    private synchronized TibrvListener createListener(String subject) {
        if (state == State.STOPPED) return null;
        try {
            ensureInitialized();
            if (log.isDebugEnabled()) {
                log.debug("Creating listener for ‘" + description + "’ on subject ‘" + subject + "’.");
            }
            return new TibrvListener(queue, this, transport, subject, this);
        } catch (TibrvException e) {
            if (log.isErrorEnabled()) {
                log.error("Could not create Rendezvous connection.", e);
            }
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
            hashCode = new HashCodeBuilder()
                .append(network).append(service).append(daemon)
                .toHashCode();
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
        if (state != State.STARTED)
            throw new IllegalStateException("Connection must be started before it can be paused.");
        log.info("Pausing connection: " + description);
        final State oldState = state;
        state = State.PAUSED;
        log.info("Paused connection: " + description);
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
            log.error("Could not set connection description.", e);
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
        log.info("Starting connection: " + description);
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
                log.info("Started connection: " + description);
            } catch (TibrvException e) {
                state = State.STOPPED;
                log.error("The connection named " + description + " could not be started.", e);
            }
        }
        changeSupport.firePropertyChange(State.PROP_STATE, oldState, state);
    }

    public synchronized void stop() {
        if (state == State.STOPPED) return;
        log.info("Stopping connection: " + description);
        for (final Iterator i = subjects.values().iterator(); i.hasNext(); )
            ((TibrvEvent) i.next()).destroy();
        subjects.values().clear();
        transport.destroy();
        transport = null;
        final State oldState = state;
        state = State.STOPPED;
        log.info("Stopped connection: " + description);
        changeSupport.firePropertyChange(State.PROP_STATE, oldState, state);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append(PROP_DESCRIPTION, description)
            .append(PROP_SERVICE, service)
            .append(PROP_NETWORK, network)
            .append(PROP_DAEMON, daemon).toString();
    }

    /**
     * Create an XML fragment that represents this connection.
     *
     * @return the XML fragment that represents this connection.
     */
    public Element toXml() {
        final Element element = new Element(XML_ELEMENT, XML_NS);
        if (description.length() > 0)
            element.addAttribute(new Attribute(PROP_DESCRIPTION, description));
        if (!DEFAULT_SERVICE.equals(service))
            element.addAttribute(new Attribute(PROP_SERVICE, service));
        if (!DEFAULT_NETWORK.equals(network))
            element.addAttribute(new Attribute(PROP_NETWORK, network));
        if (!DEFAULT_DAEMON.equals(daemon))
            element.addAttribute(new Attribute(PROP_DAEMON, daemon));
        final Object[] subjects = this.subjects.keySet().toArray();
        for (int i = 0, imax = subjects.length; i < imax; ++i) {
            final String subject = (String) subjects[i];
            final Element elt = new Element(XML_SUBJECT, XML_NS);
            elt.appendChild(subject);
            element.appendChild(elt);
        }
        return element;
    }

}
