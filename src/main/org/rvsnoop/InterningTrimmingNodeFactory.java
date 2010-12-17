/*
 * Class:     InterningTrimmingNodeFactory
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

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
    private static final Nodes EMPTY = new Nodes();

    private final boolean trim;

    /**
     * Create a new factory which trims text nodes.
     */
    public InterningTrimmingNodeFactory() {
        this(true);
    }

    /**
     * Create a new factory.
     *
     * @param trim Should text nodes be trimmed or not.
     */
    private InterningTrimmingNodeFactory(boolean trim) {
        super();
        this.trim = trim;
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeAttribute(java.lang.String, java.lang.String, java.lang.String, nu.xom.Attribute.Type)
     */
    @Override
    public Nodes makeAttribute(String name, String URI, String value, Type type) {
        return super.makeAttribute(name.intern(), URI.intern(), value, type);
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeDocType(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Nodes makeDocType(String rootElementName, String publicID, String systemID) {
        return super.makeDocType(rootElementName.intern(), publicID.intern(), systemID.intern());
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeProcessingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public Nodes makeProcessingInstruction(String target, String data) {
        return super.makeProcessingInstruction(target.intern(), data);
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#makeRootElement(java.lang.String, java.lang.String)
     */
    @Override
    public Element makeRootElement(String name, String namespace) {
        return super.makeRootElement(name.intern(), namespace.intern());
    }

    // We don't need text nodes at all.
    @Override
    public Nodes makeText(String data) {
        if (trim) data = data.trim();
        return data.length() > 0 ? super.makeText(data) : EMPTY;
    }

    /* (non-Javadoc)
     * @see nu.xom.NodeFactory#startMakingElement(java.lang.String, java.lang.String)
     */
    @Override
    public Element startMakingElement(String name, String namespace) {
        return super.startMakingElement(name.intern(), namespace.intern());
    }
}