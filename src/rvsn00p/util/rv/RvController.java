//:File:    RvController.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import rvsn00p.RecentListeners;
import rvsn00p.Version;
import rvsn00p.viewer.RvSnooperGUI;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvDispatcher;
import com.tibco.tibrv.TibrvErrorCallback;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvQueue;
import com.tibco.tibrv.TibrvRvdTransport;
import com.tibco.tibrv.TibrvTransport;

/**
 * Controls all listeners to the rendezvous bus.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvController {

    private static final String DESCRIPTION = "<a href=\"http://rvsn00p.sf.net\">" + Version.getAsStringWithName() + "</a>";

    private final static Map _mapTibrvListeners = new HashMap();
    private final static Map _mapTibrvTransports = new HashMap();
    private static TibrvQueue _queue;
    private static TibrvErrorCallback _errCallBack;
    
    private static boolean isOpen = false;

    private static void open() throws TibrvException {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            _queue = new TibrvQueue();
            _queue.setName("rvsn00pQueue");
            // XXX: Do we need to do anything else with the object reference?
            new TibrvDispatcher(_queue);

            Tibrv.setErrorCallback(_errCallBack);

            new TibrvListener(_queue, new TibrvMsgCallback() {
                public void onMsg(final TibrvListener listener, final TibrvMsg message) {
                    // Do nothing.
                }
            }, Tibrv.processTransport(), "_RV.WARN.SYSTEM.QUEUE.LIMIT_EXCEEDED", null);
            new TibrvDispatcher(Tibrv.defaultQueue());
        } catch (TibrvException e) {
            final String[] buffer = new String[3];
            buffer[0] = "Failed to initialize Rendezvous. ";
            buffer[1] = e.getMessage();
            if (System.getProperty("os.name").startsWith("Windows"))
                buffer[2] = "Check that the ‘tibrv\\bin’ is in your PATH variable.";
            else
                buffer[2] = "Check that the ‘tibrv/lib’ is in your system’s library path variable.";
            JOptionPane.showMessageDialog(RvSnooperGUI.getAppFrame(), buffer, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Close the rv connection
     */
    public static void close() throws TibrvException {
        if (isOpen) Tibrv.close();
    }

    /**
     *   getRvTransport
     */
    public static synchronized TibrvRvdTransport getRvTransport(final RvParameters p)
            throws TibrvException {

        TibrvRvdTransport transport = null;

        try {

            if (_mapTibrvTransports.containsKey(p))
                return (TibrvRvdTransport) _mapTibrvTransports.get(p);

            transport = new TibrvRvdTransport(p.getService(),
                    p.getNetwork(),
                    p.getDaemon());

            if (p.getDescription().length() > 0)
                transport.setDescription(p.getDescription() + " (" + DESCRIPTION + ")");
            else
                transport.setDescription(DESCRIPTION);
            _mapTibrvTransports.put(p, transport);

            // display an error message if the rvrd is disconnected
            new TibrvListener(_queue,
                  new DisplayErrorCallBack(_errCallBack),
                    transport,
                    "_RV.WARN.SYSTEM.RVD.DISCONNECTED",
                    null);
            return transport;

        } catch (TibrvException ex) {
            throw ex;
        }
    }


    public static synchronized void startRvListener(final RvParameters p, final TibrvMsgCallback callback)
            throws TibrvException {
        if (!isOpen) open();

        if (_mapTibrvTransports.containsKey(p)) {
            // update subjects
            final Iterator i = _mapTibrvTransports.keySet().iterator();

            while (i.hasNext()) {
                final RvParameters par;
                par = (RvParameters) i.next();
                if (par.equals(p)) {
                    par.setSubjects(p.getSubjects());
                }
            }
        }

        final String[] subjects = p.getSubjects();
        for (int i = 0, imax = subjects.length; i < imax; ++i) {
            final String subject = subjects[i];
            final String id = String.valueOf(getRvTransport(p).hashCode() + subject);
            final TibrvListener lsnr;
            if (!_mapTibrvListeners.containsKey(id)) {
                lsnr = new TibrvListener(_queue,
                        callback,
                        getRvTransport(p),
                        subject,
                        null);

                _mapTibrvListeners.put(id, lsnr);
            }
        }
        RecentListeners.getInstance().add(p);
    }

    public static synchronized Set getListeners() {
        return _mapTibrvListeners.keySet();
    }

    public static synchronized Set getTransports() {
        return _mapTibrvTransports.keySet();
    }

    public static synchronized void stopRvListener(final RvParameters p) {

        if (_mapTibrvListeners.containsKey(p)) {

            final TibrvListener lsnr = (TibrvListener) _mapTibrvListeners.get(p);
            lsnr.destroy();


            _mapTibrvListeners.remove(p.getSubjectsAsString());
        }
    }
    
    public static void setErrorCallback(TibrvErrorCallback callback) {
        _errCallBack = callback;
    }

    public static synchronized void shutdownAll() {

        Iterator i = _mapTibrvListeners.keySet().iterator();

        while (i.hasNext()) {
            if (i != null) {
                ((TibrvListener) (_mapTibrvListeners.get(i.next()))).destroy();
            }
        }

        i = _mapTibrvTransports.keySet().iterator();

        while (i.hasNext()) {
            if (i != null) {
                ((TibrvTransport) _mapTibrvTransports.get(i.next())).destroy();
            }
        }

        _mapTibrvListeners.clear();
        _mapTibrvTransports.clear();
    }

    public static synchronized void pauseAll() throws TibrvException {

        if (_queue != null) {
            _queue.setLimitPolicy(TibrvQueue.DISCARD_NEW, 1, 1);
        }

    }

    public static synchronized void resumeAll() throws TibrvException {

        if (_queue != null) {
            _queue.setLimitPolicy(TibrvQueue.DISCARD_NONE, 0, 0);
        }
    }

     /**
     *  Used to notify users of Rendezvous errors.
     */
    static class DisplayErrorCallBack implements TibrvMsgCallback {
        private final TibrvErrorCallback _errCallBack;
        public DisplayErrorCallBack(final TibrvErrorCallback errCallBack) {
            super();
            this._errCallBack = errCallBack;
        }

        public void onMsg(final TibrvListener tibrvListener, final TibrvMsg tibrvMsg) {
            _errCallBack.onError(tibrvListener, 1, " - Connection to Tib Rendezvous daemon lost", null);
        }
    }

    /**
     * Do not instantiate.
     */
    private RvController() {
        throw new UnsupportedOperationException();
    }

}
