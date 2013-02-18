package local.stalin.boogie.cfgreducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import local.stalin.access.IUnmanagedObserver;
import local.stalin.access.WalkerOptions;
import local.stalin.boogie.cfgbuilder.CFGNode;
import local.stalin.boogie.cfgbuilder.CFGRootAnnotations;

import local.stalin.logic.Atom;
import local.stalin.logic.Formula;
import local.stalin.logic.TermVariable;
import local.stalin.logic.Theory;

import local.stalin.model.IElement;
import local.stalin.model.INode;
import local.stalin.model.IEdge;
import local.stalin.boogie.cfgreducer.Activator;
import local.stalin.boogie.cfgreducer.preferences.PreferenceValues;
import local.stalin.core.api.StalinServices;

/**
 * This class reduces the CFG by merging nodes, which do form a loopless sequence in the original CFG.
 * It also shifts the assumptions to the succeeding edges of a node.
 * 
 */

public class CFGReducerObserver implements IUnmanagedObserver {
	
	private static final Boolean				s_debugMessages		= false;
	private static Logger						s_Logger			= StalinServices.getInstance().getLogger(Activator.PLUGIN_ID);
	private HashMap<CFGNode, CFGExplicitNode>	m_explicitNodeMap	= new HashMap<CFGNode, CFGExplicitNode>();
	private Theory 								m_theory;
	private HashSet<CFGExplicitNode>			m_visited			= new HashSet<CFGExplicitNode>();
	private static CFGExplicitNode				m_graphroot;

	/**
	 * 
	 * @return the root of the CFG.
	 */
	public IElement getRoot(){
		if(s_debugMessages) CFGReducerObserver.s_Logger.debug("Returning graphroot!!");
		return CFGReducerObserver.m_graphroot;
	}
	
	
	@Override
	public boolean process(IElement node) {
		if (!(node instanceof CFGNode)) {
			throw new UnsupportedOperationException("CFGReducer only works on CFGBuilder output!");
		}
		
		CFGNode root	= (CFGNode) node;
		CFGRootAnnotations rootAnnotations = (CFGRootAnnotations)root.getPayload().getAnnotations().get("CFGBuilder");
		m_theory 			= rootAnnotations.getTheory();
		m_graphroot		= new CFGExplicitNode(m_theory, Atom.TRUE);
		m_graphroot.resetCounter();
		
		m_graphroot.setPayload(PayloadModifier.copyPayload(root.getPayload()));
		s_Logger.info("BMdata:(CFGReducer) >(1)PreNode#: " + countNodes(root));
		
		for (INode n : root.getOutgoingNodes()) {
			CFGExplicitNode procNode = processProcedure((CFGNode) n);
			@SuppressWarnings("unused")
			CFGEdge edge = new CFGEdge(m_theory, Atom.TRUE, m_graphroot, procNode);
			
			ConfigurationScope scope = new ConfigurationScope();
			IEclipsePreferences prefs = scope.getNode(Activator.PLUGIN_ID);
			boolean  reduceGraph= !prefs.getBoolean(PreferenceValues.NAME_REDUCEGRAPH, PreferenceValues.VALUE_REDUCEGRAPH_DEFAULT);
			if (reduceGraph){
				do{
					m_visited.clear();
				}while(reduce_Node(procNode));
			}
		}
		s_Logger.info("BMdata:(CFGReducer) >(2)PostNode#: " + countNodes(m_graphroot));
		
		return false;
	}

	@SuppressWarnings("unchecked")
	public CFGExplicitNode processProcedure(CFGNode procNode) {
		CFGExplicitNode result = new CFGExplicitNode(m_theory, (Formula)procNode.getCFGAnnotations().getAnnotationsAsMap().get("Assertion"));
		result.getPayload().setLocation(procNode.getPayload().getLocation());
		result.getPayload().setName(procNode.getPayload().getName());
		
		HashMap<String, TermVariable> oldvars	= (HashMap<String, TermVariable>)procNode.getCFGAnnotations().getAnnotationsAsMap().get("oldvars");
		HashMap<String, TermVariable> invars	= (HashMap<String, TermVariable>)procNode.getCFGAnnotations().getAnnotationsAsMap().get("invars");
		HashMap<String, TermVariable> outvars	= (HashMap<String, TermVariable>)procNode.getCFGAnnotations().getAnnotationsAsMap().get("outvars");
		HashSet<TermVariable> vars				= (HashSet<TermVariable>)procNode.getCFGAnnotations().getAnnotationsAsMap().get("vars");
		
		if (invars == null){
			invars = new HashMap<String, TermVariable>();
		}
		if (outvars == null){
			outvars = new HashMap<String, TermVariable>();
		}
		if (vars == null){
			vars = new HashSet<TermVariable>();
		}
		
		for (INode node : procNode.getOutgoingNodes()) {
			CFGExplicitNode explNode = nodeToExplicitNode((CFGNode) node, m_theory);
			CFGEdge edge = new CFGEdge(m_theory, (Formula)procNode.getCFGAnnotations().getAnnotationsAsMap().get("assumption"), result, explNode);
			edge.getSMTAnnotations().m_InVars = invars;
			edge.getSMTAnnotations().m_OutVars = outvars;
			edge.getSMTAnnotations().m_Vars = vars;
		}
		SMTNodeAnnotations.s_OldVars = oldvars;
		result.getSMTAnnotations().m_Vars = vars;
		return result;
	}		

