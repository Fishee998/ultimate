package local.stalin.plugins.generator.lazyabstraction;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Map.Entry;

import local.stalin.access.IUnmanagedObserver;
import local.stalin.access.WalkerOptions;
import local.stalin.core.api.StalinServices;
import local.stalin.model.IElement;
import local.stalin.model.INode;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import local.stalin.SMTInterface.SMTInterface;
import local.stalin.access.IUnmanagedObserver;
import local.stalin.access.WalkerOptions;
import local.stalin.boogie.cfgbuilder.CFGNode;
import local.stalin.boogie.cfgbuilder.CFGRootAnnotations;
import local.stalin.boogie.cfgreducer.Activator;
import local.stalin.boogie.cfgreducer.CFGEdge;
import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.PayloadModifier;
import local.stalin.boogie.cfgreducer.SMTEdgeAnnotations;
import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
//import local.stalin.boogie.cfgreducer.preferences.PreferenceValues;
import local.stalin.core.api.StalinServices;
import local.stalin.logic.ApplicationTerm;
import local.stalin.logic.Atom;
import local.stalin.logic.ConnectedFormula;
import local.stalin.logic.DistinctAtom;
import local.stalin.logic.EqualsAtom;
import local.stalin.logic.FletFormula;
import local.stalin.logic.Formula;
import local.stalin.logic.FormulaUnFleterer;
import local.stalin.logic.FormulaWalker;
import local.stalin.logic.FunctionSymbol;
import local.stalin.logic.ITEFormula;
import local.stalin.logic.ITETerm;
import local.stalin.logic.LetFormula;
import local.stalin.logic.NegatedFormula;
import local.stalin.logic.NumeralTerm;
import local.stalin.logic.PredicateAtom;
import local.stalin.logic.ProgramVariableTerm;
import local.stalin.logic.QuantifiedFormula;
import local.stalin.logic.RationalTerm;
import local.stalin.logic.Sort;
import local.stalin.logic.Term;
import local.stalin.logic.TermVariable;
import local.stalin.logic.Theory;
import local.stalin.logic.VariableAtom;
import local.stalin.logic.VariableTerm;
import local.stalin.model.IAnnotations;
import local.stalin.model.IEdge;
import local.stalin.model.IElement;
import local.stalin.model.INode;
import local.stalin.model.Payload;
import local.stalin.model.boogie.ast.Attribute;
import local.stalin.model.boogie.ast.Declaration;
import local.stalin.model.boogie.ast.EnsuresSpecification;
import local.stalin.model.boogie.ast.Expression;
import local.stalin.model.boogie.ast.Procedure;
import local.stalin.model.boogie.ast.RequiresSpecification;
import local.stalin.model.boogie.ast.Specification;
import local.stalin.plugins.ResultNotifier;


/**
 * Implementation of the algorithm from Ken McMillan's paper "Lazy Abstraction with Interpolants".
 * The core methods are named according to the methods of the paper's pseudo code.
 * A control flow graph from CFGReducer is taken as input and a safety check of the program's assertions
 *  is executed.
 * The output can be displayed with JungVisualization.
 */
public class LazyAbstractionObserver implements IUnmanagedObserver {

	private static Logger s_logger = StalinServices.getInstance().getLogger(Activator.PLUGIN_ID);
	private UnwindingNode m_graphroot;
	private HashMap<Term, String>		m_ConstantsToVariableName	= new HashMap<Term, String>();
	//introducing a Term for a non-SSA version of each program variable
	private HashMap<String,Term> m_VariableNameToTerm = new HashMap<String,Term>();
	private Theory m_theory;
	private TreeSet<UnwindingNode> m_openNodes;
	TreeSet<UnwindingNode> coveredToRemove;
	CFGRootAnnotations m_crAnnotations;
	HashMap<String, Procedure> m_procDeclarationsByName;
	
	boolean outputDebugInfo = false;
	
	private SMTInterface m_SMTInterface;
	private SMTInterface m_SMTInterpol;
	
	private HashMap<Formula, Integer> m_implicationDict = new HashMap<Formula, Integer>();
	
	private boolean m_programIncorrect = false;
	
	private boolean m_returnToUnwind;

	HashMap<String,ArrayDeque<TermVariable>> m_availableTermVariables 
		= new HashMap<String,ArrayDeque<TermVariable>>();
	HashMap<String,ArrayDeque<TermVariable>> m_usedTermVariables 
		= new HashMap<String,ArrayDeque<TermVariable>>();
	
//	boolean returnImmediately = false;
	int noUnwind = 0;
	int noCover = 0;
	int noExpand = 0;
	int noClose = 0;
	int noDfs = 0;
	int noRefine = 0;
	int noNodes = 0;
	int noImplicationChecks = 0;
	int noErrorPathChecks = 0;
	long timeImplicationChecks = 0;
	long timeErrorPathChecks = 0;
	
