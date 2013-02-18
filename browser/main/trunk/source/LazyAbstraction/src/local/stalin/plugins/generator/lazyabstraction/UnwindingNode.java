/**
 * 
 */
package local.stalin.plugins.generator.lazyabstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import local.stalin.boogie.cfgreducer.Activator;
import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
import local.stalin.core.api.StalinServices;
import local.stalin.logic.Atom;
import local.stalin.logic.Formula;
import local.stalin.logic.Theory;
import local.stalin.model.AbstractEdgeNode;
import local.stalin.model.IAnnotations;
import local.stalin.model.IEdge;
import local.stalin.model.INode;
import local.stalin.model.IPayload;
import local.stalin.model.Payload;

/**
 * @author Alexander Nutz
 *
 */
public class UnwindingNode extends AbstractEdgeNode {

	protected static Logger s_logger = StalinServices.getInstance().getLogger(Activator.PLUGIN_ID);
	private boolean		m_isCovered = false;
	private UnwindingNode m_coveringNode;
	private ArrayList<UnwindingNode> m_coveredNodes = new ArrayList<UnwindingNode>();
	private boolean		m_isLeaf = true; 
	//TODO: wird nicht verwendet -> eventuelle verschönerung: verwenden und dafür UnwProcRoot-Klasse auflösen
	private boolean		m_isRoot = false; 
	protected UnwindingProcRoot m_procRoot;
	protected Theory 		m_theory;
	protected Formula 	m_formula;
	protected CFGExplicitNode m_cfgLocation; //der zugehörige Knoten im CFG - repräsentiert gleichzeitig die Location
	private List<IEdge> 				m_IncomingEdges		= new ArrayList<IEdge>();
	private List<IEdge> 				m_OutgoingEdges		= new ArrayList<IEdge>();
	private int m_indexInPreorder;
	
	/**
	 * 
	 */
	public UnwindingNode() {
		this.setPayload(new Payload());
		this.getPayload().getAnnotations().put("LA", new LAAnnotations(this));
	}
	
	/*
	 * called by toUnwindingNode: payload to be set later, inits with procRoot and cfgNode
	 */
	public UnwindingNode(UnwindingProcRoot procRoot, CFGExplicitNode cfgNode) {
//		this.setPayload(new Payload());
		procRoot.addToPreorder(this); //not deprecated because of "curly<"-ordering
//		procRoot.addToPreorder(this, cfgNode);
		m_indexInPreorder = procRoot.getPreorder().indexOf(this);
		m_procRoot = procRoot;
		m_cfgLocation = cfgNode;
		
//		this.getPayload().getAnnotations().put("LA", new LAAnnotations(this));
	}
	
	
	/**
	 * (almost obsolete..)
	 */
	public UnwindingNode(Theory theory, Formula formula) {
		this.setPayload(new Payload());
		m_theory = theory;
		HashMap<String, IAnnotations>	annotations = new HashMap<String, IAnnotations>();
		SMTNodeAnnotations				annotation	= new SMTNodeAnnotations();
		annotations.put("SMT", annotation);
		formula = (formula != null) ? formula : Atom.TRUE;
		annotation.setAssertion(formula);
		annotation.setTheory(theory);
		this.getPayload().setAnnotations(annotations);
		this.getPayload().getAnnotations().put("LA", new LAAnnotations(this));
	}

	
	/* (non-Javadoc)
	 * @see local.stalin.model.INode#addIncomingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean addIncomingEdge(IEdge element) {
		return m_IncomingEdges.add(element);
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#addIncomingEdge(local.stalin.model.INode)
	 */
	@Override
	public boolean addIncomingEdge(INode src) {
		s_logger.error("addIncomingEdge(INode) has been called");
		return false;
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#addOutgoingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean addOutgoingEdge(IEdge element) {
		return m_OutgoingEdges.add(element);
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#addOutgoingEdge(local.stalin.model.INode)
	 */
	@Override
	public boolean addOutgoingEdge(INode target) {
		s_logger.error("addIncomingEdge(INode) has been called");
		return false;
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#getIncomingEdges()
	 */
	@Override
	public List<IEdge> getIncomingEdges() {
		return m_IncomingEdges;
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#getOutgoingEdges()
	 */
	@Override
	public List<IEdge> getOutgoingEdges() {
		return m_OutgoingEdges;
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#removeAllIncoming()
	 */
	@Override
	public void removeAllIncoming() {
		m_IncomingEdges.clear();
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#removeAllOutgoing()
	 */
	@Override
	public void removeAllOutgoing() {
		m_OutgoingEdges.clear();
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#removeIncomingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean removeIncomingEdge(IEdge element) {
		return m_IncomingEdges.remove(element);
	}

	/* (non-Javadoc)
	 * @see local.stalin.model.INode#removeOutgoingEdge(local.stalin.model.IEdge)
	 */
	@Override
	public boolean removeOutgoingEdge(IEdge element) {
		return m_OutgoingEdges.remove(element);
	}
	
	public void setAsRoot() {
		m_isRoot = true;
	}

	public boolean isRoot() {
		return m_isRoot;
	}
	public String toString(){
		return getPayload().getName();
	}

	public void setCovered(boolean m_isCovered) {
		this.m_isCovered = m_isCovered;
	}

	public boolean isCovered() {
		return m_isCovered;
	}

	public void set_coveringNode(UnwindingNode m_coveringNode) {
		this.m_coveringNode = m_coveringNode;
	}

	public UnwindingNode get_coveringNode() {
		return m_coveringNode;
	}

	public ArrayList<UnwindingNode> get_coveredNodes() {
		return m_coveredNodes;
	}

	public void set_isLeaf(boolean m_isLeaf) {
		this.m_isLeaf = m_isLeaf;
	}

	public boolean isLeaf() {
		return m_isLeaf;
	}
	
	public SMTNodeAnnotations getSMTAnnotations(){
		return (SMTNodeAnnotations)this.getPayload().getAnnotations().get("SMT");
	}

	public int getIndexInPreorder() {
		return m_indexInPreorder;
	}
}
