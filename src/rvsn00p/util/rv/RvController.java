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

import rvsn00p.RecentListeners;

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
public class RvController {

    protected final static Map _mapTibrvListeners = new HashMap();
    protected final static Map _mapTibrvTransports = new HashMap();
    protected static TibrvQueue _queue;
    protected static TibrvListener _ignore;
    protected static TibrvListener _error;
    protected static TibrvErrorCallback _errCallBack;

    /**
     * Open the Tibrv connection
     */
    public static void open(final TibrvErrorCallback errCallBack) throws TibrvException {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            _queue = new TibrvQueue();
            _queue.setName("rvsn00pQueue");
            _errCallBack = errCallBack;
            // XXX: Do we need to do anything else with the object reference?
            new TibrvDispatcher(_queue);

            Tibrv.setErrorCallback(_errCallBack);

            _ignore = new TibrvListener(_queue,
                  new IgnoreListenersCallBack(),
                    Tibrv.processTransport(),
                    "_RV.WARN.SYSTEM.QUEUE.LIMIT_EXCEEDED",
                    null);

        } catch (TibrvException e) {
            throw e;
        }
        new TibrvDispatcher(Tibrv.defaultQueue());
    }

    /**
     * Close the rv connection
     */
    public static void close() throws TibrvException {
        Tibrv.close();
    }

    /**
     *   getRvTransport
     */
    public static synchronized TibrvRvdTransport getRvTransport(final RvParameters p)
            throws TibrvException {

        TibrvRvdTransport transport = null;

        try {

            if (_mapTibrvTransports.containsKey(p)) {
                return (TibrvRvdTransport) _mapTibrvTransports.get(p);
            }

            transport = new TibrvRvdTransport(p.getService(),
                    p.getNetwork(),
                    p.getDaemon());

            transport.setDescription(p.getDescription());
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

        final Set s = p.getSubjects();
        final Iterator i = s.iterator();
        while (i.hasNext()) {
            final String n = (String) i.next();
            final String id = String.valueOf(getRvTransport(p).hashCode() + n);
            final TibrvListener lsnr;
            if (!_mapTibrvListeners.containsKey(id)) {
                lsnr = new TibrvListener(_queue,
                        callback,
                        getRvTransport(p),
                        n,
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
     *  Used to hide ADV_NAME="QUEUE.LIMIT_EXCEEDED".
     */
    static class IgnoreListenersCallBack implements TibrvMsgCallback {
        public void onMsg(final TibrvListener tibrvListener, final TibrvMsg tibrvMsg) {
            // no-op
        }
    }

     /**
     *  Used to notify users of Rendezvous errors.
     */
    static class DisplayErrorCallBack implements TibrvMsgCallback {
         protected TibrvErrorCallback _errCallBack;
         public DisplayErrorCallBack(final TibrvErrorCallback errCallBack) {
             this._errCallBack = errCallBack;
         }

        public void onMsg(final TibrvListener tibrvListener, final TibrvMsg tibrvMsg) {
            _errCallBack.onError(tibrvListener,1," - Connection to Tib Rendezvous daemon lost",null);
        }
    }
}
