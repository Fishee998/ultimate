package local.stalin.boogie.cfgreducer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;

import local.stalin.core.api.StalinServices;
import local.stalin.logic.Atom;
import local.stalin.logic.Formula;
import local.stalin.logic.FormulaUnFleterer;
import local.stalin.logic.FunctionSymbol;
import local.stalin.logic.Sort;
import local.stalin.logic.Term;
import local.stalin.logic.TermVariable;
import local.stalin.logic.Theory;
import local.stalin.model.IAnnotations;
import local.stalin.model.IEdge;
import local.stalin.model.INode;
import local.stalin.model.IPayload;
import local.stalin.model.Payload;
import local.stalin.SMTInterface.SMTInterface;

public class CFGEdge implements IEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5142112003169579223L;

	private CFGExplicitNode 	m_Source = null;
	private CFGExplicitNode 	m_Target = null;
	
	static private int counter = 0;
	private Payload 			m_Payload 		= new Payload();
	private Theory				m_Theory;
	private static Logger		s_Logger		= StalinServices.getInstance().getLogger(Activator.PLUGIN_ID);
	private static SMTInterface s_SMTInterface	= null;
	private static int			s_TheoremProverCalls 	= 0;
	private static long			s_totalTime				= 0;
	
	
	public CFGEdge(Theory theory, Formula assumption, INode source, INode target){
		m_Theory = theory;
		setSource((CFGExplicitNode)source);
		setTarget((CFGExplicitNode)target);
		HashMap<String, IAnnotations>	annotations = new HashMap<String, IAnnotations>();
		SMTEdgeAnnotations				annotation	= new SMTEdgeAnnotations();
		annotations.put("SMT", annotation);
		assumption = assumption != null? assumption: Atom.TRUE;
		annotation.setAssumption(assumption);
		annotation.setTheory(theory);
		m_Payload.setAnnotations(annotations);
	}
	
	@Override
	public INode getSource() {
		return m_Source;
	}

	@Override
	public INode getTarget() {
		return m_Target;
	}

	public void resetStats() {
		s_TheoremProverCalls = 0;
		s_totalTime = 0;
	}
	
	public int getTheoremProverCount() {
		return s_TheoremProverCalls;
	}
	
	public long getTotalTime() {
		return s_totalTime;
	}
	
	@Override
	public void setSource(INode source) {
		CFGExplicitNode oldSource = m_Source;
		m_Source = (CFGExplicitNode)source;
		
		if(source != null && 
				!source.getOutgoingEdges().contains(this)){
			source.addOutgoingEdge(this);
		}
		if(oldSource != null && 
				oldSource.getOutgoingEdges().contains(this)){
			oldSource.removeOutgoingEdge(this);
		}
		checkName();
	}

	@Override
	public void setTarget(INode target) {
		CFGExplicitNode oldTarget = m_Target;
		m_Target = (CFGExplicitNode)target;
		
		if(target != null && 
				!target.getIncomingEdges().contains(this)){
			target.addIncomingEdge(this);
		}
		if(oldTarget != null && 
				oldTarget.getIncomingEdges().contains(this)){
			oldTarget.removeIncomingEdge(this);
		}
		checkName();
	}

	public void checkName(){
		if(m_Source != null && m_Target != null){
			m_Payload.setName(m_Source.getPayload().getName() + "->" + m_Target.getPayload().getName());
		}
	}
	
	@Override
	public IPayload getPayload() {
		return m_Payload;
	}

	public SMTEdgeAnnotations getSMTAnnotations(){
		return (SMTEdgeAnnotations)m_Payload.getAnnotations().get("SMT");
	}
	
	@Override
	public boolean hasPayload() {
		return m_Payload.isValue();
	}

	@Override
	public void setPayload(IPayload payload) {
		m_Payload = (Payload)payload;
		setAssumption(getAssumption()); //Sets the assumption to TRUE if it's null
		checkName();
	}

	public Formula getAssumption(){
		return getSMTAnnotations() != null? getSMTAnnotations().getAssumption(): Atom.TRUE;
	}
	
	public void setAssumption(Formula assumption){
		assumption = assumption != null? assumption: Atom.TRUE;
		getSMTAnnotations().setAssumption(assumption);
	}
	
	public boolean mergeEdgeWith(IEdge edge){
		SMTEdgeAnnotations				annotation	= getSMTAnnotations();
		HashMap<String, TermVariable> 	in_vars		= annotation.getInVars();
		HashMap<String, TermVariable> 	out_vars	= annotation.getOutVars();
		HashSet<TermVariable> 			vars		= annotation.getVars();
		SMTEdgeAnnotations				e_annotation= (SMTEdgeAnnotations)edge.getPayload().getAnnotations().get("SMT");
		HashMap<String, TermVariable> 	e_in_vars	= e_annotation.getInVars();
		HashMap<String, TermVariable> 	e_out_vars	= e_annotation.getOutVars();
		HashSet<TermVariable> 			e_vars		= e_annotation.getVars();
		
		HashMap<String, TermVariable>	n_in_vars	= new HashMap<String, TermVariable>();
		HashMap<String, TermVariable>	n_out_vars	= new HashMap<String, TermVariable>();
		HashSet<TermVariable>			n_vars		= new HashSet<TermVariable>();
		
		Formula assumption		= annotation.getAssumption();
		Formula e_assumption	= e_annotation.getAssumption();
		
		HashSet<String> all_out_varnames = new HashSet<String>();
		all_out_varnames.addAll(out_vars.keySet());
		all_out_varnames.addAll(e_out_vars.keySet());
		n_vars.addAll(vars);
		n_vars.addAll(e_vars);
		
		for (String varname : all_out_varnames) {
			TermVariable invar		= in_vars.get(varname);
			TermVariable outvar		= out_vars.get(varname);
			TermVariable e_invar	= e_in_vars.get(varname);
			TermVariable e_outvar	= e_out_vars.get(varname);
			if (outvar != null){
				if (outvar.equals(e_outvar)){
					/* if the two edges have an identical out going variable, that means that they inherited it from an predecessor node
					 * and therefore no mapping is needed
					 */
					if (invar != null) {
						if (e_invar != null) {
							if (invar != e_invar){
								e_assumption = m_Theory.let(e_invar, m_Theory.term(invar), e_assumption);
								//n_vars.remove(e_invar);
							}
						}
						n_in_vars.put(varname, invar);
					} else if (e_invar != null) {
						n_in_vars.put(varname, e_invar);
					}
					n_out_vars.put(varname, outvar);
					continue;
				}	
			}
			
			if (outvar != invar) {
				//variable is changed, check other edge.
				if (e_outvar == e_invar) {
					if (e_invar == null) {
						e_invar = m_Theory.createTermVariable(outvar.getName()+"in", outvar.getSort());
						n_vars.add(e_invar);
					} 
					Formula equality = m_Theory.equals(m_Theory.term(outvar), m_Theory.term(e_invar));
					e_assumption = m_Theory.and(e_assumption, equality);
					n_out_vars.put(varname, outvar);
				} else {
					if (e_vars.contains(outvar)){
						assumption = m_Theory.let(outvar, m_Theory.term(e_outvar), assumption); // Changed
						n_out_vars.put(varname, e_outvar);
						n_out_vars.remove(outvar);
					} else {
						e_assumption = m_Theory.let(e_outvar, m_Theory.term(outvar), e_assumption);
						n_out_vars.put(varname, outvar);
					}
					//n_vars.remove(e_outvar);
				}
				//n_out_vars.put(varname, outvar);
			} else if (e_outvar != e_invar) {
				//other variable is changed, this edge is unchanged.
				if (invar == null) {
					invar = m_Theory.createTermVariable(e_outvar.getName()+"in", e_outvar.getSort());
					n_vars.add(invar);
				}
				Formula equality = m_Theory.equals(m_Theory.term(e_outvar), m_Theory.term(invar));
				assumption = m_Theory.and(assumption, equality);
				n_out_vars.put(varname, e_outvar);
			} else if (outvar != null) {
				n_out_vars.put(varname, outvar);
			} else {
				assert (e_outvar != null);
				n_out_vars.put(varname, e_outvar);
			}

			if (invar != null) {
				if (e_invar != null) {
					if (invar != e_invar){
						e_assumption = m_Theory.let(e_invar, m_Theory.term(invar), e_assumption);
						//n_vars.remove(e_invar);
					}
				}
				n_in_vars.put(varname, invar);
			} else if (e_invar != null) {
				n_in_vars.put(varname, e_invar);
			}
		}
		
		Formula n_assumption = m_Theory.or(assumption, e_assumption);
		
		annotation.setInVars(n_in_vars);
		annotation.setOutVars(n_out_vars);
		annotation.setVars(n_vars);
		annotation.setAssumption(n_assumption);

		return ((CFGEdge)edge).deleteEdge();
	}
	
	public boolean deleteEdge(){
		boolean result = true;
		if (m_Source != null){
			result = m_Source.removeOutgoingEdge(this);
		}
		if (m_Target != null){
			result = result? m_Target.removeIncomingEdge(this): false;
		}
		return (result);
	}
	
	public boolean removeMultipleEdge(){
		for (IEdge edge: this.m_Source.getOutgoingEdges()){
			if ((edge.getTarget() == this.m_Target) && (edge != this)){
				return mergeEdgeWith(edge);
			}
		}
		return false;
	}
	
	public CFGEdge copyWithoutNodes(){
		return copyWithoutNodes(false);
	}
	
	//Clones edge, its payload and its CFGEdgeAnnotaions
	private CFGEdge copyWithoutNodes(boolean usingSSA){
		CFGEdge newCFGEdge = new CFGEdge(m_Theory, null, null, null);
		Payload newPayload = usingSSA? PayloadModifier.copyPayloadWithSSA(m_Payload): PayloadModifier.copyPayload(m_Payload);
		
		newCFGEdge.setPayload(newPayload);
		
		//newCFGEdge.m_SMTInterface = m_SMTInterface;
		
		newCFGEdge.checkName();
		return newCFGEdge;
	}
	
	public CFGEdge copyWithoutNodesUsingSSAMapping(HashMap<TermVariable, TermVariable> ssaMapping){
		getSMTAnnotations().setSSAMapping(ssaMapping);
		CFGEdge newEdge = copyWithoutNodes(true);
		return newEdge;
	}
	
	public int checkSatisfiability(){
		return checkEdge(false);
	}
	
	public int checkValidity(){
		return checkEdge(true);
	}
	
	public int checkEdge(boolean negateTarget){
//		s_SMTInterface = (SMTInterface)StalinServices.getInstance().getStoredObject("groundifier");
		s_SMTInterface = new SMTInterface(m_Theory, SMTInterface.SOLVER_SMTINTERPOL);
		assert(s_SMTInterface != null);
		
		CFGExplicitNode source = m_Source;
		CFGExplicitNode target = m_Target;
		
		if (m_Source == m_Target){
			target = m_Source.copyWithoutEdgesWithSSA();
		}
		
		Formula target_assertion	= target.getAssertion();
		Formula assumption			= getAssumption();
		Formula formula				= null;
		
		SMTEdgeAnnotations annotation = getSMTAnnotations();
		
		HashMap<String, TermVariable>	target_inVars	= target.getSMTAnnotations().getInVars();
		HashSet<TermVariable> 			target_Vars		= target.getSMTAnnotations().getVars();		
		
		HashSet<TermVariable>			vars				= new HashSet<TermVariable>();
		
		Formula							source_assertion	= Atom.TRUE;
		HashMap<String, TermVariable>	source_inVars		= new HashMap<String, TermVariable>();
		HashSet<TermVariable>			source_Vars			= new HashSet<TermVariable>();
		
		if (source.getSMTAnnotations() != null){
			source_assertion	= source.getAssertion();
			source_inVars		= source.getSMTAnnotations().getInVars();
			source_Vars			= source.getSMTAnnotations().getVars();
		}
		HashMap<String, TermVariable>	edge_inVars			= annotation.getInVars();
		HashMap<String, TermVariable>	edge_outVars		= annotation.getOutVars();
		HashSet<TermVariable>			edge_Vars			= annotation.getVars();
		
		HashMap<String, TermVariable>	oldVars				= SMTNodeAnnotations.s_OldVars;
		
		vars.addAll(source_Vars);
		vars.addAll(target_Vars);
		vars.addAll(edge_Vars);
		
		for (String tName: target_inVars.keySet()){
			if (edge_outVars.containsKey(tName)){
				TermVariable t_invar	= target_inVars.get(tName);
				TermVariable e_outvar	= edge_outVars.get(tName);
				target_assertion = m_Theory.let(t_invar, m_Theory.term(e_outvar), target_assertion);
				vars.remove(t_invar);
			} else if (source_inVars.containsKey(tName)){
				TermVariable t_invar	= target_inVars.get(tName);
				TermVariable s_invar	= source_inVars.get(tName);
				target_assertion = m_Theory.let(t_invar, m_Theory.term(s_invar), target_assertion);
				vars.remove(t_invar);
			}
		}
		Formula tmp_assertion = negateTarget? m_Theory.not(target_assertion): target_assertion;
		
		formula = m_Theory.and(assumption, tmp_assertion);
		
		for (String tvName: edge_inVars.keySet()){
			if (source_inVars.containsKey(tvName)){
				TermVariable e_invar	= edge_inVars.get(tvName);
				TermVariable s_invar	= source_inVars.get(tvName);
				if (e_invar != s_invar){
					formula = m_Theory.let(e_invar, m_Theory.term(s_invar), formula);
					vars.remove(e_invar);
				}
			}
		}
		
		formula = m_Theory.and(source_assertion, formula);
		if (oldVars != null){
			for (String tName: oldVars.keySet()){
				TermVariable s_invar = source_inVars.get(tName);
				if (s_invar != null){
					formula = m_Theory.let(s_invar, m_Theory.term(oldVars.get(tName)), formula);
					vars.remove(s_invar);
				}
			}
		}

		vars.addAll(oldVars.values());
		
		TermVariable[] varsArray = new TermVariable[vars.size()];
		vars.toArray(varsArray);
		
		if (varsArray.length > 0) {
			//formula = m_Theory.exists(varsArray, formula);
			for (TermVariable tv: varsArray) {
				Term constant = makeConstant(tv);
				formula = m_Theory.let(tv, constant, formula);
			}
		}

		if (negateTarget) {
			s_Logger.info("Checking validity of edge " + m_Payload.getName()+ ".");
		} else {
			s_Logger.info("Checking satisfiability of edge " + m_Payload.getName()+ ".");
		}
//		FormulaUnFleterer unFleterer = new FormulaUnFleterer(m_Theory);
//		formula = unFleterer.unflet(formula);
		int result;
		long startTime = System.currentTimeMillis();
		result = s_SMTInterface.checkSatisfiable(formula);
		//Formula[] interpolants = s_SMTInterface.computeInterpolants(new Formula[]{formula}, null);
		s_totalTime += (System.currentTimeMillis() - startTime);
		s_TheoremProverCalls++;
		
//		if (interpolants == null){
//			result = s_SMTInterface.checkSatisfiabilitySmtlib(formula);
//		} else {
//			result = SMTInterface.SMT_UNSAT;
//		}
//		try{
//			counter++;
//			//FormulaUnFleterer unFleterer = new FormulaUnFleterer(m_Theory);
//			//formula = unFleterer.unflet(formula);
//			String s_intpol_filename = "C:/Users/Evren/Desktop/SMT-Files/Edges/" + this.getSource().getPayload().getName() + "-" + this.getTarget().getPayload().getName() + "(" + counter + ")" + ".smt";
//			File intpol_filename = new File(s_intpol_filename);
//			s_Logger.info("Dumped to " + s_intpol_filename);
//			FileWriter intpol_file;
//			intpol_file = new FileWriter(intpol_filename);
//			PrintWriter intpol_pw = new PrintWriter(intpol_file);
//			this.m_Theory.dumpInterpolationBenchmark(intpol_pw, new Formula[]{formula});
//			intpol_file.close();
//		}
//		catch (IOException e){
//		}

		switch (result) {
		case 0:
			s_Logger.info("Result: UNSAT");
		break;
	    case 1:
	    	s_Logger.info("Result: SAT");
	    break;
	    case 2:
	    	s_Logger.info("Result: UNKNOWN");
	    break;
	    default:
	    	s_Logger.info("Result: ERROR");
	    break;
		}	
		return result;
	}
	
	private Term makeConstant(TermVariable tv){
		//new name for constant variable
		String constName = tv.getName() + "_const";
		//need a list of sorts of the input parameters of the function
		Sort[] dummy_Sorts = {};
		/*faking constant by creating function without input parameters and getting function symbol of newly created fake constant or
		 * of old fake constant that has already been created before*/
		FunctionSymbol fsym = m_Theory.getFunction(constName, dummy_Sorts);
		if (fsym == null)
			fsym = m_Theory.createFunction(constName, dummy_Sorts, tv.getSort());
		//need list of terms for input parameters of function in order to create term out of it
		Term[] dummyTerms = {};
		//making constant term and returning it
		Term const_Term = m_Theory.term(fsym, dummyTerms);
		return const_Term;
	}
	
	public String toString(){
		return getPayload().getName();
	}
}