package com.shadowolf.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that creates a dependency tree for some type E, where E is some class that depends on 
 * <i>n</i> children and is required by exactly one <i>parent</i>.
 * 
 * @param <E> The type of the Object that fits in the dependency tree. 
 */
public class DependencyTree<E> {
	private E val;
    private DependencyTree<E> parent;
    private List<DependencyTree<E>> children = new ArrayList<>();

    /**
     * Creates a new depedency tree by creating a root node.
     * @param e The root node of the dependency tree, the root object.
     */
    public DependencyTree(E e) {
        this(e, null);
    }

    /**
     * Create a new depedency tree by creating a subtree and attaching it as a child node to parent.
     * @param e the object used as a root node in the created subtree.
     * @param parent the tree to attach the created subtree to (attached as a child).
     */
    public DependencyTree(E e, DependencyTree<E> parent) {
        this(e, parent, null);
    }

    /**
     * Creates a new depedency tree by creating a subtree with root node value <i>e, attached as a child
     * subtree to parent tree <i>parent</i> with children <i>children</i>.
     * <br/><br/>
     * Passing null to <i>parent</i> will create a new top-level tree.
     *  
     * @param e the object to use as a root node value in the created subtree.
     * @param parent the tree to attach the created subtree to (attached as a child).
     * @param children some collection of objects to be added as children to the created subtree.
     */
    public DependencyTree(E e, DependencyTree<E> parent, Iterable<E> children) {
        this.val = e;
        this.parent = parent;

        if(children != null) {
            for(E el : children) {
                this.addChild(el);
            }
        }
    }

    /**
     * Checks whether <i>ancestor</i> exists anywhere in the this tree's ancestors.  
     * <br/><br/>
     * Remember that a tree may have one parent that depends on the tree.  This method
     * iterates upwards from parent to parent until the root node is reached (at which 
     * point, it will return false) or until it detects <i>itself</i> as one of its 
     * parents.  In that case, it will throw a {@link CircularDependencyException}.
     *   
     * @param ancestor 
     * @return whether <i>ancestor</i> was found as an ancestor of the callee
     * @throws CircularDependencyException if the callee detects itself as a dependency of itself, a circular dependency has been detected. 
     */
    public boolean hasAncestor(E e) throws CircularDependencyException {
        DependencyTree<E> parent = this;

        while(parent.hasParent()) {
            parent = parent.getParent();

            if(parent.getVal().equals(this.getVal())) {
            	throw new CircularDependencyException(parent.getVal().getClass() + " detected as dependency of itself.");
            }
            
            if(parent.getVal().equals(e)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether this tree has a parent tree. 
     * @return whether the tree has a parent or not.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns this tree's parent.
     * @return this tree's parent, or null if root tree.
     */
    public DependencyTree<E> getParent() {
        return parent;
    }

    /**
     * Get the value represented by this tree node.
     * @return the value
     */
    public E getVal() {
        return val;
    }

    /**
     * Remove e as a dependency of this tree.
     * @param e the child to remove
     */
    public void removeChild(E e) {
        children.remove(e);
    }

    /**
     * Add e as a dependency of this tree.
     * 
     * @param e the child to add
     */
    public void addChild(E e) {
        children.add(new DependencyTree<E>(e, this));
    }


    /**
     * Adds e as a depedencny of this tree
     */
    public void addChild(DependencyTree<E> e) {
    	children.add(e);
    }
	/**
     * Add kids as children to this tree.
     * @param kids the objects to add as dependencies.
     */
    public void addChildren(Iterable<E> kids) {
    	for(E e : kids) {
    		addChild(e);
    	}
    }
    
    /**
     * Return all the children of this tree: all of the subtree's
     * depended on by this class.  Will return an empty list if there
     * are no dependencies.
     * @return a list of children; will be empty if there are none.
     */
    public List<DependencyTree<E>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns the children of the tree as array e. 
     * @see #getChildren()
     */
    public DependencyTree<E>[] getChildren(DependencyTree<E>[] e) {
        return children.toArray(e);
    }

    /**
     * Collects all values of all children of this tree (only direct children!) 
     * and returns them in array e.
     * @see #getChildValues()
     * @return an array of child values.
     */
    @SuppressWarnings("unchecked")
    public E[] getChildValues(E[] e) {
        if(e.length < children.size()) {
            e = (E[]) new Object[children.size()];
        }

        for(int i = 0; i < children.size(); i++) {
            e[i] = children.get(i).getVal();
        }

        return e;
    }

    /**
     * Gets the children <i>values</i> of this tree.  This returns the values themselves,
     * not their subtrees, and this only returns the direct dependencies of this tree, not
     * any recursive dependencies (just children, not grandchildren).  
     * @return a List of child dependencies.
     */
    public List<E> getChildValues() {
        List<E> vals = new ArrayList<E>(children.size());
        for(DependencyTree<E> e : children) {
            vals.add(e.getVal());
        }

        return vals;
    }

    /**
     * Get the number of children in this tree.
     * @return number of tree children
     */
    public int numChildren() {
        return children.size();
    }

    /**
     * Determine whether tree has children or not.  A tree without any children is some
     * object that has no dependencies.
     * 
     * @return whether this tree has children.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }
}