	@Override
	/*
	 * main entry point
	 */
	public boolean process(IElement node) {
		s_logger.debug("entered process()");
		CFGExplicitNode root	= (CFGExplicitNode) node;
		m_theory 			= 
			((CFGRootAnnotations)root.getPayload().getAnnotations().get("CFGBuilder")).getTheory();
		m_graphroot		= new UnwindingNode(m_theory, Atom.TRUE);
		m_graphroot.setPayload(PayloadModifier.copyPayload(root.getPayload()));
		m_graphroot.setAsRoot();
	
		m_crAnnotations = 
			((CFGRootAnnotations) m_graphroot.getPayload().getAnnotations().get("CFGBuilder")); 
		m_procDeclarationsByName = new HashMap<String, Procedure>();
		
		//put the Declarations into a dictionary such that the can be accessed by procedure name
		s_logger.debug(" ------  the declarations in the CFGBuilder Annotation:");
		m_crAnnotations.getAnnotationsAsMap();
		
		for(Declaration d : m_crAnnotations.getDeclarations()) {
			s_logger.debug(d);
			if(d instanceof Procedure) {
				Procedure p = (Procedure) d;
				m_procDeclarationsByName.put(p.getIdentifier(), p);
			}
			
		}
		
		m_SMTInterface = new SMTInterface(m_theory, SMTInterface.SOLVER_GROUNDIFY);
		m_SMTInterpol = new SMTInterface(m_theory, SMTInterface.SOLVER_SMTLIB);
		
		StalinServices.getInstance().putInToolchainStore("solver", m_SMTInterface);
		
		for (INode n : root.getOutgoingNodes()) {

			UnwindingProcRoot procRoot = processProcedure((CFGExplicitNode) n);
			
			UnwindingEdge edge = new UnwindingEdge(m_theory, m_graphroot, procRoot);
			procRoot.addIncomingEdge(edge);
			m_graphroot.addOutgoingEdge(edge);
	
		}
		
		if(m_programIncorrect) {
			s_logger.info(" ------------- P R O G R A M   U N S A F E ------------------- ");
			ResultNotifier.programIncorrect();
		}
		else {
			s_logger.info(" ------------- P R O G R A M   S A F E ------------------- ");
			ResultNotifier.programCorrect();	
		}
		logStats();
		
		return false;
	}

	/*
	 * performs a safety check for each procedure
	 */
	public UnwindingProcRoot processProcedure(CFGExplicitNode cfgProcNode) {

		UNWComparator comp = new UNWComparator();
//		coveredToRemove	= new TreeSet<UnwindingNode>(comp);//TODO - hack in unwind
		m_openNodes = new TreeSet<UnwindingNode>(comp);
		
		UnwindingProcRoot procRoot = toUnwindingProcRoot(cfgProcNode);

		//expand 1 - convention: a procedure root node has exactly one child
		//and there's no assumption in the edge.. -> which is wrong if the reducer reduced the graph
		//also then there is no outgoing edge for programs without loops
		if(cfgProcNode.getOutgoingEdges().isEmpty()) {
			m_openNodes.add(procRoot); //there seems to be a problem at this point with invars being null..
		}
		else {
			for(IEdge iEdge : cfgProcNode.getOutgoingEdges()) {
				CFGEdge cEdge = (CFGEdge) iEdge;
				CFGExplicitNode cfgChild = (CFGExplicitNode) cEdge.getTarget();
				UnwindingNode unwChild = toUnwindingNode(cfgChild, procRoot);
				UnwindingEdge newEdge = new UnwindingEdge(
						cfgProcNode.getTheory(), cEdge.getSMTAnnotations(), procRoot, unwChild);

				//		UnwindingEdge newEdge = //-> wrong if there is an assumption in the edge 
				//			new UnwindingEdge(cfgProcNode.getTheory(),
				//					 procRoot, unwChild);
				unwChild.addIncomingEdge(newEdge);
				procRoot.addOutgoingEdge(newEdge);

				updateVarNamesToTermsMapping(newEdge);

				//execute the Lazy Abstraction Algorithm
				m_openNodes.add(unwChild);
			}
		}
		
		paper_UNWIND();
		
		return procRoot;
	}
	

	@SuppressWarnings("unchecked")
	private void paper_UNWIND() {
		noUnwind++;
		if(outputDebugInfo)
			s_logger.debug("UNWIND has been called");
		
		if(outputDebugInfo)
			logStats();

		while(!m_openNodes.isEmpty()) {
			
			if(outputDebugInfo)
				s_logger.debug("UNWIND: loop started again with " + m_openNodes);

//			if(abortCondition()) {
//				ResultNotifier.programUnknown("abortCondition() became true");
//				return;
//			}//TODO: zeitschranke richtig ein bauen
//			if(counter < 0) return;
			if(m_programIncorrect) return;
			m_returnToUnwind = false;
			
			TreeSet<UnwindingNode> openNodes_0 = 
				(TreeSet<UnwindingNode>) m_openNodes.clone();
			
//			s_Logger.debug("UNWIND "/* + counter */+ ": open nodes " + openNodes_0);
			for(UnwindingNode openNode : openNodes_0) {
				//CLOSE() auf alle Vorgänger - wenn es welche gibt..
				if(openNode.getIncomingEdges().size() > 0)
					closeRec((UnwindingNode)(openNode.getIncomingEdges().get(0).getSource()));
				//DFS() auf openNode
				paper_DFS(openNode);
			}
			
//			//TODO: HACK
//			for(UnwindingNode n : m_openNodes) {
//				if(n.isCovered())
//					n.setCovered(false);
////					coveredToRemove.add(n);
//			}
////			m_openNodes.removeAll(coveredToRemove);
			
		}
	}

	private void logStats() {
		String s = "stats: \n" 
			//+ "unwind:" + noUnwind + "\n"
			+ "dfs: " + noDfs + "\n"
			+ "close: " + noClose + "\n"
			+ "expand: " + noExpand + "\n"
			+ "refine: " + noRefine + "\n"
			+ "cover: " + noCover + "\n"
			+ "BMdata:(Lazy Abstraction)PC: error path checks: " + noErrorPathChecks + "\n"
			+ "BMdata:(Lazy Abstraction)TIME: error path checks: " + timeErrorPathChecks + "\n"
			+ "BMdata:(Edge Checks)EC: implication checks: " + noImplicationChecks + "\n"
			+ "BMdata:(Edge Checks)TIME: implication checks: " + timeImplicationChecks + "\n"
			+ "nodes: " + noNodes;
		s_logger.info(s);
	}
	
	/*
	 * calls CLOSE recursively on all nodes going upwards to the procroot
	 */
	private void closeRec(UnwindingNode unwNode) {
		if(!(unwNode instanceof UnwindingProcRoot)) {
			paper_CLOSE(unwNode);
			UnwindingNode parent = (UnwindingNode)(unwNode.getIncomingEdges().get(0).getSource());
			if(!(parent instanceof UnwindingProcRoot)) {
				closeRec(parent);
			}
		}
	}
	
