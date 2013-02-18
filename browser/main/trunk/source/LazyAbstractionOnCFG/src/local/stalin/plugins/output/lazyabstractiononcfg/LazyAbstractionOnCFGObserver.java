package local.stalin.plugins.output.lazyabstractiononcfg;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import local.stalin.SMTInterface.SMTInterface;
import local.stalin.access.IUnmanagedObserver;
import local.stalin.access.WalkerOptions;
//import local.stalin.boogie.cfgreducer.CFGEdge;
//import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.CFGEdge;
import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.PayloadModifier;
import local.stalin.boogie.cfgreducer.SMTEdgeAnnotations;
import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
//import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
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
import local.stalin.plugins.ResultNotifier;




import org.apache.log4j.Logger;

/**
 * Auto-Generated Stub for the plug-in's Observer
 */
public class LazyAbstractionOnCFGObserver implements IUnmanagedObserver {

	private static Logger s_logger = StalinServices.getInstance().getLogger(Activator.s_PLUGIN_ID);

	//	private boolean m_finished = false;
	private boolean debugOutput = true;

	private SMTInterface 		m_SMTInterface	 			= null;
	private Theory 				m_theory 					= null;
	private HashMap<String, Term>		m_outConstants				= new HashMap<String, Term>();
	private HashMap<Term, String>		m_constantsToVariableName	= new HashMap<Term, String>();
	//	private HashMap<Formula, Integer> m_implicationDict = new HashMap<Formula, Integer>();

	GraphWriter gw;	


	HashMap<String,ArrayDeque<TermVariable>> m_availableTermVariables =
		new HashMap<String,ArrayDeque<TermVariable>>();
	HashMap<String,ArrayDeque<TermVariable>> m_usedTermVariables = 
		new HashMap<String,ArrayDeque<TermVariable>>();

