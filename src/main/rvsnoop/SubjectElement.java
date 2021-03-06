/*
 * Class:     SubjectElement
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2006-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package rvsnoop;

import com.google.common.base.Joiner;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * An element in a Rendezvous subject hierarchy.
 * <p>
 * Based on <a href="http://wiki.apache.org/logging-log4j/LogFactor5">Log Factor 5</a>.
 *
 * @author <a href="mailto:lundberg@home.se">Örjan Lundberg</a>
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class SubjectElement extends DefaultMutableTreeNode {

    private static final String[] ROOT_PATH = { "root" };

    private static final long serialVersionUID = -3670587626244234923L;

    private int hashCode;

    private boolean isErrorHere = false;

    private boolean isErrorUnder = false;

    private boolean isParentSet = false;

    private boolean isSelected = true;

    private int numRecordsHere;

    private int numRecordsUnder;

    private TreeNode[] path;

    private final String[] userObjectPath;

    public SubjectElement() {
        super(ROOT_PATH[0]);
        this.userObjectPath = ROOT_PATH;
    }

    /**
     * Create a new subject tree node by appending a new subject element to an
     * existing node. This should be used like so:
     * <code>parent.add(new SubjectElement(parent, "foo"));</code>
     *
     * @param parent Must be a <code>SubjectElement</code>.
     * @param element The subject element name.
     */
    public SubjectElement(DefaultMutableTreeNode parent, String element) {
        super(element);
        if (parent instanceof SubjectElement) {
            Object[] pp = parent.getUserObjectPath();
            userObjectPath = new String[pp.length + 1];
            // This is safe because the user objects for SubjectElements are always strings.
            System.arraycopy(pp, 0, userObjectPath, 0, pp.length);
            userObjectPath[pp.length] = element;
        } else {
            throw new IllegalArgumentException("Invalid parent node.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SubjectElement)) return false;
        final Object[] thisPath = getUserObjectPath();
        final Object[] thatPath = ((DefaultMutableTreeNode) obj).getUserObjectPath();
        if (thisPath.length != thatPath.length) return false;
        // OK, we need to test the path element by element then...
        for (int i = thisPath.length - 1; i >= 0; --i)
            if (!thisPath[i].equals(thatPath[i]))
                return false;
        return true;
    }

    public String getElementName() {
        return (String) getUserObject();
    }

    /**
     * The number of records directly at this element.
     *
     * @return The number of records.
     */
    public int getNumRecordsHere() {
        return numRecordsHere;
    }

    /**
     * The number of records at this element and all descendents.
     *
     * @return The number of records.
     */
    public int getNumRecordsUnder() {
        return numRecordsUnder;
    }

    /**
     * Since this class does not support re-parenting this can cache the result.
     *
     * @see DefaultMutableTreeNode#getPath()
     */
    @Override
    public TreeNode[] getPath() {
        if (path == null) path = super.getPath();
        // Don't expose the internal array!
        return path.clone();
    }

    /**
     * For a subject tree node the user object path is all of the subject name
     * elements.
     * <p>
     * Since there must be a root node however, the actual elements start at the
     * 1-index, the 0-th elements in the array is undefined. All other elements
     * may be cast to strings.
     *
     * @see DefaultMutableTreeNode#getUserObjectPath()
     */
    @Override
    public Object[] getUserObjectPath() {
        // Don't expose the internal array.
        return userObjectPath.clone();
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) hashCode = userObject.hashCode() * userObjectPath.length * 11;
        return hashCode;
    }

    public void incNumRecordsHere() {
        ++numRecordsHere;
        final TreeNode parent = getParent();
        if (parent instanceof SubjectElement)
            ((SubjectElement) parent).incNumRecordsUnder();
    }

    private void incNumRecordsUnder() {
        ++numRecordsUnder;
        final TreeNode parent = getParent();
        if (parent instanceof SubjectElement)
            ((SubjectElement) parent).incNumRecordsUnder();
    }

    public boolean isErrorHere() {
        return isErrorHere;
    }

    public boolean isErrorUnder() {
        return isErrorUnder;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void reset() {
        numRecordsHere = 0;
        numRecordsUnder = 0;
        isErrorHere = false;
        isErrorUnder = false;
    }

    public void setErrorHere() {
        isErrorHere = true;
        final TreeNode parent = getParent();
        if (parent instanceof SubjectElement)
            ((SubjectElement) parent).setErrorUnder();
    }

    private void setErrorUnder() {
        isErrorUnder = true;
        final TreeNode parent = getParent();
        if (parent instanceof SubjectElement)
            ((SubjectElement) parent).setErrorUnder();
    }

    /**
     * This has some additional checks to ensure that once a subject tree node
     * has been removed from the tree it is not reattched at a later time.
     *
     * @see DefaultMutableTreeNode#setParent(MutableTreeNode)
     */
    @Override
    public void setParent(MutableTreeNode newParent) {
        if (newParent == getParent()) return;
        // Use setting parent = this as a marker that we have been removed from a hierarchy.
        if (isParentSet && newParent != null)
            throw new UnsupportedOperationException("SubjectElement instances can not be re-used.");
        super.setParent(newParent);
        isParentSet = true;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    @Override
    public void setUserObject(Object userObject) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a string representation of this subject.
     *
     * @return The subject string.
     */
    public String toDisplayString() {
        return Joiner.on('.').join(userObjectPath);
    }

}