	private void paper_DFS(UnwindingNode v) {
		noDfs++;
		if(outputDebugInfo) {
			s_logger.debug("DFS has been called");
			logStats();
		}
				
		if(abortCondition()) return;
		if(m_returnToUnwind) return; 
		
		paper_CLOSE(v);
		if(v instanceof UnwindingErrorLocation) {
			paper_REFINE((UnwindingErrorLocation) v);
			closeRec(v);
		}
		paper_EXPAND(v);
		
		for(IEdge edge : v.getOutgoingEdges()) {
			UnwindingEdge unwEdge = (UnwindingEdge) edge;
			paper_DFS((UnwindingNode) unwEdge.getTarget());
		}
	}
	
	private void paper_EXPAND(UnwindingNode v) {
		noExpand++;
		if(outputDebugInfo) {
			s_logger.debug("EXPAND: open Nodes: " + m_openNodes);
			logStats();
		}
//		s_Logger.debug("EXPAND has been called");
		if(abortCondition()) return;
		
		if(!v.isCovered() && v.isLeaf()) {
			CFGExplicitNode cfgNode = v.m_cfgLocation;//TODO: getter
			v.set_isLeaf(false);
			m_openNodes.remove(v);
			for(IEdge edge : cfgNode.getOutgoingEdges()) {
				CFGEdge cEdge = (CFGEdge) edge;
				CFGExplicitNode cfgChild = (CFGExplicitNode) cEdge.getTarget();
				UnwindingNode unwChild = toUnwindingNode(cfgChild, v.m_procRoot);

				UnwindingEdge newEdge = 
					new UnwindingEdge(cfgNode.getTheory(), cEdge.getSMTAnnotations(), v, unwChild);
				unwChild.addIncomingEdge(newEdge);
				v.addOutgoingEdge(newEdge);
				
				m_openNodes.add(unwChild);

				updateVarNamesToTermsMapping(/*unwChild,*/ newEdge);

			}
		}
		
		if(outputDebugInfo)
			s_logger.debug("open Nodes after: " + m_openNodes);
	}

	/*
	 * method extracted from EXPAND for making it more readable - updates the dictionary of non-SSA 
	 * constants/variables used for the assertions in the nodes 
	 */
	@SuppressWarnings("unchecked")
	private void updateVarNamesToTermsMapping(/*UnwindingNode unwChild, */UnwindingEdge newEdge) {
		//update mapping of varNames to Terms 
//		for(Entry<String, TermVariable> e : ((SMTNodeAnnotations) unwChild.getPayload()
//				.getAnnotations().get("SMT")).getInVars().entrySet()) {
//			if(!m_VariableNameToTerm.containsKey(e.getValue().getName())) {
//				if(m_theory.getFunction(e.getKey(), new Sort[0]) == null){
//					updateVariableToTermNewFunction(e);
//				}
//				else {
//					m_VariableNameToTerm.put(e.getValue().getName(), m_theory.term(
//							m_theory.getFunction(e.getKey(), new Sort[0])));
//				}
//			}
//		}
		HashSet<TermVariable> inOut = new HashSet<TermVariable>();
		for(Entry<String, TermVariable> e : ((SMTEdgeAnnotations) newEdge.getPayload()
				.getAnnotations().get("SMT")).getInVars().entrySet()) {
			inOut.add(e.getValue());
			if(!m_VariableNameToTerm.containsKey(e.getValue().getName())) {
				if(m_theory.getFunction(e.getKey(), new Sort[0]) == null){
					updateVariableToTermNewFunction(e);
				}
				else {
					m_VariableNameToTerm.put(e.getValue().getName(), m_theory.term(
							m_theory.getFunction(e.getKey(), new Sort[0])));
				}
			}
		}
		for(Entry<String, TermVariable> e : ((SMTEdgeAnnotations) newEdge.getPayload().getAnnotations()
				.get("SMT")).getOutVars().entrySet()) {
			inOut.add(e.getValue());
			if(!m_VariableNameToTerm.containsKey(e.getValue().getName())) {
				if(m_theory.getFunction(e.getKey(), new Sort[0]) == null){
					updateVariableToTermNewFunction(e);
				}
				else {
					m_VariableNameToTerm.put(e.getValue().getName(), m_theory.term(
							m_theory.getFunction(e.getKey(), new Sort[0])));
				}
			}
		}
//		SMTEdgeAnnotations sea = ((SMTEdgeAnnotations) newEdge.getPayload().getAnnotations()
//				.get("SMT"));
//		sea.useSSA();
		HashSet<TermVariable> notInOut =(HashSet<TermVariable>) ((SMTEdgeAnnotations) newEdge.getPayload().getAnnotations()
				.get("SMT")).getVars().clone(); 
		notInOut.removeAll(inOut);
		//TODO: ziemlicher Hack -> muss auch anders gehen, überhaupt korrekt??? 
		// -- oder ist die Benennung hier für die Funktion egal, solange die Einträge da sind?
		for(TermVariable tv : notInOut) {
			String name = tv.getName();
			String[] spl = name.split("_");
			String inProgName = new String();
			for(int i = 0; i < spl.length; i++) {
				if(i == 1)
					inProgName = spl[i];
				else if(i < 0 && i != spl.length-1) 
					inProgName.concat("_" + spl[i]);
			}
			
			if(!m_VariableNameToTerm.containsKey(name/*e.getValue().getName()*/)) {
				if(m_theory.getFunction(inProgName/*e.getKey()*/, new Sort[0]) == null){
//					updateVariableToTermNewFunction(new Entry<String,TermVariable>());
					m_VariableNameToTerm.put(name, m_theory.term(
							m_theory.createFunction(inProgName, new Sort[0], tv.getSort())));	
					m_VariableNameToTerm.put(inProgName, m_theory.term( //reflexivität - wieso eigentlich?
							m_theory.getFunction(inProgName, new Sort[0])));
					m_ConstantsToVariableName.put(
							m_theory.term(m_theory.getFunction(inProgName, new Sort[0])), inProgName);
				}
				else {
					m_VariableNameToTerm.put(name, m_theory.term(
							m_theory.getFunction(inProgName, new Sort[0])));
				}
			}
		}
	}

