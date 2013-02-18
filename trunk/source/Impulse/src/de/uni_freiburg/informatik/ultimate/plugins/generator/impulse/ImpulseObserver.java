package de.uni_freiburg.informatik.ultimate.plugins.generator.impulse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import de.uni_freiburg.informatik.ultimate.access.IUnmanagedObserver;
import de.uni_freiburg.informatik.ultimate.access.WalkerOptions;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.model.IEdge;
import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.INode;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.errortrace.ErrorTrace;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RCFGEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.preferences.TAPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.preferences.PreferenceValues.Solver;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.Predicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.TraceChecker;
import de.uni_freiburg.informatik.ultimate.result.CounterExampleResult;
import de.uni_freiburg.informatik.ultimate.result.IResult;
import de.uni_freiburg.informatik.ultimate.result.PositiveResult;
import de.uni_freiburg.informatik.ultimate.result.UnprovableResult;

import org.apache.log4j.Logger;

/**
 * Auto-Generated Stub for the plug-in's Observer
 */
public class ImpulseObserver implements IUnmanagedObserver {

	public enum Result { CORRECT, TIMEOUT , MAXEDITERATIONS , UNKNOWN , INCORRECT }

	private static Logger s_Logger = UltimateServices.getInstance().getLogger(Activator.s_PLUGIN_ID);
	private RootNode m_originalRoot;
	private SmtManager m_smtManager;
	private TAPreferences m_taPrefs;
	private RootNode m_graphRoot;
	
	private Predicate m_truePredicate;
	private Predicate m_falsePredicate;
	private ProgramPoint m_pELProgramPoint;

	private HashMap<AnnotatedProgramPoint, AnnotatedProgramPoint> m_nodeToCopy;
	private HashMap<AnnotatedProgramPoint, AnnotatedProgramPoint> m_nodeToCopyCurrent;
	private AnnotatedProgramPoint m_currentProcRoot;

	GraphWriter m_gw;
	int m_gwCounter = 0;

	int m_pathChecks = 0;
	private ErrorTrace m_errorTrace;

	@Override
	public boolean process(IElement root) {
		m_originalRoot = (RootNode) root;
		RootAnnot rootAnnot = m_originalRoot.getRootAnnot();
		m_taPrefs = rootAnnot.getTaPrefs();
		m_smtManager = new SmtManager(rootAnnot.getBoogie2Smt(), 
				Solver.SMTInterpol, rootAnnot.getGlobalVars(), false, "");

		m_truePredicate = m_smtManager.newTruePredicate(null);
		m_falsePredicate = m_smtManager.newFalsePredicate(null);
		m_pELProgramPoint = new ProgramPoint("PEL", "all", true, null, null, m_smtManager.getScript());
		
		m_gw  = new GraphWriter("/home/alexander/impulseGraphs",
				true, true, true, true, m_smtManager.getScript());

		RCFG2AnnotatedRCFG r2ar = new RCFG2AnnotatedRCFG(m_smtManager);
		m_graphRoot = r2ar.convert(m_originalRoot);

		Result overallResult = null;

		for (RCFGEdge procEdge : m_graphRoot.getOutgoingEdges()) {
			Result procResult = processProcedure(procEdge);

			if (overallResult == null || 
					(overallResult == Result.CORRECT && procResult != Result.CORRECT))
				overallResult = procResult;
		}

		s_Logger.info("-----------------");
		s_Logger.info(overallResult);
		s_Logger.info("-----------------");

		s_Logger.info("PC#: " + m_smtManager.getInterpolQueries());
		s_Logger.info("TIME#: " + m_smtManager.getInterpolQuriesTime());
		s_Logger.info("ManipulationTIME#: " + m_smtManager.getTraceCheckTime());
		s_Logger.info("EC#: " + m_smtManager.getNontrivialSatQueries());
		s_Logger.info("TIME#: " + m_smtManager.getSatQuriesTime());
		s_Logger.info("ManipulationTIME#: "	+ m_smtManager.getCodeBlockCheckTime());

		if (overallResult == Result.CORRECT) {
			PositiveResult<CodeBlock> result = new PositiveResult<CodeBlock>(
					null,
					Activator.s_PLUGIN_NAME,
					UltimateServices.getInstance().getTranslatorSequence(),
					this.m_graphRoot.getPayload().getLocation());
			result.setShortDescription("Program is safe!");
			reportResult(result);
		} else if (overallResult == Result.INCORRECT) {
			this.reportResult(new CounterExampleResult<CodeBlock>(null,
					Activator.s_PLUGIN_NAME,
					UltimateServices.getInstance().getTranslatorSequence(),
					null, null));//m_errorTrace.getCounterExampleResult());
		} else {
			this.reportResult(new UnprovableResult<CodeBlock>(null,
					Activator.s_PLUGIN_NAME,
					UltimateServices.getInstance().getTranslatorSequence(),
					null));
		}
		
		return false;
	}

