package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction;

import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.IRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IPredicate;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.PredicateFactory;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.predicates.SmtManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singleTraceCheck.PredicateUnifier;

public class AbstractInterpretationPathProgramGenerator
        implements IAbstractInterpretationAutomatonGenerator<CodeBlock, IPredicate> {

	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final NestedWordAutomaton<CodeBlock, IPredicate> mResult;
	private final IRun<CodeBlock, IPredicate> mCurrentCounterExample;
	private final PredicateFactory mPredicateFactory;
	private final Boogie2SMT mBoogie2Smt;

	public AbstractInterpretationPathProgramGenerator(final IUltimateServiceProvider services,
	        final INestedWordAutomaton<CodeBlock, IPredicate> oldAbstraction,
	        final PredicateUnifier predUnifier, final SmtManager smtManager,
	        final IRun<CodeBlock, IPredicate> currentCounterexample) {
		mServices = services;
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mCurrentCounterExample = currentCounterexample;
		mBoogie2Smt = smtManager.getBoogie2Smt();
		mPredicateFactory = new PredicateFactory(services, mBoogie2Smt);

		mResult = getPathProgramAutomaton(oldAbstraction, predUnifier);
	}

	@Override
	public NestedWordAutomaton<CodeBlock, IPredicate> getResult() {
		return mResult;
	}

	private NestedWordAutomaton<CodeBlock, IPredicate> getPathProgramAutomaton(
	        final INestedWordAutomaton<CodeBlock, IPredicate> oldAbstraction, final PredicateUnifier predicateUnifier) {

		mLogger.info("Creating automaton from AI predicates.");

		final NestedWordAutomaton<CodeBlock, IPredicate> result = new NestedWordAutomaton<>(
		        new AutomataLibraryServices(mServices), oldAbstraction.getInternalAlphabet(),
		        oldAbstraction.getCallAlphabet(), oldAbstraction.getReturnAlphabet(), oldAbstraction.getStateFactory());
		final Set<IPredicate> predicates = new HashSet<>();
		IPredicate currentState = predicateUnifier.getTruePredicate();
		result.addState(true, false, currentState);
		predicates.add(predicateUnifier.getTruePredicate());

		final IPredicate falsePred = predicateUnifier.getFalsePredicate();

		// TODO: is it correct to go to length - 1 here? I assume that the last state is the error location and we don't
		// want anything to to with that as we are introducing this location in the end.
		for (int cexI = 0; cexI < mCurrentCounterExample.getLength() - 1; cexI++) {
			IPredicate prevState = currentState;

			// Add state
			currentState = mPredicateFactory.newPredicate(mBoogie2Smt.getScript().term("true"));
			result.addState(false, false, currentState);

			// Add transition
			final CodeBlock currentSymbol = mCurrentCounterExample.getSymbol(cexI);
			result.addInternalTransition(prevState, currentSymbol, currentState);
		}

		if (result.getFinalStates().isEmpty() || !predicates.contains(falsePred)) {
			result.addState(false, true, predicateUnifier.getFalsePredicate());
		}

		return result;
	}

}