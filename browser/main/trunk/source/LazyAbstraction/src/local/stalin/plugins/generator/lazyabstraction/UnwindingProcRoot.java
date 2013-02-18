/**
 * 
 */
package local.stalin.plugins.generator.lazyabstraction;

import java.util.ArrayList;
import java.util.HashMap;

import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
import local.stalin.logic.Atom;
import local.stalin.logic.Formula;
import local.stalin.logic.Theory;
import local.stalin.model.IAnnotations;
import local.stalin.model.Payload;

/**
 * @author alexander
 *
 */
public class UnwindingProcRoot extends UnwindingNode {

	private static final long serialVersionUID = 4212252432287810076L;
	
	private ArrayList<UnwindingNode> m_allNodesInPreorder;
//	private HashMap<CFGExplicitNode, ArrayList<UnwindingNode>> m_locationToPreorder;
	
	public UnwindingProcRoot() {
		super();
		m_allNodesInPreorder = new ArrayList<UnwindingNode>();
//		m_locationToPreorder = new HashMap<CFGExplicitNode, ArrayList<UnwindingNode>>();
		m_procRoot = this;
	}
	
	public UnwindingProcRoot(Theory theory, Formula formula) {
//		m_theory = theory;
//		m_formula = formula;
		// * taken from CFGExplicitNode:
		super(theory, formula);
		m_allNodesInPreorder = new ArrayList<UnwindingNode>();
		m_procRoot = this;
	}
	
	
	/*
	 * the procedure root keeps track of the preorder of the nodes - has to be updated when 
	 * a node is added to an unwinding 
	 */
	public void addToPreorder(UnwindingNode n) {
		m_allNodesInPreorder.add(n);
	}
	
//	/*
//	 * the procedure root keeps track of the preorder of the nodes - has to be updated when 
//	 * a node is added to an unwinding
//	 */
//	public void addToPreorder(UnwindingNode un, CFGExplicitNode cn) {
//		if(!m_locationToPreorder.containsKey(cn)) 			
//			m_locationToPreorder.put(cn, new ArrayList<UnwindingNode>());
//			
//		m_locationToPreorder.get(cn).add(un);
//	}

	/*
	 * get the List representing the Preordering of the Nodes in the Unwinding
	 */
	public ArrayList<UnwindingNode> getPreorder() {
		return m_allNodesInPreorder;
	}

//	/**
//	 * @return the m_locationToPreorder
//	 */
//	public HashMap<CFGExplicitNode, ArrayList<UnwindingNode>> getLocationToPreorder() {
//		return m_locationToPreorder;
//	}
}
