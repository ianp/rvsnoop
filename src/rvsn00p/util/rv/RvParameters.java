/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;

public class RvParameters implements Cloneable, Serializable {

    //--------------------------------------------------------------------------
    //   Constants:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Protected Variables:
    //--------------------------------------------------------------------------
    /**
     * Used for "lazy" initialization of _hashCode
     */
    protected int _hashCode = 0;

    protected String _network;
    protected String _service;
    protected String _deamon;
    protected boolean _displayRvParameters;
    protected String _subject;
    protected String _description;

     //--------------------------------------------------------------------------
    //   Private Variables:
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //   Constructors:
    //--------------------------------------------------------------------------

    public RvParameters() {

        this._network = "";
        this._service = "7500";
        this._deamon = "tcp:7500";
        this._displayRvParameters = true;
        this._subject = "DEV.>";
        this._description = "";

        calcHashCode();
    }

    public RvParameters(String _subject, String _service, String _network, boolean _displayRvParameters, String _deamon, String _description) {
        this._subject = _subject;
        this._service = _service;
        this._network = _network;
        this._displayRvParameters = _displayRvParameters;
        this._deamon = _deamon;
        this._description = _description;

        calcHashCode();
    }


    //--------------------------------------------------------------------------
    //   Public methods:
    //--------------------------------------------------------------------------

    public String getDescription() {

          return _description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    /**
     * Calculates an hascode using the Network, Service and Deamon values
     */
    protected void calcHashCode() {
        String hcstr = new String();

        if (_network != null) {
            hcstr += _network;
        }

        if (_service != null) {
            hcstr += _service;
        }

        if (_deamon != null) {
            hcstr += _deamon;
        }

          if (_subject != null) {
            hcstr += _subject;
        }

        _hashCode = hcstr.hashCode();
    }

    public String getNetwork() {
        return _network;
    }



    public void setNetwork(String _network) {
        this._network = _network;
        calcHashCode();
    }

    public String getService() {
        return _service;
    }

    public void setService(String in_service) {
        this._service = in_service;
        calcHashCode();
    }

    public String getDaemon() {
        return _deamon;
    }

    public void setDeamon(String _deamon) {
        this._deamon = _deamon;
        calcHashCode();
    }

    public void configureByLineString(String lineString) {

        int where = 0;

        String[] results = new String[4];

        StringTokenizer st = new StringTokenizer(lineString, "|", true);

        int i = 0;
        // stuff each token into the current user
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if ("|".equals(s)) {
                if (i++ >= 4)
                    throw new IllegalArgumentException("Input line " +
                                                       lineString + " has too many fields");
                continue;
            }
            if (s != null) {
                results[i] = s;
            } else {
                results[i] = " ";
            }
        }

        this._deamon =  results[0] ;
        this._service = results[1] ;
        this._network = results[2] ;
        this._subject = results[3] ;

        calcHashCode();
    }

    public String toString() {
        String sRetval = new String(this._deamon);
        sRetval += "|";
        if (_network != null) {
            sRetval += _network;
        }

        sRetval += "|";

        if (_service != null) {
            sRetval += _service;
        }
        sRetval += "|";
        if (_subject != null) {
            sRetval += _subject;
        }

        return sRetval;
    }

    public boolean isDisplayRvParameters() {
        return _displayRvParameters;
    }

    public void setDisplayRvParameters(boolean displayRvParameters) {
        this._displayRvParameters = displayRvParameters;
    }

    public String getSubject() {
        return _subject;
    }



    public Object clone() throws CloneNotSupportedException {
        return new RvParameters(_network, _service, _deamon, _displayRvParameters, _subject);
    }

    public RvParameters(String _network, String _service, String _deamon, boolean displayRvParameters, String _subject) {
        this._network = _network;
        this._service = _service;
        this._deamon = _deamon;
        this._displayRvParameters = displayRvParameters;
        this._subject = _subject;
        calcHashCode();
    }

    public void setSubject(String _subject) {
        this._subject = _subject;
    }

    /**
     * Returns an hascode using the Network, Service and Deamon values
     */
    public int hashCode() {
        return _hashCode;
    }


    /**
     * Write out RVParameters
     */
    private void writeObject(ObjectOutputStream s)

            throws IOException {
           // Call even if there is no default serializable fields.
        s.defaultWriteObject();

        s.writeUTF(_deamon);
        s.writeUTF(_network);
        s.writeUTF(_service);
        s.writeUTF(_subject);
        s.writeBoolean(_displayRvParameters);
    }


    /**
     * Read in the RvParameters
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
/*  Call even if there is no default serializable fields.
 *  Enables default serializable fields to be added in future versions
 *  and skipped by this version which has no default serializable fields.
 */
        s.defaultReadObject();

// restore
        _deamon = s.readUTF();
        _network = s.readUTF();
        _service = s.readUTF();
        _subject = s.readUTF();
        _displayRvParameters = s.readBoolean();
        calcHashCode();
    }

    public boolean equals(Object o) {

        boolean equals = false;

        if (o instanceof RvParameters) {
            if (this.hashCode() ==
                    ((RvParameters) o).hashCode()) {
                equals = true;
            }
        }

        return equals;
    }

}
