/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import com.tibco.tibrv.*;
import com.tibco.tibrv.TibrvQueue;

import java.util.*;

/**
 * Controls all listeners to the rendezvous bus.
 */
public class RvController {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected static Map _mapTibrvListeners = new HashMap();
    protected static Map _mapTibrvTransports = new HashMap();
    protected static TibrvQueue _queue;
    protected static TibrvListener _ignore;
    protected static TibrvListener _error;
    protected static TibrvErrorCallback _errCallBack;


    //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------
    //   Public Methods:
    //--------------------------------------------------------------------------
    /**
     * Open the Tibrv connection
     */
    public static void open(TibrvErrorCallback errCallBack) throws TibrvException {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            _queue = new TibrvQueue();
            _queue.setName("rvsn00pQueue");
            _errCallBack = errCallBack;
            TibrvDispatcher rvd = new TibrvDispatcher(_queue);

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


    public static synchronized void startRvListener(final RvParameters p, TibrvMsgCallback callback)
            throws TibrvException {


        if (_mapTibrvTransports.containsKey(p)) {
            // update subjects
            Iterator i = _mapTibrvTransports.keySet().iterator();

            while (i.hasNext()) {
                RvParameters par;
                par = (RvParameters) i.next();
                if (par.equals(p)) {
                    par.setSubjects(p.getSubjects());
                }
            }
        }

        Set s = p.getSubjects();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            String n = (String) i.next();
            String id = String.valueOf(getRvTransport(p).hashCode() + n);
            TibrvListener lsnr;
            if (!_mapTibrvListeners.containsKey(id)) {
                lsnr = new TibrvListener(_queue,
                        callback,
                        getRvTransport(p),
                        n,
                        null);



                _mapTibrvListeners.put(id, lsnr);
            }
        }


    }

    public static synchronized Set getListeners() {
        return _mapTibrvListeners.keySet();
    }

    public static synchronized Set getTransports() {
        return _mapTibrvTransports.keySet();
    }

    public static synchronized void stopRvListener(final RvParameters p) {

        if (_mapTibrvListeners.containsKey(p)) {

            TibrvListener lsnr = (TibrvListener) _mapTibrvListeners.get(p);
            lsnr.destroy();


            _mapTibrvListeners.remove(p.getSubject());
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

    //--------------------------------------------------------------------------
    //   Nested Top-Level Classes or Interfaces:
    //--------------------------------------------------------------------------

    /**
     *  Used to hide ADV_NAME="QUEUE.LIMIT_EXCEEDED".
     */
    static class IgnoreListenersCallBack implements TibrvMsgCallback {
        public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
        }
    }

     /**
     *  Used to notify users of Rendezvous errors.
     */
    static class DisplayErrorCallBack implements TibrvMsgCallback {
         protected TibrvErrorCallback _errCallBack;
         public DisplayErrorCallBack(TibrvErrorCallback errCallBack) {
             this._errCallBack = errCallBack;
         }

        public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
            _errCallBack.onError(tibrvListener,1," - Connection to Tib Rendezvous daemon lost",null);
        }
    }
}