	private Result processProcedure(RCFGEdge procEdge) {
		ErrorPathBuilder epb = new ErrorPathBuilder();
		m_nodeToCopy = new HashMap<AnnotatedProgramPoint, AnnotatedProgramPoint>();
		m_nodeToCopyCurrent = new HashMap<AnnotatedProgramPoint, AnnotatedProgramPoint>();
		m_currentProcRoot = (AnnotatedProgramPoint) procEdge.getTarget();

		m_gw.writeGraphAsImage(m_currentProcRoot, "graph_" + (++m_gwCounter) + "_procproc");

		while (true) {
			s_Logger.debug("did " + m_pathChecks + " iterations, starting new");
			Pair<AnnotatedProgramPoint[], NestedWord<CodeBlock>> errorNWP = 
					epb.getErrorPathAsNestedWord(procEdge);


			if (errorNWP == null) {
				m_gw.writeGraphAsImage(m_currentProcRoot, "graph_" + (++m_gwCounter) + "_provenCorrect");
				return Result.CORRECT;
			} else {
				s_Logger.debug("found an error path");
				boolean isPEL = errorNWP.getFirst()[errorNWP.getFirst().length - 1].
						isPseudoErrorLocation();
				AnnotatedProgramPoint pEL = isPEL ? 
						errorNWP.getFirst()[errorNWP.getFirst().length - 1] :
							null;
						if (isPEL)
							s_Logger.debug("it is a Pseudo Error Location");

						m_gw.writeGraphAsImage(m_currentProcRoot,
								"graph_" + (++m_gwCounter) + "_ep", errorNWP.getFirst());

						TraceChecker traceChecker = new TraceChecker(m_smtManager, 
								m_originalRoot.getRootAnnot().getModifiedVars(), 
								m_originalRoot.getRootAnnot().getEntryNodes(),
								dumpInitialize());
						LBool isSafe = traceChecker.checkTrace(m_truePredicate, 
								isPEL ? pEL.getPredicate() : m_falsePredicate, 
										errorNWP.getSecond());
						m_pathChecks++;

						if(isSafe == LBool.UNSAT) {
							Predicate[] interpolants = traceChecker.getInterpolants(
									new TraceChecker.AllIntegers());

							copyNodes(errorNWP, interpolants);
							m_gw.writeGraphAsImage(m_currentProcRoot,
									"graph_" + (++m_gwCounter) + "_cp", m_nodeToCopyCurrent, m_nodeToCopy);

							doDefaultRedirecting(errorNWP);
							m_gw.writeGraphAsImage(m_currentProcRoot,
									"graph_" + (++m_gwCounter) + "_ddr", m_nodeToCopyCurrent, m_nodeToCopy);

							redirect(errorNWP);
							m_gw.writeGraphAsImage(m_currentProcRoot,
									"graph_" + (++m_gwCounter) + "_rd", m_nodeToCopyCurrent, m_nodeToCopy);

							//					m_gw.writeGraphAsImage(m_currentProcRoot, "graph_" + (++m_gwCounter) + "_cpddrrd");
						} else {
							if (isPEL) {
								AnnotatedProgramPoint lastApp = errorNWP.getFirst()[errorNWP.getFirst().length - 1];
								AnnotatedProgramPoint secondLastApp = errorNWP.getFirst()[errorNWP.getFirst().length - 2];
								secondLastApp.removeOutgoingNode(lastApp);
								lastApp.removeIncomingNode(secondLastApp);
								traceChecker.forgetTrace();
							} else {
//								makeErrorTraceFromNW(errorNWP.getSecond());
								return Result.INCORRECT;
							}
						}
			}
		}
	}

	private void makeErrorTraceFromNW(NestedWord<CodeBlock> errorNW) {
		ArrayList<IElement> errorPathAL = new ArrayList<IElement>();
		Term[] errorPathFormulas = new Term[errorNW.length()];
		for (int i = 0; i < errorNW.length(); i++) { 
			errorPathAL.add(errorNW.getSymbolAt(i));
			errorPathFormulas[i] = errorNW.getSymbolAt(i).getTransitionFormula().getFormula();
		}
		m_errorTrace = new ErrorTrace(m_smtManager.getScript(), errorPathAL, errorPathFormulas);
	}
	
	private void copyNodes(
			Pair<AnnotatedProgramPoint[], NestedWord<CodeBlock>> errorNWP,
			Predicate[] interpolants) {
		m_nodeToCopyCurrent.clear();

		AnnotatedProgramPoint[] appPath = errorNWP.getFirst();

		for (int i = 0; i < appPath.length - 1; i++) {
			Predicate newPredicate = 
					m_smtManager.and(appPath[i].getPredicate(), interpolants[i]); //FIXME: indices may be incorrect

			AnnotatedProgramPoint copy = new AnnotatedProgramPoint(newPredicate, appPath[i].getProgramPoint());

			for (INode outNode : appPath[i].getOutgoingNodes()) {
				AnnotatedProgramPoint outApp = (AnnotatedProgramPoint) outNode;
				copy.addOutgoingNode(outApp, appPath[i].getOutgoingCodeBlockOf(outApp));
			}
			m_nodeToCopyCurrent.put(appPath[i], copy);
		}
		m_nodeToCopy.putAll(m_nodeToCopyCurrent);
	}