	@Override
	public boolean process(IElement root) {
		s_logger.debug(" --------------- LazyAbstractionOnCFG starting ------------------");		
			
		CFGExplicitNode cfgRoot;

		if(root instanceof CFGExplicitNode){
			cfgRoot = (CFGExplicitNode) root;
		}
		else {
			s_logger.error("LazyAbstractionONCFG only runs on the output of ErrorLocationGenerator");
			return false;
		}

		m_theory		= cfgRoot.getTheory();
		m_SMTInterface = new SMTInterface(m_theory, SMTInterface.SOLVER_GROUNDIFY);
		gw = new GraphWriter(true, true, true , false, m_theory);


		int graphIndex = 0;
		while(true) {
			graphIndex++;			

			s_logger.debug("starting on new CFG");
			gw.writeGraphAsImage(cfgRoot, "graph_" + graphIndex + "_a_repeat");

			ArrayList<IElement> error_trace = searchShortestPath_BFS(cfgRoot);


			if (error_trace == null) {
				ResultNotifier.programCorrect();
				s_logger.info("-------------- P R O G R A M   S A F E -----------------");
				break;
			}
			else {//TODO: nicht so schön...
				error_trace = new ArrayList<IElement>(error_trace.subList(2, error_trace.size()));	
			}

			//reset lists used in getFormulas
			for(Entry<String, ArrayDeque<TermVariable>> e : m_availableTermVariables.entrySet()) {
				m_availableTermVariables.get(e.getKey()).addAll(m_usedTermVariables.get(e.getKey()));
				m_usedTermVariables.get(e.getKey()).clear();	
			}

			Formula[] formulas = getFormulas(error_trace);

			if(debugOutput) { 
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < formulas.length; i++)
					sb.append(formulas[i] + "\n");
				s_logger.debug("computing interpolants for: \n" + sb);
			}
			Formula[] interpolants = m_SMTInterface.computeInterpolants(formulas, null);
			if(interpolants == null) {
				ResultNotifier.programIncorrect();

				StringBuilder sb = new StringBuilder();
				for(int i = 1; i < error_trace.size(); i += 2) {
					Formula formula = ((CFGEdge)error_trace.get(i)).getAssumption();
					FormulaUnFleterer unFleterer = new FormulaUnFleterer(m_theory);
					formula = unFleterer.unflet(formula);
					sb.append(formula + "\n");
				}
				s_logger.info("-------------- P R O G R A M   U N S A F E -----------------");
				s_logger.info("error trace: \n" + sb);
				break;
			}


			HashMap<CFGExplicitNode, CFGExplicitNode> nodeToCopy = 
				new HashMap<CFGExplicitNode, CFGExplicitNode>();

			//--------------------------------------------------------------------------
			// make a copy of each node along the error trace, its assertion is strengthened with
			// the interpolant, don't bother with incoming edges, but duplicate outgoing edges
			for(int i = 2; i < error_trace.size() - 2; i += 2) { //TODO berechnung checken
				CFGExplicitNode node = (CFGExplicitNode) error_trace.get(i);




				//TODO indexberechung checken
				//                              Formula newFormula = m_theory.and(node.getAssertion(), interpolants[i/2 - 1]); 
				//auÃerdem: in- und outvars fÃŒr copy-node neu setzen -> da die interpolante hinzukommt
				//eigenschaft der Interpolante: 
				//alle enthaltenen variablen sind in mindestens einer der beiden hinzugezogenen Formeln enthalten
				//also: entweder in den (outvars??) aller vorhergehenden - das letzte Auftauchen ist dann relevant 
				//- oder im folgeknoten (invars??) 
				//Evren macht das auch so Ã€hnlich:

				//--aktuelle interpolante
				Formula tmp_interpolant = interpolants[i/2 - 1];

				//--alle konstanten in der Formel extrahieren
				HashSet<Term> constants = ConstantsGetter.getConstants(tmp_interpolant);
				//jeder dieser Konstanten ihre zugehörige Variable zuordnen
				HashMap<Term,TermVariable> constToVar = new HashMap<Term,TermVariable>();
				for(Term c : constants) {
					constToVar.put(c, m_theory.createTermVariable(m_constantsToVariableName.get(c), c.getSort()));
				}

				SMTNodeAnnotations oldsmtna = node.getSMTAnnotations(); 
				HashMap<String, TermVariable> additionalInVars =
					getCorrespondingVarMapping(error_trace, constToVar.values(), i);


				//--in der formel alle Konstanten durch ihre zugehörigen Variablen ersetzen
				// reverts all substitute variables of the interpolant to the correct variables of the specific node
				ConstToVariableVisitor ctvVisitor = new ConstToVariableVisitor(constToVar,
						m_constantsToVariableName, node, m_theory);
				FormulaWalker formulaWalker = new FormulaWalker(ctvVisitor, m_theory);
				Formula interpolant = formulaWalker.process(tmp_interpolant);

				//ersetze alle konstanten in tmp_interpolant durch die zugehÃ¶rige Variable, wie sie in 
				// invars vom knoten mit ssa versehen wurde - was wenn sie im Knoten nicht auftaucht?
				// dann mit der letzen outVar, wo sie auftaucht..



				//-- alle Variablen in der Formel anhand der InVars-Zuordnung so "verletten",
				//-- dass sie die ssa-Variante zugeordnet bekommen, die sie auch schon in der
				//-- Formel, mit der der Knoten vorher annotiert war, hatten
				//-- wenn sie dort in InVars sind, lässt sich diese direkt finden
				//-- wenn nicht, und sie dennoch in Vars sind, dann gibt es ein problem
				//-- wenn sie nicht in Vars sind, dann muss ihr die ssa-Variable zugeordnet
				//-- werden, die zur richtigen nicht-SSA-Var gehört und davon die, die zuletzt
				//-- als outVar aufgetaucht ist
				//-- 2 Aufgaben also: 
				//-- "bel. ssa to non-ssa" 
				//-- und "non_ssa zu für diesen Programmpunkt richtige ssa"
				for(TermVariable tv : FreeVariableGetter.getFreeVariables(interpolant)){
					//HACK: alternative: HashMap<TermVariable, String> anhand aller
					String inProgName = new String();
					String name = tv.getName();

					String[] spl = name.split("_");

					for(int j = 0; j < spl.length; j++) {
						if(j == 1)
							inProgName = spl[j];
						else if(j < 0 && j != spl.length-1) 
							inProgName.concat("_" + spl[j]);
					}
					interpolant = m_theory.let(tv, 
							m_theory.term(node.getSMTAnnotations().getInVars().get(inProgName)),
							interpolant);   
				}

				Formula newFormula = m_theory.and(node.getAssertion(), interpolant);

				CFGExplicitNode copy = new CFGExplicitNode(null, null); //will be set later

				SMTNodeAnnotations smtna = new SMTNodeAnnotations();

				smtna.setTheory(m_theory);
				if(oldsmtna != null) {
					//-- wird unnötig und würde auch nicht funktionieren, da ja einem String nur
					//-- max eine ssa-variante zugeordnet werden kann (Funktionseigenschaft)
					//smtna.getInVars().putAll(additionalInVars); 
					smtna.getInVars().putAll(oldsmtna.getInVars());
					smtna.getOldVars().putAll(node.getSMTAnnotations().getInVars());
					//                                      smtna.setOldVars(oldsmtna.getOldVars());
					smtna.getVars().addAll(oldsmtna.getVars());
					//                                      smtna.getVars().addAll(additionalInVars.values());
				}
				HashMap<String, IAnnotations> newAnns = new HashMap<String, IAnnotations>();
				newAnns.put("SMT", smtna);

				//                              HashMap<TermVariable, String> tvToName = new HashMap<TermVariable, String>();
				//                              for(Entry<String, TermVariable> en : additionalInVars.entrySet()) {
				//                                      tvToName.put(en.getValue(), en.getKey());
				//                              }
				//                              for(Entry<String, TermVariable> en : oldsmtna.getInVars().entrySet()) {
				//                                      tvToName.put(en.getValue(), en.getKey());
				//                              }

				//                              newFormula = VariableEqualizer.equalizeVariables(newFormula, tvToName, smtna.getInVars(), m_theory);

				smtna.setAssertion(newFormula);

				Payload np = new Payload();
				np.setName(node.getPayload().getName() 
						+ "=cp(" + node.getPayload().getID().toString().substring(0,4) + ")");

				np.setAnnotations(newAnns);

				copy.setPayload(np);

				for(IEdge edge : node.getOutgoingEdges()) {
					CFGEdge outgoing_edge = (CFGEdge) edge;
					CFGEdge newEdge = new CFGEdge(m_theory, null,
							copy, outgoing_edge.getTarget());
					newEdge.setPayload(outgoing_edge.getPayload());
				}
				nodeToCopy.put(node, copy);
			}

			gw.writeGraphAsImage(cfgRoot, "graph_" + graphIndex + "_b_copied");
			
			//--------------------------------------------------------------------------
			//check along the error trace if any incoming edges can be bent over towards
			//the copy of a node - do it in case
			for(int i = 2; i < error_trace.size() - 2; i += 2) {
				CFGExplicitNode node = (CFGExplicitNode) error_trace.get(i);
//				ArrayList<IEdge> incomingCopy = new ArrayList<IEdge>(node.getIncomingEdges());
				ArrayList<CFGEdge> toRemove = new ArrayList<CFGEdge>();

				for(Iterator<IEdge> it = node.getIncomingEdges().iterator(); it.hasNext();) {
					CFGEdge incoming_edge = (CFGEdge)it.next();//  node.getIncomingEdges().get(j);

					CFGEdge hypoEdge = new CFGEdge(m_theory, incoming_edge.getAssumption(),
							incoming_edge.getSource(), nodeToCopy.get(node));
					hypoEdge.setPayload(incoming_edge.getPayload());

//					s_logger.debug("checking Edge with formulas:" 
//						+ "source node:" + ((CFGExplicitNode)hypoEdge.getSource()).getAssertion()
//						+ "edge: " + ((CFGEdge)hypoEdge).getAssumption()  
//						+ "target node: " + ((CFGExplicitNode)hypoEdge.getTarget()).getAssertion());


					int bendEdge = hypoEdge.checkValidity();//incoming_edge.checkEdge(true);
					//bend if 0, don't otherwise
					if(bendEdge == 0) {
						s_logger.debug("bending over edge" + incoming_edge);
//						incoming_edge.setTarget(nodeToCopy.get(node));
						toRemove.add(incoming_edge);
					}
					else {
						hypoEdge.deleteEdge();
					}
				}
				for(CFGEdge ed : toRemove) {
					ed.deleteEdge();
				}
			}



			//TODO: invalid edges entfernen? - 
//			gibt's überhaupt welche?, außer dem letzen von der copy zur EL/lohnt sich's? 
			//wenn man die letzte Edge nicht entfernt, gibt es keinen Fortschritt 
			//- versuch: nur die von der letzten kopie zur EL löschen
			for(int i = 2; i < error_trace.size(); i += 2) {
				CFGExplicitNode node = (CFGExplicitNode) error_trace.get(i);
				node.cleanUnsatisfiableEdges();
			}
			//			CFGEdge toRemove = null;
			//			for(IEdge ed : ((CFGExplicitNode)error_trace.get(error_trace.size() - 1)).getIncomingEdges()) {
			//				if(ed.getSource() == nodeToCopy.get(error_trace.get(error_trace.size() - 3))) {
			//					toRemove = (CFGEdge) ed;
			//				}
			//			}
			//			toRemove.deleteEdge();

			//			for(int i = 2; i < error_trace.size(); i += 2) {
			//				CFGExplicitNode node = (CFGExplicitNode) error_trace.get(i);
			//				for(IEdge edge : node.getIncomingEdges()) {
			//					CFGEdge incoming_edge = (CFGEdge) edge;
			//					
			//					//determine whether edge is satisfiable -> if not, remove it
			//					int dontRemoveEdge = checkImplication(
			//							m_theory.and(
			//									  ((CFGExplicitNode)incoming_edge.getSource()).getAssertion(), 
			//									  incoming_edge.getAssumption()),
			//									nodeToCopy.get(node).getAssertion(),
			//									false);
			//				}
			//			}

			//			if(m_finished) //TODO: ev. unnötig..
			//				break;
		}
		return false;
	}

	/*
	 * takes an error trace, the corresponding sequence of interpolants and the index of the
	 * element of the trace to care about
	 * returns an InVars-Mapping for the interpolant
	 */
	private HashMap<String, TermVariable> getCorrespondingVarMapping(
			ArrayList<IElement> errorTrace, Collection<TermVariable> tvSet, int curNodeIndex) {
		HashMap<String, TermVariable> mapping = new HashMap<String, TermVariable>();
		//TODO: passt das mit dem Index? -> was, wenn die Interpolante Vars enthält
		//die nur auf der rechten Seite der implikation auftauchen?
		for(int i = 0; i <= curNodeIndex; i++) {//TODO: < oder <=??
			if(i % 2 == 0) {
				CFGExplicitNode node = (CFGExplicitNode)errorTrace.get(i);
				for(Entry<String, TermVariable> en : node.getSMTAnnotations().getInVars().entrySet()) {
					if(tvSet.contains(en.getValue()))
						mapping.put(en.getKey(), en.getValue());
				}
			}
			else {
				CFGEdge edge = (CFGEdge)errorTrace.get(i);
				for(Entry<String, TermVariable> en : edge.getSMTAnnotations().getInVars().entrySet()) {
					if(tvSet.contains(en.getValue()))
						mapping.put(en.getKey(), en.getValue());
				}
				for(Entry<String, TermVariable> en : edge.getSMTAnnotations().getOutVars().entrySet()) {
					if(tvSet.contains(en.getValue()))
						mapping.put(en.getKey(), en.getValue());
				}
			}

		}
		//		HashSet<TermVariable> freeVariables = fw.getFreeVariables(interpolant);
		return mapping;
	}



	// -------------------------------------------------------------------------
	// ----------------------- search shortest Path ----------------------------
	// -------------------------------------------------------------------------


	// alternative search with BFS -> taken from SafetyChecker
	private ArrayList<IElement> searchShortestPath_BFS(CFGExplicitNode m_Graphroot) {
		ArrayList<IEdge> m_searchStack = new ArrayList<IEdge>();
		//		m_searchStack.clear();
		for (IEdge e: m_Graphroot.getOutgoingEdges()) {
			m_searchStack.add(e);
		}

		int i = 0;
		// if the search stack still holds edges that might lead to an error continue...
		while(i < m_searchStack.size()) {
			// get the current node which will be expanded
			INode node = m_searchStack.get(i).getTarget();
			// run through all descendants... 
			for(IEdge e: node.getOutgoingEdges()) {
				INode target = e.getTarget();
				// check if descendant has already been visited by another path(shorter path by construction of BFS)
				if (getNodeFromSearchStack(target, m_searchStack)  < 0) {
					// append new edge to search stack since descendant has not been visited yet
					m_searchStack.add(e);
					// check if descendant is an error node
					if (target.getPayload().getName().contains("ERROR")) {
						return buildErrorPath(m_searchStack.indexOf(e), m_searchStack, m_Graphroot);
						//						return true;
					}
				}
			}
			i++;
		}
		return null;
	}

	// builds the error path by backtracking through search stack -> taken from SafetyChecker
	private ArrayList<IElement> buildErrorPath(
			int errorIndex, ArrayList<IEdge> m_searchStack, CFGExplicitNode m_Graphroot) {
		ArrayList<IElement> m_ShortestPath = new ArrayList<IElement>();
		int i = errorIndex;
		while (i >= 0){ 
			IEdge e = m_searchStack.get(i);
			m_ShortestPath.add(0, e.getTarget());
			m_ShortestPath.add(0, e);
			i = getNodeFromSearchStack(e.getSource(), m_searchStack);
		}
		m_ShortestPath.add(0, m_Graphroot);
		return m_ShortestPath;
	}

	// returns the index of the edge in the search stack that leads to the node else returns -1
	//-> taken from SafetyChecker
	private int getNodeFromSearchStack(INode node, ArrayList<IEdge> m_searchStack) {
		for (IEdge e: m_searchStack) {
			if (e.getTarget() == node) {
				return m_searchStack.indexOf(e);
			}
		}
		return -1;
	}	

	// -------------------------------------------------------------------------
	// ----------------------- get Formulas         ----------------------------
	// -------------------------------------------------------------------------

	//	private Formula[] getFormulas(ArrayList<IElement> path){
	//		
	//		
	//		return null;
	//	}


	//returns an array of the formulas corresponding to the current shortest path
	@SuppressWarnings("unchecked")
	private Formula[] getFormulas(ArrayList<IElement> path){
		//container for the Formulas to be returned
		ArrayList<Formula> formulas = new ArrayList<Formula>();

		HashMap<String, Term> availableVars = new HashMap<String,Term>();
		//		HashSet<TermVariable> usedVars = new HashSet<TermVariable>(); //?

		//add the transformed formulas to the returned array
		for(int i = 0; i < path.size(); i += 2) {
			CFGExplicitNode node = (CFGExplicitNode) path.get(i);


			Formula assertion = node.getSMTAnnotations().getAssertion();


			HashSet<TermVariable> edge_notInOutVars = 
				(HashSet<TermVariable>) node.getSMTAnnotations().getVars().clone();

			//add the let's for inVars			
			for(Entry<String, TermVariable> entry : node.getSMTAnnotations().getInVars().entrySet()) {
				edge_notInOutVars.remove(entry.getValue());
				//wohin der wert von key zuletzt gespeichert wurde
				Term tvIn = availableVars.get(entry.getKey());
				if(tvIn == null) { //the variable is not initialised at that program point
					tvIn = makeConstant(entry.getValue());
				}
				//let entry.value = tvIn
				assertion = m_theory.let(entry.getValue(), tvIn, assertion);
			}

			Formula assumption;
			if(i < path.size() - 1) {
				CFGEdge edge = (CFGEdge) path.get(i+1);
				assumption = edge.getSMTAnnotations().getAssumption();
				//add the let's for inVars			
				for(Entry<String, TermVariable> entry : edge.getSMTAnnotations().getInVars().entrySet()) {
					edge_notInOutVars.remove(entry.getValue());
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


				//add the let's for outVArs
				for(Entry<String, TermVariable> entry : edge.getSMTAnnotations().getOutVars().entrySet()) {
					edge_notInOutVars.remove(entry.getValue());
					if(!node.getSMTAnnotations().getInVars().containsValue(entry.getValue())) {
						Term tOut = makeConstant(
								getUniqueTermVariable(entry), "_" + entry.getKey() + "_"); //TODO:Hack!!
						assumption = m_theory.let(entry.getValue(), tOut, assumption);
						availableVars.put(entry.getKey(), tOut);
					}
				}
			} 
			else {
				assumption = Atom.TRUE;
			}
			
			Formula conjunction = m_theory.and(assertion, assumption);

			for(TermVariable tv : edge_notInOutVars) {
				Term con = makeConstant(tv);
				conjunction = m_theory.let(tv, con, conjunction);
			}
			//			availableVars.putAll(edge.getSMTAnnotations().getOutVars());//noch frisch machen

			formulas.add(conjunction);
		}
		//		formulas.add(Atom.TRUE); // ev am Abschluss nötig..?
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
	 * returns constant that has already been created for this variable before
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
		m_constantsToVariableName.put(const_Term, tv.getName());
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
		m_constantsToVariableName.put(const_Term, name);
		return const_Term;
	}


	// -------------------------------------------------------------------------
	// -----------------------    interface stuff   ----------------------------
	// -------------------------------------------------------------------------

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

}
