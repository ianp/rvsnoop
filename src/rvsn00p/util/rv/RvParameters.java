/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.
 */
package rvsn00p.util.rv;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

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
    protected Set _subjects;
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
        this._subjects = new HashSet();
        this._description = "";

        calcHashCode();
    }

    public RvParameters(String subject, String service, String network, boolean displayRvParameters, String deamon, String description) {
        this._subjects = new HashSet();
        this._service = service;
        this._network = network;
        this._displayRvParameters = displayRvParameters;
        this._deamon = deamon;
        this._description = description;

        calcHashCode();
    }

    public RvParameters(Set subjects, String service, String network, boolean displayRvParameters, String deamon, String description) {
        this._subjects = new HashSet();
        _subjects.addAll(subjects);


        this._service = service;
        this._network = network;
        this._displayRvParameters = displayRvParameters;
        this._deamon = deamon;
        this._description = description;

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
     * Does not use the subjects
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

        this._deamon = results[0];
        this._service = results[1];
        this._network = results[2];
        String subjects = results[3];

        StringTokenizer subjectTokenizer;
        subjectTokenizer = new StringTokenizer(subjects, ",", true);


        while (subjectTokenizer.hasMoreTokens()) {
            String sto = subjectTokenizer.nextToken();
            if (",".equals(sto)) {
                continue;
            }

            _subjects.add(sto);
        }


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
        if (_subjects != null) {
            boolean first = true;
            Iterator i = _subjects.iterator();
            while (i.hasNext()) {
                if (!first) {
                    sRetval += ",";
                }
                sRetval += (String) i.next();
                first = false;
            }
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
        String sRetVal = new String();

        boolean first = true;
        Iterator i = _subjects.iterator();
        while (i.hasNext()) {
            if (!first) {
                sRetVal += ",";
            }
            sRetVal += (String) i.next();
            first = false;
        }
        return sRetVal;
    }

    public Set getSubjects(){
         return _subjects;
    }

    public void setSubjects(Set subjects){

        _subjects.addAll(subjects);
    }

    /**
     *
     * @param subjects list of subjects separated with ","
     */
    public void setSubjects(String subjects){
        if (_subjects == null) {
            throw new IllegalArgumentException("Subjects may not be null");
        }


        StringTokenizer subjectTokenizer;
        subjectTokenizer = new StringTokenizer(subjects, ",", true);


        while (subjectTokenizer.hasMoreTokens()) {
            String sto = subjectTokenizer.nextToken();
            if (",".equals(sto)) {
                continue;
            }

            _subjects.add(sto);
        }


    }

    public Object clone() throws CloneNotSupportedException {
        return new RvParameters(_subjects, _service,_network,_displayRvParameters, _deamon,  _deamon);
    }


    public void addSubject(String _subject) {
        this._subjects.add(_subject);
        calcHashCode();
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
 /*   private void writeObject(ObjectOutputStream s)

            throws IOException {
        // Call even if there is no default serializable fields.
        s.defaultWriteObject();

        s.writeUTF(_deamon);
        s.writeUTF(_network);
        s.writeUTF(_service);
        s.writeUTF(_subjects);
        s.writeBoolean(_displayRvParameters);
    }
   */

    /**
     * Read in the RvParameters
     */
  /* private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
//  Call even if there is no default serializable fields.
//  Enables default serializable fields to be added in future versions
//   and skipped by this version which has no default serializable fields.

        s.defaultReadObject();


        _deamon = s.readUTF();
        _network = s.readUTF();
        _service = s.readUTF();
        _subject = s.readUTF();
        _displayRvParameters = s.readBoolean();
        calcHashCode();
    }
     */

    public boolean equals(Object o) {

        boolean equals = false;

        if (o instanceof RvParameters) {
            if (this.hashCode() == o.hashCode()) {
                equals = true;
            }
        }

        return equals;
    }

}