	private void doDefaultRedirecting(
			Pair<AnnotatedProgramPoint[], NestedWord<CodeBlock>> errorNWP) {
		AnnotatedProgramPoint[] appPath = errorNWP.getFirst();

		//always redirect "procRoot -> appPath[0]" towards "procRoot -> nodeToCopy(appPath[0)"
		redirectEdge(m_currentProcRoot, appPath[0], m_nodeToCopyCurrent.get(appPath[0]));

		for (int i = 0; i < appPath.length - 2; i++) 
			redirectEdge(m_nodeToCopyCurrent.get(appPath[i]), 
					appPath[i+1], 
					m_nodeToCopyCurrent.get(appPath[i+1]));

		//always delete the edge from the last copy towards the error location
		m_nodeToCopyCurrent.get(appPath[appPath.length - 2]).
		removeOutgoingNode(appPath[appPath.length - 1]);
		appPath[appPath.length - 1].removeIncomingNode(m_nodeToCopyCurrent.get(appPath[appPath.length - 2]));

	}

	private void redirect(Pair<AnnotatedProgramPoint[],
			NestedWord<CodeBlock>> errorNWP) {
		AnnotatedProgramPoint[] appPath = errorNWP.getFirst();

		for (int i = 0; i < appPath.length - 1; i++) {
			AnnotatedProgramPoint copy = m_nodeToCopyCurrent.get(appPath[i]);
			for (INode outNode : copy.getOutgoingNodes()) {
				AnnotatedProgramPoint outApp = (AnnotatedProgramPoint) outNode;

				AnnotatedProgramPoint copyOfOutApp = m_nodeToCopy.get(outApp); 

				if (copyOfOutApp != null) {
					CodeBlock statement = copy.getOutgoingCodeBlockOf(outApp);
					if (statement instanceof Summary)
						continue;

					LBool isInductive;
					if (statement instanceof Call) 
						isInductive = m_smtManager.isInductiveCall(
								copy.getPredicate(), (Call) statement, copyOfOutApp.getPredicate());
					else if (statement instanceof Return) 
						isInductive = m_smtManager.isInductiveReturn(copy.getPredicate(), 
								m_nodeToCopy.get(appPath[errorNWP.getSecond().getCallPosition(i+1)]).getPredicate(), //TODO: hm...
								(Return) statement, copyOfOutApp.getPredicate());
					else 
						isInductive = m_smtManager.isInductive(
								copy.getPredicate(), statement, copyOfOutApp.getPredicate());

					if (isInductive == LBool.UNSAT)
						redirectEdge(copy, outApp, copyOfOutApp);
					else
						appendNewPseudoErrorLocation(copy, 
								copy.getOutgoingCodeBlockOf(outApp), 
								copyOfOutApp.getPredicate());
				}
			}
		}
	}

	private void appendNewPseudoErrorLocation(AnnotatedProgramPoint node,
			CodeBlock codeBlock, Predicate predicate) {
//		Predicate newPredicate = m_smtManager.not(predicate); //will be negated implicitly by checkTrace
		if (m_smtManager.isInductive(node.getPredicate(), codeBlock, predicate) == LBool.UNSAT)
			return;

		AnnotatedProgramPoint pEL = 
				new AnnotatedProgramPoint(predicate, m_pELProgramPoint, true);
		node.addOutgoingNode(pEL, codeBlock);
		pEL.addIncomingNode(node, codeBlock);
	}

	/*
	 * assuming there are no parallel edges
	 */
	private void redirectEdge(AnnotatedProgramPoint source, AnnotatedProgramPoint oldTarget,
			AnnotatedProgramPoint newTarget) {
		source.addOutgoingNode(newTarget, source.getOutgoingCodeBlockOf(oldTarget));
		source.removeOutgoingNode(oldTarget);
		oldTarget.removeIncomingNode(source);
		newTarget.addIncomingNode(source, source.getOutgoingCodeBlockOf(newTarget));
	}

	private PrintWriter dumpInitialize() {
		File file = 
				new File(m_taPrefs.dumpPath() + "/" + ".txt");
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(file);
			return new PrintWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	RootNode getRoot() {
		return m_graphRoot;
	}

	private void reportResult(IResult res) {
		UltimateServices.getInstance().reportResult(Activator.s_PLUGIN_ID, res);
	}
	
	// --------------------------------------------------
	// -------------- interface stuff -------------------
	// --------------------------------------------------

	@Override
	public void finish() {

	}

	@Override
	public WalkerOptions getWalkerOptions() {
		return null;
	}

	@Override
	public void init() {

	}

	@Override
	public boolean performedChanges() {
		return false;
	}

}
