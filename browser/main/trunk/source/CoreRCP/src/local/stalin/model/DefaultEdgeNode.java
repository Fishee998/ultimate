/*
 * Project:	CoreRCP
 * Package:	local.stalin.model
 * File:	DefaultEdgeNode.java created on Nov 26, 2009 by Björn Buchhold
 *
 */
package local.stalin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DefaultEdgeNode
 * 
 * @author Björn Buchhold
 * 
 */
public class DefaultEdgeNode extends AbstractEdgeNode {

	/**
	 * long serialVersionUID
	 */
	private static final long serialVersionUID = 6463748219614307755L;
	private List<IEdge> incEdges = new ArrayList<IEdge>();
	private List<IEdge> outEdges = new ArrayList<IEdge>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#addIncomingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean addIncomingEdge(IEdge element) {
		element.setTarget(this);
		return this.incEdges.add(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#addIncomingEdge(local.stalin.model.INode)
	 */
	@Override
	public boolean addIncomingEdge(INode src) {
		return this.incEdges.add(new Edge(src, this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#addOutgoingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean addOutgoingEdge(IEdge element) {
		element.setSource(this);
		return this.outEdges.add(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#addOutgoingEdge(local.stalin.model.INode)
	 */
	@Override
	public boolean addOutgoingEdge(INode target) {
		return this.outEdges.add(new Edge(this, target));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#getIncomingEdges()
	 */
	@Override
	public List<IEdge> getIncomingEdges() {
		return this.incEdges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#getOutgoingEdges()
	 */
	@Override
	public List<IEdge> getOutgoingEdges() {
		return this.outEdges;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#removeAllIncoming()
	 */
	@Override
	public void removeAllIncoming() {
		this.incEdges.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see local.stalin.model.INode#removeAllOutgoing()
	 */
	@Override
	public void removeAllOutgoing() {
		this.outEdges.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * local.stalin.model.INode#removeIncomingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean removeIncomingEdge(IEdge element) {
		return this.incEdges.remove(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * local.stalin.model.INode#removeOutgoingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean removeOutgoingEdge(IEdge element) {
		return this.outEdges.remove(element);
	}

}
