//:File:    InterningTrimmingNodeFactory.java
//:Created: Nov 24, 2005
//:Legal:   Copyright © 2005-@year@ Apache Software Foundation.
//:Legal:   Copyright © 2005-@year@ Ian Phillips.
//:License: Licensed under the Apache License, Version 2.0.
//:CVSID:   $Id$
package rvsn00p.util;

import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;
import nu.xom.Attribute.Type;

/**
 * A node factory that interns all structural strings and removes empty text.
 * <p>
 * A structural string is defined as any element or attribute name, as well as
 * any namespace URI or prefix.
 * <p>
 * This will generally have the effect of slowing down parsing but reducing the
 * space requirements and improving performance once parsed (because
 * <code>equals</code> calls will always match on the initial identity test).
 * 
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 * @since 1.3
 */
public final class InterningTrimmingNodeFactory extends NodeFactory {
    private final Nodes EMPTY = new Nodes();

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeAttribute(java.lang.String, java.lang.String, java.lang.String, nu.xom.Attribute.Type)
     */
    public Nodes makeAttribute(String name, String URI, String value, Type type) {
        return super.makeAttribute(name.intern(), URI.intern(), value, type);
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeDocType(java.lang.String, java.lang.String, java.lang.String)
     */
    public Nodes makeDocType(String rootElementName, String publicID, String systemID) {
        return super.makeDocType(rootElementName.intern(), publicID.intern(), systemID.intern());
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeProcessingInstruction(java.lang.String, java.lang.String)
     */
    public Nodes makeProcessingInstruction(String target, String data) {
        return super.makeProcessingInstruction(target.intern(), data);
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeRootElement(java.lang.String, java.lang.String)
     */
    public Element makeRootElement(String name, String namespace) {
        return super.makeRootElement(name.intern(), namespace.intern());
    }

    // We don't need text nodes at all.
    public Nodes makeText(String data) {
        data = data.trim();
        return data.length() > 0 ? super.makeText(data) : EMPTY; 
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#startMakingElement(java.lang.String, java.lang.String)
     */
    public Element startMakingElement(String name, String namespace) {
        return super.startMakingElement(name.intern(), namespace.intern());
    }
}