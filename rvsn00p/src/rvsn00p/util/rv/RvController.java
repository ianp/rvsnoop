/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import com.tibco.tibrv.*;

import java.util.*;

public class RvController {
    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    protected static Map _mapTibrvListeners = new HashMap();
    protected static Map _mapTibrvTransports = new HashMap();

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
    public static void open() throws TibrvException {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            // Try the java implementation instead
            Tibrv.open(Tibrv.IMPL_JAVA);
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
                lsnr = new TibrvListener(Tibrv.defaultQueue(),
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

}
