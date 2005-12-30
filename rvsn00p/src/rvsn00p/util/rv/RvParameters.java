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
 * A value object to hold the parameters used to connect to a Rendezvous daemon.
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
        final String[] params = string.split("\\|");
        if (params.length != 4)
            throw new IllegalArgumentException(string + " is not a valid configuration string.");
        final RvParameters p = new RvParameters(params[1], params[0], params[2]);
        final String[] subjects = params[3].split(",");
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
    private final Set subjects = new HashSet();

    public RvParameters(String service, String network, String daemon) {
        super();
        setNetwork(network);
        setService(service);
        setDaemon(daemon);
    }

    public void addSubject(String subject) {
        if (subject == null) return;
        subject = subject.trim();
        if (subject.length() > 0)
            subjects.add(subject);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RvParameters)) return false;
        final RvParameters that = (RvParameters) o;
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
        final StringBuffer buffer = new StringBuffer();
        for (final Iterator i = subjects.iterator(); i.hasNext();)
            buffer.append(i.next()).append(",");
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    public String[] getSubjects(){
         return (String[]) subjects.toArray(new String[subjects.size()]);
    }

    /**
     * Returns an hash code derived from the network, service and daemon attributes.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = new StringBuffer()
                .append(network).append(service).append(daemon)
                .hashCode();
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

    public void setSubjects(String[] subjects) {
        if (subjects == null) throw new NullPointerException();
        this.subjects.clear();
        if (subjects != null)
            for (int i = 0, imax = subjects.length; i < imax; ++i)
                addSubject(subjects[i]);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(daemon).append("|");
        buffer.append(service).append("|");
        buffer.append(network).append("|");
        for (final Iterator i = subjects.iterator(); i.hasNext();)
            buffer.append(i.next()).append(",");
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

}
