/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.model;

import java.io.Serializable;
import java.util.HashSet;
import de.uni_freiburg.informatik.ultimate.model.structure.IWalkable;

/**
 * This class is the general model container. It should preselect walkers and
 * perform a number of search operations on its model.
 * 
 * @author dietsch
 * @version 0.0.2
 */
public class ModelContainer implements Serializable {

	/**
	 * long serialVersionUID
	 */
	private static final long serialVersionUID = -1957760572620128974L;

	private IElement mGraphRoot;

	private GraphType mGraphType;

	private String mGraphName;

	protected ModelContainer(IElement rootNode, GraphType type, String name) {
		mGraphRoot = rootNode;
		mGraphType = type;
		mGraphName = name;
		init();
	}

	protected IElement getRoot() {
		return mGraphRoot;
	}

	protected String getName() {
		return mGraphName;
	}

	protected int getSize() {
		return -1;
	}

	public String toString() {
		return mGraphType.toString();
	}

	protected GraphType getType() {
		return mGraphType;
	}

	private void init() {
		mGraphType.setSize(countNodes(this.mGraphRoot));
	}

	protected void cleanup() {
	}

	private int countNodes(IElement root) {
		return 0;
		/*
		 * int acc=1; for(INode n : root.getOutgoing()){ acc=acc+countNodes(n);
		 * } return acc;
		 */
	}

	protected IElement findNode(String outerAnnotationKey,
			String innerAnnotationKey, Object innerAnnotationValue) {
		return findNode(outerAnnotationKey, innerAnnotationKey,
				innerAnnotationValue, mGraphRoot);
	}

	protected IPayload findNode(UltimateUID id) {
		return findNode(id.toString(), this.mGraphRoot);
	}

	protected IPayload findNode(String id) {
		return findNode(id, this.mGraphRoot);
	}

	protected static IPayload findNode(String id, IElement root) {
		return findNode(id, root, new HashSet<IElement>());
	}

	protected static IPayload findNode(String id, IElement currentRoot,
			HashSet<IElement> visited) {
		if (visited.contains(currentRoot))
			return null;
		visited.add(currentRoot);
		if (currentRoot.getPayload().getID().equals(id)) {
			return currentRoot.getPayload();
		} else {
			if (currentRoot instanceof IWalkable) {
				for (IWalkable n : ((IWalkable) currentRoot).getSuccessors()) {
					IPayload rtr_Value = findNode(id, n, visited);
					if (rtr_Value != null) {
						return rtr_Value;
					}
				}
			}
			return null;
		}
	}

	/**
	 * Finds Nodes based on their annotations. Expects every parameter to be not
	 * null! Simple recursive depth-first search.
	 * 
	 * @param outerAnnotationKey
	 * @param innerAnnotationKey
	 * @param innerAnnotationValue
	 * @param node
	 * @return Node with given annotation.
	 */
	protected static IElement findNode(String outerAnnotationKey,
			String innerAnnotationKey, Object innerAnnotationValue,
			IElement node) {
		// TODO implement search with support for null values (perhaps even
		// nodesets as return values
		if (node.getPayload().getAnnotations().get(outerAnnotationKey)
				.getAnnotationsAsMap().get(innerAnnotationKey)
				.equals(innerAnnotationValue)) {
			return node;
		} else {
			if (node instanceof IWalkable) {
				IElement returnNode = null;
				for (IElement i : ((IWalkable) node).getSuccessors()) {
					returnNode = findNode(outerAnnotationKey,
							innerAnnotationKey, innerAnnotationValue, i);
					if (returnNode != null) {
						return returnNode;
					}
				}
			}
		}
		return null;
	}
}