	@SuppressWarnings("unchecked")
	private CFGExplicitNode nodeToExplicitNode(CFGNode node, Theory theory){
		if (m_explicitNodeMap.containsKey(node)) {
			if(s_debugMessages) CFGReducerObserver.s_Logger.info("Already made explicit node for " + node.getPayload().getName());
			return m_explicitNodeMap.get(node);
		}
		
		if(s_debugMessages) CFGReducerObserver.s_Logger.info("Starting to make explicit node for " + node.getPayload().getName());
		CFGExplicitNode explicitNode			= new CFGExplicitNode(theory, (Formula)node.getCFGAnnotations().getAnnotationsAsMap().get("assertion"));
		if(s_debugMessages) CFGReducerObserver.s_Logger.info("Adding variable information from " + node.getPayload().getName());
		HashMap<String, TermVariable> inVars	=  (HashMap<String, TermVariable>)node.getCFGAnnotations().getAnnotationsAsMap().get("invars");
		HashMap<String, TermVariable> outVars	=  (HashMap<String, TermVariable>)node.getCFGAnnotations().getAnnotationsAsMap().get("outvars");
		HashSet<TermVariable> vars				= (HashSet<TermVariable>)node.getCFGAnnotations().getAnnotationsAsMap().get("vars");
		
		if (inVars == null){
			inVars = new HashMap<String, TermVariable>();
		}
		if (outVars == null){
			outVars = new HashMap<String, TermVariable>();
		}
		if (vars == null){
			vars = new HashSet<TermVariable>();
		}
		
		m_explicitNodeMap.put(node, explicitNode);
		explicitNode.getPayload().setLocation(node.getPayload().getLocation());
		explicitNode.getPayload().setName(node.getPayload().getName());
		explicitNode.getSMTAnnotations().m_InVars = (HashMap<String, TermVariable>)inVars.clone();
		explicitNode.getSMTAnnotations().m_Vars = (HashSet<TermVariable>)vars.clone();

		for (INode successor: node.getOutgoingNodes()){
			CFGExplicitNode explicitSuccessor =  nodeToExplicitNode((CFGNode) successor, theory);
			CFGEdge edge = new CFGEdge(theory, (Formula)node.getCFGAnnotations().getAnnotationsAsMap().get("assumption"), explicitNode, explicitSuccessor);
			edge.getSMTAnnotations().m_InVars = (HashMap<String, TermVariable>)inVars.clone();
			edge.getSMTAnnotations().m_OutVars = (HashMap<String, TermVariable>)outVars.clone();
			edge.getSMTAnnotations().m_Vars = (HashSet<TermVariable>)vars.clone();
		}
		if(s_debugMessages) CFGReducerObserver.s_Logger.info("Done making explicit node for " + node.getPayload().getName());
		return explicitNode;
	}
	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

