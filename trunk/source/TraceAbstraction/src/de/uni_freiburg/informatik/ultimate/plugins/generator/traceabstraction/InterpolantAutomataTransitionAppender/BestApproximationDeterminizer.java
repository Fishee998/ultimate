package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.InterpolantAutomataTransitionAppender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.DeterminizedState;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IStateDeterminizer;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.preferences.TAPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;

public class BestApproximationDeterminizer 
		implements IStateDeterminizer<CodeBlock, IPredicate> {
	
	SmtManager m_SmtManager;
	TAPreferences m_TaPreferences;
	StateFactory<IPredicate> m_StateFactory;
	INestedWordAutomaton<CodeBlock, IPredicate> m_Nwa;
	public int m_AnswerInternalSolver = 0;
	public int m_AnswerInternalAutomaton = 0;
	public int m_AnswerInternalCache = 0;
	public int m_AnswerCallSolver = 0;
	public int m_AnswerCallAutomaton = 0;
	public int m_AnswerCallCache = 0;
	public int m_AnswerReturnSolver = 0;
	public int m_AnswerReturnAutomaton = 0;
	public int m_AnswerReturnCache = 0;


	Map<IPredicate,Map<CodeBlock,Set<IPredicate>>>	m_InductiveSuccsCache = 
		new HashMap<IPredicate,Map<CodeBlock,Set<IPredicate>>>();
	
	Map<IPredicate,Map<CodeBlock,Set<IPredicate>>>	m_InductiveCallSuccsCache = 
		new HashMap<IPredicate,Map<CodeBlock,Set<IPredicate>>>();
	
	Map<IPredicate,Map<IPredicate,Map<CodeBlock,Set<IPredicate>>>> m_InductiveReturnSuccsCache = 
		new HashMap<IPredicate,Map<IPredicate,Map<CodeBlock,Set<IPredicate>>>>();
	

	public BestApproximationDeterminizer(SmtManager mSmtManager,
			TAPreferences taPreferences,
			INestedWordAutomaton<CodeBlock, IPredicate> mNwa) {
		super();
		m_SmtManager = mSmtManager;
		m_TaPreferences = taPreferences;
		m_StateFactory = mNwa.getStateFactory();
		m_Nwa = mNwa;
	}
	
	@Override
	public DeterminizedState<CodeBlock, IPredicate> initialState() {
		DeterminizedState<CodeBlock, IPredicate> detState = 
			new DeterminizedState<CodeBlock, IPredicate>(m_Nwa);
		//FIXME add all at once
		for (IPredicate initialState : m_Nwa.getInitialStates()) {
			detState.addPair(m_Nwa.getEmptyStackState(), initialState, m_Nwa);
		}
		return detState;
	}

	@Override
	public DeterminizedState<CodeBlock, IPredicate>  internalSuccessor(
			DeterminizedState<CodeBlock, IPredicate>  detState,
			CodeBlock symbol) {
		
		DeterminizedState<CodeBlock, IPredicate>  succDetState = 
			new DeterminizedState<CodeBlock, IPredicate> (m_Nwa);
		for (IPredicate  downState : detState.getDownStates()) {
			for (IPredicate  upState : detState.getUpStates(downState)) {
				for (IPredicate  upSucc : getInternalSuccs(upState,symbol)) {
					succDetState.addPair(downState, upSucc, m_Nwa);
				}
			}
		}
		if (m_TaPreferences.computeHoareAnnotation()) {
			assert(m_SmtManager.isInductive(detState.getContent(m_StateFactory), 
						symbol, 
						succDetState.getContent(m_StateFactory)) == Script.LBool.UNSAT ||
					m_SmtManager.isInductive(detState.getContent(m_StateFactory), 
						symbol, 
						succDetState.getContent(m_StateFactory)) == Script.LBool.UNKNOWN);
		}
		return succDetState;	
	}
	
	@Override
	public DeterminizedState<CodeBlock, IPredicate>  callSuccessor(
			DeterminizedState<CodeBlock, IPredicate>  detState, 
			CodeBlock symbol) {
		
		DeterminizedState<CodeBlock, IPredicate>  succDetState = 
				new DeterminizedState<CodeBlock, IPredicate> (m_Nwa);
		for (IPredicate  downState : 
												detState.getDownStates()) {
			for (IPredicate  upState : 
										detState.getUpStates(downState)) {
				for (IPredicate  upSucc : 
									getCallSuccs(upState,(Call) symbol)) {
					succDetState.addPair(upState, upSucc, m_Nwa);
				}
			}
		}
		if (m_TaPreferences.computeHoareAnnotation()) {
			assert(m_SmtManager.isInductiveCall(detState.getContent(m_StateFactory), 
						(Call) symbol, 
						succDetState.getContent(m_StateFactory)) == Script.LBool.UNSAT ||
					m_SmtManager.isInductiveCall(detState.getContent(m_StateFactory), 
						(Call) symbol, 
						succDetState.getContent(m_StateFactory)) == Script.LBool.UNKNOWN);
		}
		return succDetState;	
	}

	@Override
	public DeterminizedState<CodeBlock, IPredicate>  returnSuccessor(
			DeterminizedState<CodeBlock, IPredicate>  detState,
			DeterminizedState<CodeBlock, IPredicate>  detLinPred,
			CodeBlock symbol) {
		
		DeterminizedState<CodeBlock, IPredicate>  succDetState = 
				new DeterminizedState<CodeBlock, IPredicate> (m_Nwa);
		
		for (IPredicate  downLinPred : 
												detLinPred.getDownStates()) {
			for (IPredicate  upLinPred : 
									detLinPred.getUpStates(downLinPred)) {
				Set<IPredicate > upStates = 
										detState.getUpStates(upLinPred);
				if (upStates == null) continue;
				for (IPredicate  upState : upStates) {
					Return returnSymbol = (Return) symbol;
					for (IPredicate  upSucc : 
							getReturnSuccs(upState,upLinPred, returnSymbol)) {
						succDetState.addPair(downLinPred, upSucc, m_Nwa);
					}
				}
			}
		}
		
		if (m_TaPreferences.computeHoareAnnotation()) {
			assert(m_SmtManager.isInductiveReturn(detState.getContent(m_StateFactory),
					detLinPred.getContent(m_StateFactory),
					(Return) symbol, 
					succDetState.getContent(m_StateFactory)) == Script.LBool.UNSAT ||
					m_SmtManager.isInductiveReturn(detState.getContent(m_StateFactory),
						detLinPred.getContent(m_StateFactory),
						(Return) symbol, 
						succDetState.getContent(m_StateFactory)) == Script.LBool.UNKNOWN);
		}

		return succDetState;	
	}
	
	
	
	
	
	/**
	 * Return all states succ such that (state, symbol, succ) is inductive.
	 */
	private Set<IPredicate> getInternalSuccs(
			IPredicate state,
			CodeBlock symbol) {
		Set<IPredicate> succs = getInternalSuccsCache(state, symbol);
		if (succs == null) {
			succs = new HashSet<IPredicate>();
			for (IPredicate succCandidate : m_Nwa.getStates()) {
				if (isInductiveInternalSuccessor(state, symbol, succCandidate))
					succs.add(succCandidate);
			}
			putInternalSuccsCache(state, symbol, succs);
		}
		else {
			m_AnswerInternalCache++;
		}
		return succs;
	}

	/**
	 * Store in the cache that succs is the set of all states succ such that
	 * (state, symbol, succ) is inductive.
	 */
	private void putInternalSuccsCache(
			IPredicate state,
			CodeBlock symbol,
			Set<IPredicate> succs) {
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			m_InductiveSuccsCache.get(state);
		if (symbol2succs == null) {
			symbol2succs = new HashMap<CodeBlock, Set<IPredicate>>();
			m_InductiveSuccsCache.put(state, symbol2succs);
		}
		symbol2succs.put(symbol, succs);
	}

	/**
	 * Let succs be the set of all states succ such that (state, symbol, succ)
	 * is inductive. If the cache knows this result succs is returned, otherwise
	 * null is returned.
	 */
	private Set<IPredicate> getInternalSuccsCache(
			IPredicate state,
			CodeBlock symbol) {
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			m_InductiveSuccsCache.get(state);
		if (symbol2succs == null) {
			return null;
		}
		Set<IPredicate> succs = symbol2succs.get(symbol);
		if (succs == null) {
			return null;
		}
		return succs;
	}
	
	
	
	
	
	/**
	 * Return all states succ such that (state, symbol, succ) is inductive.
	 */
	private Set<IPredicate> getCallSuccs(
			IPredicate state,
			Call symbol) {
		Set<IPredicate> succs = getCallSuccsCache(state, symbol);
		if (succs == null) {
			succs = new HashSet<IPredicate>();
			for (IPredicate succCandidate : m_Nwa.getStates()) {
				if (inductiveCallSuccessor(state, symbol, succCandidate))
					succs.add(succCandidate);
			}
			putCallSuccsCache(state, symbol, succs);
		}
		else {
			m_AnswerCallCache++;
		}
		return succs;
	}

	
	/**
	 * Store in the cache that succs is the set of all states succ such that
	 * (state, symbol, succ) is inductive.
	 */
	private void putCallSuccsCache(
			IPredicate state,
			CodeBlock symbol,
			Set<IPredicate> succs) {
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			m_InductiveCallSuccsCache.get(state);
		if (symbol2succs == null) {
			symbol2succs = new HashMap<CodeBlock, Set<IPredicate>>();
			m_InductiveCallSuccsCache.put(state, symbol2succs);
		}
		symbol2succs.put(symbol, succs);
	}

	/**
	 * Let succs be the set of all states succ such that (state, symbol, succ)
	 * is inductive. If the cache knows this result succs is returned, otherwise
	 * null is returned.
	 */
	private Set<IPredicate> getCallSuccsCache(
			IPredicate state,
			CodeBlock symbol) {
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			m_InductiveCallSuccsCache.get(state);
		if (symbol2succs == null) {
			return null;
		}
		Set<IPredicate> succs = symbol2succs.get(symbol);
		if (succs == null) {
			return null;
		}
		return succs;
	}
	
	/**
	 * Returns true iff (state,symbol,succ) is inductive. Fist the interpolant
	 * automaton is queried for a yes-answer, afterwards the solver is
	 * queried for a yes/no/unknown-answer. We querying the interpolant
	 * automaton for two reasons:
	 * <ul>
	 * <li> a query to the solver is expensive
	 * <li> if we do not compute interpolants for every location we have 
	 * unknown-labeled states for which the solver can not give a yes/no-answer.
	 * </ul> 
	 */
	private boolean isInductiveInternalSuccessor(
			IPredicate  state,
			CodeBlock symbol,
			IPredicate  succ) {
		
		if (m_Nwa.succInternal(state, symbol).contains(succ)) {
			m_AnswerInternalAutomaton++;
			return true;
		}
		IPredicate presentPs = state;
		IPredicate succPs = succ;
		LBool sat = m_SmtManager.isInductive(presentPs, symbol, succPs);
		m_AnswerInternalSolver++;
		if (sat == Script.LBool.UNSAT) {
			m_Nwa.addInternalTransition(state, symbol, succ);
			return true;
		}
		else {
			return false;	
		}
	}
	
	/**
	 * Returns true iff (state,symbol,succ) is inductive. Fist the interpolant
	 * automaton is queried for a yes-answer, afterwards the solver is
	 * queried for a yes/no/unknown-answer. We querying the interpolant
	 * automaton for two reasons:
	 * <ul>
	 * <li> a query to the solver is expensive
	 * <li> if we do not compute interpolants for every location we have 
	 * unknown-labeled states for which the solver can not give a yes/no-answer.
	 * </ul> 
	 */
	private boolean inductiveCallSuccessor(
			IPredicate  state,
			Call symbol,
			IPredicate  succ) {
		if (m_Nwa.succCall(state,symbol).contains(succ)) {
			m_AnswerCallAutomaton++;
			return true;
		}
		IPredicate presentPs = state;
		IPredicate succPs = succ;
		LBool sat = m_SmtManager.isInductiveCall(presentPs, symbol, succPs);
		m_AnswerCallSolver++;
		if (sat == Script.LBool.UNSAT) {
			return true;
		}
		return false;
	}
	
	
	
	
	/**
	 * Return all states succ such that (state, linPred, symbol, succ) is
	 * inductive.
	 */
	private Set<IPredicate> getReturnSuccs(
			IPredicate state,
			IPredicate linPred, 
			Return symbol) {
		Set<IPredicate> succs = getReturnSuccsCache(state, linPred, symbol);
		if (succs == null) {
			succs = new HashSet<IPredicate>();
			for (IPredicate succCandidate : m_Nwa.getStates()) {
				if (isInductiveReturnSuccessor(state, linPred, symbol, succCandidate))
					succs.add(succCandidate);
			}
			putReturnSuccsCache(state, linPred, symbol, succs);
		}
		else {
			m_AnswerReturnCache++;
		}
		return succs;
	}
	
	/**
	 * Store in the cache that succs is the set of all states succ such that
	 * (state, linPred, symbol, succ) is inductive.
	 */
	private void putReturnSuccsCache(
			IPredicate state,
			IPredicate linPred, 
			CodeBlock symbol,
			Set<IPredicate> succs) {
		Map<IPredicate, Map<CodeBlock, Set<IPredicate>>> linPred2symbol2succs = 
			m_InductiveReturnSuccsCache.get(state);
		if (linPred2symbol2succs == null) {
			linPred2symbol2succs = 
				new HashMap<IPredicate, Map<CodeBlock, Set<IPredicate>>>();
			m_InductiveReturnSuccsCache.put(state, linPred2symbol2succs);	
		}
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			linPred2symbol2succs.get(linPred);
		if (symbol2succs == null) {
			symbol2succs = new HashMap<CodeBlock, Set<IPredicate>>();
			linPred2symbol2succs.put(linPred, symbol2succs);
		}
		symbol2succs.put(symbol, succs);
	}

	/**
	 * Let succs be the set of all states succ such that 
	 * (state, linPred, symbol, succ) is inductive. If the cache knows this 
	 * result succs is returned, otherwise
	 * null is returned.
	 */
	private Set<IPredicate> getReturnSuccsCache(
			IPredicate state,
			IPredicate linPred, 
			CodeBlock symbol) {
		Map<IPredicate, Map<CodeBlock, Set<IPredicate>>> linPred2symbol2succs = 
			m_InductiveReturnSuccsCache.get(state);
		if (linPred2symbol2succs == null) {
			return null;
		}
		Map<CodeBlock, Set<IPredicate>> symbol2succs = 
			linPred2symbol2succs.get(linPred);
		if (symbol2succs == null) {
			return null;
		}
		Set<IPredicate> succs = symbol2succs.get(symbol);
		if (succs == null) {
			return null;
		}
		return succs;
	}
	
	
	/**
	 * Returns true iff (state,callerState,symbol,succ) is inductive.
	 * Fist the interpolant automaton is queried for a yes-answer, afterwards 
	 * the solver is queried for a yes/no/unknown-answer. We querying the 
	 * interpolant automaton for two reasons:
	 * <ul>
	 * <li> a query to the solver is expensive
	 * <li> if we do not compute interpolants for every location we have 
	 * unknown-labeled states for which the solver can not give a yes/no-answer.
	 * </ul> 
	 */
	private boolean isInductiveReturnSuccessor(
			IPredicate  state,
			IPredicate  callerState,
			Return symbol,
			IPredicate  succ) {
		if (m_Nwa.succReturn(state,callerState, symbol).contains(succ)) {
			m_AnswerReturnAutomaton++;
			return true;
		}
		IPredicate presentPs = state;
		IPredicate callerPs = callerState;
		IPredicate succPs = succ;
		LBool sat = 
			m_SmtManager.isInductiveReturn(presentPs, callerPs, symbol, succPs);
		m_AnswerReturnSolver++;
		if (sat == Script.LBool.UNSAT) {
			return true;
		}
		return false;
	}

	@Override
	public int getMaxDegreeOfNondeterminism() {
		// TODO Auto-generated method stub
		return 0;
	}


	


	








}
