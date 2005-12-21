//:File:    RvParameters.java
//:Legal:   Copyright © 2002-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util.rv;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A value object to hold the parameters used to connect to a Rendezvous Daemon.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RvParameters {
    
    /**
     * Create a new parameter object from a configuration string.
     * 
     * @param string The configuration string to parse.
     * @return The configured <code>RvParameters</code> instance.
     */
    public static RvParameters parseConfigurationString(String string) {
        String[] params = string.split("\\|");
        if (params.length != 4)
            throw new IllegalArgumentException(string + " is not a valid configuration string.");
        RvParameters p = new RvParameters(params[1], params[0], params[2]);
        String[] subjects = params[3].split(",");
        for (int i = 0, imax = subjects.length; i < imax; ++i)
            p.addSubject(subjects[i]);
        return p;
    }

    /**
     * The Rendezvous daemon parameter.
     */
    private String daemon;

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
    private String network;
    
    /**
     * The Rendezvous service parameter.
     */
    private String service;
    
    /**
     * The set of subjects to subscribe to.
     */
    private Set subjects = new HashSet();

    public RvParameters() {
        this("7500", "", "tcp:7500");
    }
    
    public RvParameters(String service, String network, String daemon) {
        setNetwork(network);
        setService(service);
        setDaemon(daemon);
    }

    public void addSubject(String _subject) {
        this.subjects.add(_subject);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RvParameters)) return false;
        return hashCode() == o.hashCode();
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

    /**
     * Get the Rendezvous service parameter.
     */
    public String getService() {
        return service;
    }

    /**
     * Get's a comma-seperated list of all of the subjects which are subscribed
     * to.
     * <p>
     * This method does not handle escaping so it will return inconsistent
     * results if any of the subject names include commas themselves.
     * 
     * @return The concatenated subject list.
     */
    public String getSubjectsAsString() {
        if (subjects.size() == 0) return "";
        StringBuffer buffer = new StringBuffer();
        Iterator i = subjects.iterator();
        while (i.hasNext())
            buffer.append(i.next()).append(",");
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    public Set getSubjects(){
         return subjects;
    }

    /**
     * Returns an hash code derived from the network, service and daemon attributes.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (hashCode == 0) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(network).append(service).append(daemon);
            hashCode = buffer.hashCode();
        }
        return hashCode;
    }

    /**
     * Set the Rendezvous daemon parameter.
     */
    public void setDaemon(String daemon) {
        this.daemon = daemon != null ? daemon : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    /**
     * Set the Rendezvous network parameter.
     */
    public void setNetwork(String network) {
        this.network = network != null ? network : "";
    }

    /**
     * Set the Rendezvous service parameter.
     */
    public void setService(String service) {
        this.service = service != null ? service : "";
    }

    public void setSubjects(Set subjects) {
        if (subjects == null) throw new NullPointerException();
        this.subjects.clear();
        this.subjects.addAll(subjects);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(daemon).append("|");
        buffer.append(service).append("|");
        buffer.append(network).append("|");
        Iterator i = subjects.iterator();
        while (i.hasNext())
            buffer.append(i.next()).append(",");
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

}