	@Override
	public WalkerOptions getWalkerOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}
	
	//Returns true if a node has been reduced, false if there was nothing to be reduced in subgraph of source node
	private boolean reduce_Node(CFGExplicitNode source){
		boolean do_merge = false;
		CFGEdge edge = null;
		CFGExplicitNode target = null;

		//Check if source has already been visited and therefore already been reduced ...
		if (m_visited.contains(source)){
			//if source is already been reduced then return without taking any actions
			return false;
		}
		m_visited.add(source);
		//Iterate through all edges of source node in order to start reducing
		for (IEdge iedge: source.getOutgoingEdges()){
			edge = (CFGEdge)iedge;
			target = (CFGExplicitNode)edge.getTarget();
			if (target == source){
				//source has a loop then ignore it
				continue;
			} else if (target.getIncomingEdges().size() > 1){
				/*target has more than one incoming edge; ignore this edge of source node
				 * also handles self-looping targets*/
				continue;
			} else {
				do_merge = true;
				break;
			}
		}
		if(do_merge){
			//target will be reduced into source
			//merge nodes
			merge_Nodes(edge);
			//Iterate through all edges of the target and concatenate them with edge coming from source
			for (IEdge isucc_edge: target.getOutgoingEdges()){
				CFGEdge succ_edge = (CFGEdge)isucc_edge;
				//concatenates the edges
				CFGEdge new_edge = concat_Edges(edge, succ_edge);
				//merges edges that have the same source and target as new edge
				ConfigurationScope scope = new ConfigurationScope();
				IEclipsePreferences prefs = scope.getNode(Activator.PLUGIN_ID);
				boolean  mergeParallelEdges= prefs.getBoolean(PreferenceValues.NAME_MERGEPARALLELEDGES, PreferenceValues.VALUE_MERGEPARALLELEDGES_DEFAULT);
				if (mergeParallelEdges){
					merge_DoubleEdges(new_edge);
				}
			}
			//delete target and all edges that lead to and from it
			delete_Node(target);
			//returns true because reduction actions have taken place
			return true;
		}
		
		/*iterates through all children of source if source has not done any reduction in order
		 * to reduce them*/
		for (IEdge iedge: source.getOutgoingEdges()){
			edge = (CFGEdge)iedge;
			//if a child of source has been reduced then break up and tell caller so
			if (reduce_Node((CFGExplicitNode)edge.getTarget())){
				return true;
			}
		}
		//if no actions have taken place return false
		return false;
	}
		
		
	private void merge_Nodes(CFGEdge edge) {
		//get all formulas and variables of the edge and its source and target
		CFGExplicitNode node1 = (CFGExplicitNode) edge.getSource();
		CFGExplicitNode node2 = (CFGExplicitNode) edge.getTarget();
		HashMap<String, TermVariable> e_invars	= edge.getSMTAnnotations().m_InVars;
		HashMap<String, TermVariable> e_outvars = edge.getSMTAnnotations().m_OutVars;
		
		HashMap<String, TermVariable> n1_invars = node1.getSMTAnnotations().m_InVars;
		HashMap<String, TermVariable> n2_invars = node2.getSMTAnnotations().m_InVars;
		
		HashSet<TermVariable> e_vars	= edge.getSMTAnnotations().m_Vars;
		HashSet<TermVariable> n1_vars	= node1.getSMTAnnotations().m_Vars;
		HashSet<TermVariable> n2_vars	= node2.getSMTAnnotations().m_Vars;
			
		HashMap<String, TermVariable>	n_invars	= new HashMap<String, TermVariable>();
		HashSet<TermVariable> 			n_vars		= new HashSet<TermVariable>();
		
		Formula n1_assertion	= node1.getAssertion();
		Formula e_assumption	= edge.getAssumption();
		
		Formula n_assertion		= node2.getAssertion();
		
		n_vars.addAll(n1_vars);
		n_vars.addAll(n2_vars);
		n_vars.addAll(e_vars);
		
		//all in coming variables of the source node will also be in coming variables of the merged result node
		n_invars.putAll(n1_invars);
		
		//iterate through all in coming variables of the target node and map them if possible
		for (String vname: n2_invars.keySet()){
			TermVariable n2_invar	= n2_invars.get(vname);
			TermVariable e_outvar	= e_outvars.get(vname);
			TermVariable n1_invar	= n1_invars.get(vname);
			if (e_outvar != null){
				n_assertion = m_theory.let(n2_invar, m_theory.term(e_outvar), n_assertion);
			} else if (n1_invar != null){
					//node1 can have an in coming variable vname since it can obtain it from a previous merge with another node
					n_assertion = m_theory.let(n2_invar, m_theory.term(n1_invar), n_assertion);
					//n_vars.remove(n2_invar);
			} else {
				n_invars.put(vname, n2_invar);
			}
		}
		
		//append the formulas of the edge and the target node
		n_assertion = m_theory.implies(e_assumption, n_assertion);
		
		//iterate through all in coming variables of the edge and map them to in coming variables of the source node
		for (String vname: e_invars.keySet()){
			TermVariable e_invar	= e_invars.get(vname);
			TermVariable n1_invar	= n1_invars.get(vname);
			if (n1_invar != null){
				if(n1_invar != e_invar){
					n_assertion = m_theory.let(e_invar, m_theory.term(n1_invar), n_assertion);
					//n_vars.remove(e_invar);
				}
			} else {
				n_invars.put(vname, e_invar);
			}
		}
		
		//append the formulas of the source and current assertion
		n_assertion = m_theory.and(n1_assertion, n_assertion);
		
		node1.setAssertion(n_assertion);
		node1.getSMTAnnotations().m_InVars	= n_invars;
		node1.getSMTAnnotations().m_Vars	= n_vars;
		return;
	}

	//deletes node and all its edges
	private void delete_Node(CFGExplicitNode node) {
		//go to all predecessor and remove pointer to incoming edges of node
		for (IEdge iedge: node.getIncomingEdges()){
			CFGEdge edge = (CFGEdge)iedge;
			edge.getSource().getOutgoingEdges().remove(edge);
		}
		//go to all successors and remove pointer to outgoing edges of node
		for (IEdge iedge: node.getOutgoingEdges()){
			CFGEdge edge = (CFGEdge)iedge;
			edge.getTarget().getIncomingEdges().remove(edge);
		}
		//clear the list of in and out going edges of this node
		node.removeAllIncoming();
		node.removeAllOutgoing();
	}


	private void merge_DoubleEdges(CFGEdge edge) {
		edge.removeMultipleEdge();
	}

	private CFGEdge concat_Edges(CFGEdge edge1, CFGEdge edge2) {
		
		SMTEdgeAnnotations edge1Annotation = edge1.getSMTAnnotations();
		SMTEdgeAnnotations edge2Annotation = edge2.getSMTAnnotations();
		
		HashMap<String, TermVariable> edge1InVars		= edge1Annotation.m_InVars;
		HashMap<String, TermVariable> edge2InVars		= edge2Annotation.m_InVars;
		
			
		HashMap<String, TermVariable> newInVars			= new HashMap<String, TermVariable>();
		
		HashMap<String, TermVariable> edge1OutVars	= edge1Annotation.m_OutVars;
		HashMap<String, TermVariable> edge2OutVars	= edge2Annotation.m_OutVars;
		HashMap<String, TermVariable> newOutVars	= new HashMap<String, TermVariable>();
		
		HashSet<TermVariable> edge1Vars				= edge1Annotation.m_Vars;
		HashSet<TermVariable> edge2Vars				= edge2Annotation.m_Vars;
		HashSet<TermVariable> newVars				= new HashSet<TermVariable>();
		
		
		Formula edge1Assumption	= edge1.getAssumption();
		Formula edge2Assumption	= edge2.getAssumption();
		Formula newAssumption	= edge2Assumption;
		
		//first fill all variables in n_vars, later delete each variable that is mapped by a let-statement and therefore not visible anymore
		newVars.addAll(edge1Vars);
		newVars.addAll(edge2Vars);
		
		newInVars.putAll(edge1InVars);
		newOutVars.putAll(edge2OutVars);
		
		//this loop handles all out variables of e1
		for (String vname: edge1OutVars.keySet()){
			TermVariable e1_outvar	= edge1OutVars.get(vname);
			TermVariable e2_invar	= edge2InVars.get(vname);
			
			if (e2_invar != null){
				newAssumption = m_theory.and(m_theory.equals(m_theory.term(e2_invar), m_theory.term(e1_outvar)), newAssumption);
				//newVars.remove(e2_invar);
//				if (edge2OutVars.get(vname) == e2_invar){
//					newOutVars.put(vname, e1_outvar);
//				}
			} else {
				if (!edge2OutVars.containsKey(vname)) {
					newOutVars.put(vname, e1_outvar);
				}
			}
		}
		
		//this loop handles all in variables of e2 that could not be mapped to any out variable of e1
		for (String vname: edge2InVars.keySet()){
			TermVariable edge1Outvar	= edge1OutVars.get(vname);
			TermVariable edge2Invar	= edge2InVars.get(vname);
			if (edge1Outvar == null){
				newInVars.put(vname, edge2Invar);
			}
		}
		
		//append formulas
		newAssumption = m_theory.and(edge1Assumption, newAssumption);
		
		//put everything in a new CFG edge and return it
		CFGEdge newEdge = new CFGEdge(m_theory, newAssumption, edge1.getSource(), edge2.getTarget());

		SMTEdgeAnnotations newAnnotation = newEdge.getSMTAnnotations();
		
		//fill new edge with new annotations
		newAnnotation.m_InVars	= newInVars;
		newAnnotation.m_OutVars	= newOutVars;
		newAnnotation.m_Vars	= newVars;
		
		return newEdge;	
	}


	@Override
	public boolean performedChanges() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private int countNodes(INode root){
			ArrayList<INode> searchStack = new ArrayList<INode>();
			for (INode e: root.getOutgoingNodes()) {
				searchStack.add(e);
			}
			
			int i = 0;
			// if the search stack still holds edges that might lead to an error continue...
			while(i < searchStack.size()) {
				// get the current node which will be expanded
				INode node = searchStack.get(i);
				// run through all descendants... 
				List<INode> children = node.getOutgoingNodes();
				if (children != null){
					for(INode n: node.getOutgoingNodes()) {
						// check if descendant has already been visited by another path(shorter path by construction of BFS)
						if (!searchStack.contains(n)) {
							// append new edge to search stack since descendant has not been visited yet
							searchStack.add(n);
						}
					}
				}
				i++;
			}
			return searchStack.size();
		}
}