	/*
	 * helper function making updateVarNamesToTermsMapping less redundant
	 */
	private void updateVariableToTermNewFunction(Entry<String, TermVariable> e) {
		m_VariableNameToTerm.put(e.getValue().getName(), m_theory.term(
				m_theory.createFunction(e.getKey(), new Sort[0], e.getValue().getSort())));	
		m_VariableNameToTerm.put(e.getKey(), m_theory.term( //reflexivität - wieso eigentlich?
				m_theory.getFunction(e.getKey(), new Sort[0])));
		m_ConstantsToVariableName.put(
				m_theory.term(m_theory.getFunction(e.getKey(), new Sort[0])), e.getKey());
	}

	private boolean abortCondition() {
		return false;//noExpand>6;
	}

	private void paper_REFINE(UnwindingErrorLocation v) {//check auf EL schon eingebaut
		noRefine++;
//		s_Logger.debug("REFINE has been called");
		//v is error loc. and not yet annotated false 
		//- the "false"-thing should be handled by openNodes
		if(/*(v instanceof UnwindingErrorLocation) 
				&&*/ (!v.getSMTAnnotations().getAssertion().equals(Atom.FALSE))) {
			ArrayList<IElement> path = getPath(v, new ArrayList<IElement>());
			
			//reset lists used in getFormulas
			for(Entry<String, ArrayDeque<TermVariable>> e : m_availableTermVariables.entrySet()) {
				m_availableTermVariables.get(e.getKey()).addAll(m_usedTermVariables.get(e.getKey()));
				m_usedTermVariables.get(e.getKey()).clear();	
			}
			
			Formula[] formulas = getFormulas(path);
			if(outputDebugInfo) {
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < formulas.length; i++) {
					sb.append("\n");
					sb.append(formulas[i]);
				}
				s_logger.debug("path: " + sb.toString());
			}
			//generiere refutation/Interpolantensequenz für path
			//wenn sie existiert: 
			//wenn sie nicht existiert: unsafe

			long startTime = System.currentTimeMillis();
			Formula[] interpolants = m_SMTInterface.computeInterpolants(formulas, null);
			timeErrorPathChecks += (System.currentTimeMillis() - startTime);
			noErrorPathChecks++;
			
			if(interpolants != null) {
				// update node annotations with the new interpolants
				// nur verstärken, wenn nicht eh schon impliziert (sonst wird immer geuncovert)
				// bei Verstärkung: davon gecovertes uncovern
				if(outputDebugInfo)
					s_logger.debug("REFINE: generated sequence of interpolants");
				for(int i = 0; i < interpolants.length; i++) {
					UnwindingNode nodeOnPath = (UnwindingNode) path.get(i*2+2); 
					SMTNodeAnnotations annotation = 
						(SMTNodeAnnotations) nodeOnPath.getPayload().getAnnotations().get("SMT");
					
					
					Formula oldFormula = transformFormula(annotation.getAssertion());//paper -> psi(v_i)
					Formula interpolant = transformFormula(interpolants[i]);
					
					if(outputDebugInfo)
						s_logger.debug("REFINE: checking if assertion is strengthend " 
							+ oldFormula + " =>? " + interpolant);
					if(checkNotImplies(oldFormula, interpolant) == 1) 
					{
						if(outputDebugInfo)
							s_logger.debug("REFINE: strengthening assertion with " + interpolant);
						Formula newFormula = m_theory.and(oldFormula, interpolant);
						annotation.setAssertion(newFormula);	

						if(nodeOnPath.get_coveredNodes().size() > 0) {
							//nodeOnPath covert nichts mehr
							for(UnwindingNode coveredNode : nodeOnPath.get_coveredNodes()) {
								coveredNode.set_coveringNode(null);
								coveredNode.setCovered(false);
								if(coveredNode.isLeaf()) 
								{
									m_openNodes.add(coveredNode);
								}
								uncoverRec(coveredNode.getOutgoingNodes());
							}
							nodeOnPath.get_coveredNodes().clear();
							//						seine nachfolger auch nicht - dürften sie eh nicht, 
							//						aber setCovered muss auf jeden Fall gesetzt werden
							uncoverRec(nodeOnPath.getOutgoingNodes()); 
						}
					}
				}
				v.set_isLeaf(false);
				m_openNodes.remove(v);
	
			} 
			else {
				s_logger.info("--------- P R O G R A M   U N S A F E ---------");
				StringBuilder errorPath = new StringBuilder();
				for(Formula f : formulas) {
					errorPath.append(f.toString() + "\n");
				}
				
				s_logger.info("Satisfiable Error Path: \n" + errorPath);
				m_programIncorrect = true;
			}
		}
	}

	private ArrayList<IElement> getPath(UnwindingNode v, ArrayList<IElement> path) {
		if(!(v instanceof UnwindingProcRoot)) {
			path.add(v);
			path.add(v.getIncomingEdges().get(0));
			return getPath((UnwindingNode)v.getIncomingNodes().get(0), path);	
		}
		else {
			path.add(v);
			Collections.reverse(path);//könnte man sich mit einer Deque/Queue sparen..
			return path;
		}
	}

	private void paper_CLOSE(UnwindingNode v) {
		noClose++;
		if(outputDebugInfo) {
			s_logger.debug("CLOSE has been called");
			logStats();
		}
//		if(returnImmediately) return;
		//TODO: geht das effizienter? ev +-1 beim index? - ev schneller mit for(int i ...)
		ArrayList<UnwindingNode> predecessors = new ArrayList<UnwindingNode>(
				v.m_procRoot.getPreorder().subList(0,
					v.getIndexInPreorder())); 
//		ArrayList<UnwindingNode> predecessors = 
//			v.m_procRoot.getLocationToPreorder().get(v.m_cfgLocation);
		
//		if(predecessors != null) {
			for(UnwindingNode pred : predecessors) {
				if(pred.m_cfgLocation.equals(v.m_cfgLocation)) {
					paper_COVER(v, pred);
				}
			}
//		}
//		for(int i = 0; i < v.getIndexInPreorder(); i++) {
//			if(v.m_procRoot.getPreorder().get(i).equals(v.m_cfgLocation)) {
//				paper_COVER(v, v.m_procRoot.getPreorder().get(i));
//			}
//		}
	}

	private void paper_COVER(UnwindingNode v, UnwindingNode w) {//w covert v ("v->w?")
		noCover++;
		//		s_Logger.debug("COVER has been called" + v + w);
//		if(returnImmediately) return;
		//ev nicht alle checks nötig..
		if((!v.isCovered()) && (!w.isCovered())//TODO ???
//				&& v.m_cfgLocation.equals(w.m_cfgLocation) //wird schon in CLOSE gecheckt //erübrigt sich durch procroot.locationstopreorder
//				&& v.getIndexInPreorder() > w.getIndexInPreorder() //wird notwending durch procroot.locationsToPreorder //-> dürfte wegen close nichts ändern
				)
		{
			if(outputDebugInfo)
				s_logger.debug("COVER: " + v + "->" + w + ": checking " 
					+ v.getSMTAnnotations().getAssertion() + " =>? " + w.getSMTAnnotations().getAssertion());
			Formula assertionV = /*transformFormula(*/v.getSMTAnnotations().getAssertion();//transformed wird schon in REFINE
			Formula assertionW = /*transformFormula(*/w.getSMTAnnotations().getAssertion();//wenn die formeln angelegt werden
			if(1 != checkNotImplies(assertionV, assertionW)) { 
				if(outputDebugInfo)
					s_logger.debug("COVER: implication valid -> covering..");
				v.set_coveringNode(w);
				w.get_coveredNodes().add(v);
				v.setCovered(true);
				m_openNodes.remove(v);
				
			
				//.. except for implicitly covering its descendants
				coverRec(v.getOutgoingNodes());
			}
		}
	}

	/*
	 * takes two formulas v,w , if "v implies w" is valid, it returns 0 (for "unsat(not (v -> w))")
	 * does memoization of the checked formulas
	 */
	private int checkNotImplies(Formula assertionV, Formula assertionW) {
		int result;
		Formula toCheck = m_theory.not(m_theory.implies(assertionV,	assertionW));
		if(!m_implicationDict.containsKey(toCheck)) {
			long startTime = System.currentTimeMillis();
//			Formula[] interpolants = m_SMTInterface.computeInterpolants(new Formula[]{toCheck}, null);
			result = m_SMTInterpol.checkSatisfiable(toCheck);
			timeImplicationChecks += (System.currentTimeMillis() - startTime);
			noImplicationChecks++;
//			if (interpolants == null){
//				result = m_SMTInterface.checkSatisfiabilitySmtlib(toCheck);
//			} else {
//				result = SMTInterface.SMT_UNSAT;
//			}
//			
			m_implicationDict.put(toCheck, result);
		}
		else {
			result = m_implicationDict.get(toCheck);
			if(outputDebugInfo)
				s_logger.debug("formula memoized, result: " + result);
//			s_logger.debug("size of dictionary:" + m_implicationDict.keySet().size());
		}
		return result;
	}
	
	/*
	 * transform an assertion coming from a set of interpolants 
	 * (the assertions in the nodes in lazy abstraction)
	 * in such a way that all its Termvariables are replaced by their nonSSA-counterparts
	 */
	private Formula transformFormula(Formula assertion) {
		if(assertion instanceof Atom) {
			if(assertion instanceof DistinctAtom) {
				Term[] newTerms = transformTerms(((DistinctAtom)assertion).getTerms());
				return m_theory.equals(newTerms);
			}
			else if(assertion instanceof EqualsAtom) {
				Term[] newTerms = transformTerms(((EqualsAtom)assertion).getTerms());
				return m_theory.equals(newTerms);
			}
			else if(assertion instanceof PredicateAtom) {
				Term[] newTerms = transformTerms(((PredicateAtom)assertion).getParameters());
				return m_theory.atom(((PredicateAtom)assertion).getPredicate(), newTerms);
			}
			else if(assertion instanceof VariableAtom) {
				return assertion;
			}
			else {
				return assertion; //must be Atom.TRUE/FALSE then..
			}
		}
		else if(assertion instanceof ConnectedFormula) {
			Formula lhs = transformFormula(((ConnectedFormula)assertion).getLhs());
			Formula rhs = transformFormula(((ConnectedFormula)assertion).getRhs());
			
			switch(((ConnectedFormula)assertion).getConnector()) {
			case 0:
				return m_theory.or(lhs, rhs);
			case 1:
				return m_theory.and(lhs, rhs);
			case 2:
				return m_theory.implies(lhs, rhs);
			case 3:
				return m_theory.iff(lhs, rhs);
			case 4:
				return m_theory.xor(lhs, rhs);
			case 5:
				return m_theory.oeq(lhs, rhs);					
			}
			
		}
		else if(assertion instanceof FletFormula) {
			return m_theory.flet(
					((FletFormula)assertion).getVariable(),//um FormulaVariables muss ich mich wohl nicht kümmern.. wohl ..
					transformFormula(((FletFormula)assertion).getValue()), 
					transformFormula(((FletFormula)assertion).getSubFormula()));
		}
		else if(assertion instanceof ITEFormula) {
			Formula cond = transformFormula(((ITEFormula)assertion).getCondition());
			Formula trueCase = transformFormula(((ITEFormula)assertion).getTrueCase());
			Formula falseCase = transformFormula(((ITEFormula)assertion).getFalseCase());
			return m_theory.ifthenelse(cond, trueCase, falseCase);
		}
		else if(assertion instanceof LetFormula) {
			Term varTerm = m_VariableNameToTerm.get(
					m_ConstantsToVariableName.get(
					((LetFormula)assertion).getVariable().getName()));
			TermVariable var = ((VariableTerm)varTerm).getVariable();
			
			Term[] valArray = new Term[1];
			Term val = ((LetFormula)assertion).getValue();
			valArray[0] = val;
			val = transformTerms(valArray)[0];
			
			Formula form = transformFormula(((LetFormula)assertion).getSubFormula());
			
			return m_theory.let(var, val, form);
		}
		else if(assertion instanceof NegatedFormula) {
			return m_theory.not(transformFormula(((NegatedFormula)assertion).getSubFormula()));
		}
		else if(assertion instanceof QuantifiedFormula) {
			switch(((QuantifiedFormula)assertion).getQuantifier()){
			case 0:
				return m_theory.exists(
						((QuantifiedFormula)assertion).getVariables(), ((QuantifiedFormula)assertion).getSubformula());
			case 1: 
				return m_theory.forall(
						((QuantifiedFormula)assertion).getVariables(), ((QuantifiedFormula)assertion).getSubformula());
			}
			//Achtung - keine Rekursion hier TODO: vermutlich müssen die quantifizierten Variablen 
			//bei der Transformation ausgespart werden
		}
		return null;
	}

	private Term[] transformTerms(Term[] terms) {
		ArrayList<Term> toReturn = new ArrayList<Term>();
		for(Term t : terms) {
			toReturn.add(transformTerm(t));
		}
		return toReturn.toArray(terms);
	}
	
	
	/*
	 * helper for transformAssertions - doing the same thing for terms
	 */
	private Term transformTerm(Term term) {
//		ArrayList<Term> toReturn = new ArrayList<Term>();
		
		if(term instanceof ApplicationTerm) {
			ApplicationTerm term1 = (ApplicationTerm) term;
			if(term1.getParameters().length != 0) {
				return m_theory.term(term1.getFunction(), transformTerms(term1.getParameters()));
			}
			else {
				Term tr = m_VariableNameToTerm.get(m_ConstantsToVariableName.get(term1));
				if(tr == null)
					throw new NullPointerException();
				return tr;
			}
			
		}
		else if(term instanceof ITETerm) {
			ITETerm term1 = (ITETerm) term;

			Term trueCase = transformTerm(term1.getTrueCase());
			Term falseCase = transformTerm(term1.getFalseCase());

			return m_theory.ite(transformFormula(term1.getCondition()),
					trueCase, falseCase);
		}
		else if(term instanceof NumeralTerm) {
//			NumeralTerm terms = (NumeralTerm) term;
			return term;
		}
		else if(term instanceof ProgramVariableTerm) {
			ProgramVariableTerm term1 = (ProgramVariableTerm) term;
			//TODO: etwas spekulativ - was sind PVTs genau?
			return m_VariableNameToTerm.get(m_ConstantsToVariableName.get(
						term1.toString().substring(0, term1.toString().indexOf(" :"))));
		}
		else if(term instanceof RationalTerm) {
//			RationalTerm terms1 = (RationalTerm) term;
			return term;//wie's aussieht stecken da keine Variablen drin
		}
		else if(term instanceof VariableTerm) {
			//ev quatsch/dürfte nicht vorkommen, da konstanten als 0-stellige Fkten modelliert sind
			VariableTerm term1 = (VariableTerm) term;
			return m_VariableNameToTerm.get
						(m_ConstantsToVariableName.get(term1.getVariable().getName()));
		}
		return null;
	}

	/*
	 * for each given element recursively (going down) remove all covering edges connected to it
	 * and mark it as covered (by a node above of it by convention..)
	 */
	private void coverRec(List<INode> outgoingNodes) {
		if(outgoingNodes == null) return;
		for(INode iNode : outgoingNodes) {
			UnwindingNode unwNode = (UnwindingNode) iNode;
			
			unwNode.setCovered(true);
			m_openNodes.remove(unwNode);
			
			if(unwNode.get_coveringNode() != null) {
				unwNode.get_coveringNode().get_coveredNodes().remove(unwNode);
			}
			
			for(UnwindingNode un : unwNode.get_coveredNodes()) {
				un.set_coveringNode(null);
				//TODO ?? .. vielleicht kritische stelle.. -> schein hack in unwind unnötig zu machen
				un.setCovered(false);				
//				un.get_coveredNodes().clear(); -> _muss_ eh leer sein
				
				if(un.isLeaf()) {
					m_openNodes.add(un);
				}
				//im falle eines nachkommen, wird hier auf false gesetzt, was später eh true wird..
				uncoverRec(un.getOutgoingNodes()); 
			}
			unwNode.get_coveredNodes().clear();
			unwNode.set_coveringNode(null);
			
			coverRec(unwNode.getOutgoingNodes());
		}
	}
	
	/*
	 * for each given element recursively (going down) set isCovered false and 
	 * clear the coveringNodes List 
	 */
	private void uncoverRec(List<INode> outgoingNodes) {
		if(outgoingNodes == null) return;
		for(INode iNode : outgoingNodes) {
			UnwindingNode unwNode = (UnwindingNode) iNode;
			
			unwNode.setCovered(false);
			if(unwNode.isLeaf()) {
				m_openNodes.add(unwNode);	
			}
						
			if(unwNode.get_coveringNode() != null) {
				unwNode.set_coveringNode(null);
			}

			uncoverRec(unwNode.getOutgoingNodes());
		}
	}

	/*
	 * make an UnwindingNode out of a CFGNode - if there are assertions other than True
	 * then make an edge to the Error Location and move them there 
	 */
	private UnwindingNode toUnwindingNode(CFGExplicitNode oldNode, UnwindingProcRoot procRoot) {
		noNodes++;
		
		UnwindingNode newNode = new UnwindingNode(procRoot, oldNode);
		Payload newPayload = PayloadModifier.copyPayload(oldNode.getPayload());
		Formula assertion = ((SMTNodeAnnotations)newPayload.getAnnotations().get("SMT")).getAssertion();
		
		if(!(assertion.equals(Atom.TRUE))) {
			//replace assertion with Atom.True in the Node
			HashMap<String,IAnnotations> newAnnots = newPayload.getAnnotations();
			SMTNodeAnnotations newA = new SMTNodeAnnotations();
			newA.setAssertion(Atom.TRUE);
			newAnnots.put("SMT", newA);
			newPayload.setAnnotations(newAnnots); //unnötig/Quatsch?? wg ref?
			
			//add an edge with the assertion pointing to the error location
//			UnwindingEdge errorEdge = new UnwindingEdge(m_theory, assertion, newNode, m_ErrorLocation);
//			newNode.addOutgoingEdge(errorEdge);
//			m_ErrorLocation.addIncomingEdge(errorEdge);
			//version for improved graph readability: one error location each
			UnwindingErrorLocation el = new UnwindingErrorLocation(procRoot, oldNode);
			UnwindingEdge errorEdge = new UnwindingEdge(m_theory, oldNode.getSMTAnnotations(), newNode, el, true);
			newNode.addOutgoingEdge(errorEdge);
			el.addIncomingEdge(errorEdge);
			el.set_isLeaf(true);
			newNode.set_isLeaf(true);
			//always do refinement on error locations first, thus insert them an the 
			//beginning of m_openNodes
			m_openNodes.add(el);
			//if a new Error location has been discovered we have to return to UNWIND with the new list
			//otherwise it will never find any error locations
			m_returnToUnwind = true;
//			if(oldNode.getOutgoingEdges(). size() == 0) //-> wohl böse, da dann expand auf den knoten nichts mehr macht
//				newNode.set_isLeaf(false);
		}
		newNode.setPayload(newPayload);		
		//helpers for debugging
		String oldName = newNode.getPayload().getName();
		String sUID = newNode.getPayload().getID().toString();
		newNode.getPayload().setName(oldName.replace("$Stalin#", "")
				+ "-" + sUID.substring(0, sUID.length()/6));
		newNode.getPayload().getAnnotations().put("LA", new LAAnnotations(newNode));
		newNode.getPayload().getAnnotations().remove("SC");
		
		return newNode;
	}

	private UnwindingProcRoot toUnwindingProcRoot(CFGExplicitNode oldNode) {
		//		UnwindingProcRoot newNode = new UnwindingProcRoot();
		//		newNode.setPayload(PayloadModifier.copyPayload(oldNode.getPayload()));
		//		return newNode;

		noNodes++;

		UnwindingProcRoot newNode = new UnwindingProcRoot();
		Payload newPayload = PayloadModifier.copyPayload(oldNode.getPayload());
		Formula assertion = ((SMTNodeAnnotations)newPayload.getAnnotations().get("SMT")).getAssertion();

		if(!(assertion.equals(Atom.TRUE))) {
			//replace assertion with Atom.True in the Node
			HashMap<String,IAnnotations> newAnnots = newPayload.getAnnotations();
			SMTNodeAnnotations newA = new SMTNodeAnnotations();
			newA.setAssertion(Atom.TRUE);
			newAnnots.put("SMT", newA);
			newPayload.setAnnotations(newAnnots); //unnötig/Quatsch?? wg ref?

			//add an edge with the assertion pointing to the error location
			//			UnwindingEdge errorEdge = new UnwindingEdge(m_theory, assertion, newNode, m_ErrorLocation);
			//			newNode.addOutgoingEdge(errorEdge);
			//			m_ErrorLocation.addIncomingEdge(errorEdge);
			//version for improved graph readability: one error location each
			UnwindingErrorLocation el = new UnwindingErrorLocation(newNode, oldNode);
			UnwindingEdge errorEdge = new UnwindingEdge(m_theory, oldNode.getSMTAnnotations(), newNode, el, true);
			newNode.addOutgoingEdge(errorEdge);
			el.addIncomingEdge(errorEdge);
			el.set_isLeaf(true);
			newNode.set_isLeaf(true);
			//always do refinement on error locations first, thus insert them at the 
			//beginning of m_openNodes (this is ensured by the unwcomparator)
			m_openNodes.add(el);
			//if a new Error location has been discovered we have to return to UNWIND with the new list
			//otherwise it will never find any error locations
			m_returnToUnwind = true;
			//			if(oldNode.getOutgoingEdges(). size() == 0) //-> wohl böse, da dann expand auf den knoten nichts mehr macht
			//				newNode.set_isLeaf(false);
		}
		newNode.setPayload(newPayload);		
		//helpers for debugging
//		String oldName = newNode.getPayload().getName();
//		String sUID = newNode.getPayload().getID().toString();
//		newNode.getPayload().setName(oldName.replace("$Stalin#", "")
//				+ "-" + sUID.substring(0, sUID.length()/6));
		newNode.getPayload().getAnnotations().put("LA", new LAAnnotations(newNode));
		newNode.getPayload().getAnnotations().remove("SC");
		
		newNode.m_cfgLocation = oldNode;
		//the variable mappings are not needed in the nodes anymore
		//		((SMTNodeAnnotations)newNode.getPayload().getAnnotations().get("SMT")).setInVars(null);
		//		((SMTNodeAnnotations)newNode.getPayload().getAnnotations().get("SMT")).setOldVars(null);
		//		((SMTNodeAnnotations)newNode.getPayload().getAnnotations().get("SMT")).setVars(null);

		return newNode;
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

	@Override
	public boolean performedChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the root of the CFG.
	 */
	public INode getRoot(){
		s_logger.debug("getRoot has been called");
		boolean doLA = true; //TODO: only for debugging -> viewing the cfg without changing the toolchain
		if(doLA)
			return (INode) m_graphroot;
		else
			return null;
	}

	private Formula[] getFormulas(ArrayList<IElement> path) {
		//container for the Formulas to be returned
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		//we are not interested in the nodes: make a list of the edges only
		ArrayList<UnwindingEdge> edges = new ArrayList<UnwindingEdge>();
		for(int i = 1; i < path.size(); i+=2) {
			edges.add((UnwindingEdge) path.get(i));
		}
		
		HashMap<String, Term> availableVars = new HashMap<String,Term>();
//		HashSet<TermVariable> usedVars = new HashSet<TermVariable>(); //?

		//add the transformed formulas to the returned array
		for(UnwindingEdge edge : edges) {
//			Formula newFormula;
			Formula assumption = edge.getSMTAnnotations().getAssumption();
			
			HashSet<TermVariable> notInOutVars = 
				(HashSet<TermVariable>) edge.getSMTAnnotations().getVars().clone();
			
			for(Entry<String, TermVariable> entry : edge.getSMTAnnotations().getInVars().entrySet()) {
				notInOutVars.remove(entry.getValue());
				//wohin der wert von key zuletzt gespeichert wurde
				Term tvIn = availableVars.get(entry.getKey());
				if(tvIn == null) { //the variable is not initialised at that program point
					tvIn = makeConstant(entry.getValue());
				}
				//let entry.value = tvIn
				assumption = m_theory.let(entry.getValue(), tvIn, assumption);
				if(edge.getSMTAnnotations().getOutVars().containsValue(entry.getValue())) {
					availableVars.put(entry.getKey(), tvIn);
				}
			}
			
			for(Entry<String, TermVariable> entry : edge.getSMTAnnotations().getOutVars().entrySet()) {
				notInOutVars.remove(entry.getValue());
				if(!edge.getSMTAnnotations().getInVars().containsValue(entry.getValue())) {
					Term tOut = makeConstant(
							getUniqueTermVariable(entry), entry.getKey());
					assumption = m_theory.let(entry.getValue(), tOut, assumption);
					availableVars.put(entry.getKey(), tOut);
				}
			}
			
			for(TermVariable tv : notInOutVars) {
				Term con = makeConstant(tv);
				assumption = m_theory.let(tv, con, assumption);
			}
//			availableVars.putAll(edge.getSMTAnnotations().getOutVars());//noch frisch machen
			
			formulas.add(assumption);
		}
		formulas.add(Atom.TRUE); // ev am Abschluss nötig..?
		Formula[] fArray = new Formula[formulas.size()];
		return formulas.toArray(fArray);
	}

	
	/**
	 * @param entry 
	 * @return a Termvariable that is not yet used in the current path
	 */
	private TermVariable getUniqueTermVariable(Entry<String, TermVariable> entry) {
		if(m_availableTermVariables.get(entry.getKey()) == null) {
			m_availableTermVariables.put(entry.getKey(), new ArrayDeque<TermVariable>());
		}
		if(m_usedTermVariables.get(entry.getKey()) == null) {
			m_usedTermVariables.put(entry.getKey(), new ArrayDeque<TermVariable>());
		}
		if(m_availableTermVariables.get(entry.getKey()).isEmpty())/*m_availableTermVariables.isEmpty())*/ {
			TermVariable newTV = m_theory.createFreshTermVariable(
					/*"v"*/entry.getKey(), entry.getValue().getSort());
			m_usedTermVariables.get(entry.getKey()).push(newTV);
			return newTV;
		}
		else {
			TermVariable tv = m_availableTermVariables.get(entry.getKey()).pop();
			m_usedTermVariables.get(entry.getKey()).push(tv);
			return tv;
		}
	}
	

	/*returns new constant for variable or
	 * returns constant that has already been created for this variable before (taken from SafetyChecker)
	 */
	private Term makeConstant(TermVariable tv){
		//new name for constant variable
		String constName = tv.getName() + "_const";
		//need a list of sorts of the input parameters of the function
		Sort[] dummy_Sorts = {};
		/*faking constant by creating function without input parameters and getting function symbol of newly created fake constant or
		 * of old fake constant that has already been created before*/
		FunctionSymbol fsym = m_theory.getFunction(constName, dummy_Sorts);
		if (fsym == null)
			fsym = m_theory.createFunction(constName, dummy_Sorts, tv.getSort());
		//need list of terms for input parameters of function in order to create term out of it
		Term[] dummyTerms = {};
		//making constant term and returning it
		Term const_Term = m_theory.term(fsym, dummyTerms);
//		const_to_Variable.put(const_Term, tv);
		m_ConstantsToVariableName.put(const_Term, tv.getName());
		return const_Term;
	}
	
	/*returns new constant for variable or
	 * returns constant that has already been created for this variable before (taken from SafetyChecker)
	 */
	private Term makeConstant(TermVariable tv, String name){
		//new name for constant variable
		String constName = tv.getName() + "_const";
		//need a list of sorts of the input parameters of the function
		Sort[] dummy_Sorts = {};
		/*faking constant by creating function without input parameters and getting function symbol of newly created fake constant or
		 * of old fake constant that has already been created before*/
		FunctionSymbol fsym = m_theory.getFunction(constName, dummy_Sorts);
		if (fsym == null)
			fsym = m_theory.createFunction(constName, dummy_Sorts, tv.getSort());
		//need list of terms for input parameters of function in order to create term out of it
		Term[] dummyTerms = {};
		//making constant term and returning it
		Term const_Term = m_theory.term(fsym, dummyTerms);
//		const_to_Variable.put(const_Term, tv);
		m_ConstantsToVariableName.put(const_Term, name);
		return const_Term;
	}
	
}
/*
 * the comparator for UnwindingNodes: It takes care that Error Locations are 
 * always sorted in at the beginning and, at second priority, takes the order of the 
 * expansion of the nodes into account - sort of preorder..
 */
class UNWComparator implements Comparator<UnwindingNode> {

	@Override
	public int compare(UnwindingNode o1, UnwindingNode o2) {
		if(o1 instanceof UnwindingErrorLocation) {
			if(o2 instanceof UnwindingErrorLocation) {
				if(o1.equals(o2)) {
					return 0;
				}
				else {
					return -1;
				}
			}
			else {
				return -1;				
			}

		}
		else if (o2 instanceof UnwindingErrorLocation){
			return 1;
		}
		else {//TODO: - richtigrum? - scheint so..
			return o1.getIndexInPreorder() - o2.getIndexInPreorder();	
		}
	}
	
}