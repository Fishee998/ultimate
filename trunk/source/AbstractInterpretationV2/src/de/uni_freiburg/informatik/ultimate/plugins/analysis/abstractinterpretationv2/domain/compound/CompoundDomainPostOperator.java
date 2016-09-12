/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.compound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplicationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.XnfConversionTechnique;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgStatementExtractor;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractState;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractStateBinaryOperator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences.AbsIntPrefInitializer;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.tool.AbstractInterpreter;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlockFactory;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootAnnot;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence.Origin;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.util.TransFormulaAdder;

/**
 * Post operator of the {@link CompoundDomain}.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CompoundDomainPostOperator implements IAbstractPostOperator<CompoundDomainState, CodeBlock, IBoogieVar> {

	private final boolean mCreateStateAssumptions;
	private final boolean mUseSmtSolverChecks;
	private final boolean mSimplifyAssumption;

	private final ILogger mLogger;
	private final Boogie2SMT mBoogie2Smt;
	private final SimplicationTechnique mSimplificationTechnique = SimplicationTechnique.SIMPLIFY_DDA;
	private final XnfConversionTechnique mXnfConversionTechnique = XnfConversionTechnique.BOTTOM_UP_WITH_LOCAL_SIMPLIFICATION;
	private final Script mScript;
	private final CodeBlockFactory mCodeBlockFactory;
	private final RcfgStatementExtractor mStatementExtractor;
	private final TransFormulaAdder mTransformulaBuilder;
	private final IUltimateServiceProvider mServices;

	/**
	 * Default constructor of the {@link CompoundDomain} post operator.
	 * 
	 * @param logger
	 *            The logger.
	 * @param rootAnnotation
	 *            The {@link RootAnnot} node from the {@link AbstractInterpreter}.
	 */
	protected CompoundDomainPostOperator(final IUltimateServiceProvider services, final RootAnnot rootAnnotation) {
		mLogger = services.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mBoogie2Smt = rootAnnotation.getBoogie2SMT();
		mScript = rootAnnotation.getScript();
		mCodeBlockFactory = rootAnnotation.getCodeBlockFactory();
		mStatementExtractor = new RcfgStatementExtractor();
		mTransformulaBuilder = new TransFormulaAdder(mBoogie2Smt, services);
		mServices = services;

		final IPreferenceProvider ups = mServices.getPreferenceProvider(Activator.PLUGIN_ID);
		mCreateStateAssumptions = ups.getBoolean(CompoundDomainPreferences.LABEL_CREATE_ASSUMPTIONS);
		mUseSmtSolverChecks = ups.getBoolean(CompoundDomainPreferences.LABEL_USE_SMT_SOLVER_FEASIBILITY);
		mSimplifyAssumption = ups.getBoolean(CompoundDomainPreferences.LABEL_SIMPLIFY_ASSUMPTIONS);
	}

	@Override
	public List<CompoundDomainState> apply(final CompoundDomainState oldstate, final CodeBlock transition) {
		final List<CompoundDomainState> returnStates = new ArrayList<>();

		final List<IAbstractState<?, CodeBlock, IBoogieVar>> states = oldstate.getAbstractStatesList();
		final List<IAbstractDomain> domains = oldstate.getDomainList();
		assert domains.size() == states.size();

		final List<CodeBlock> transitionList = createTransitionList(transition, states);
		assert transitionList.size() == domains.size();

		final List<IAbstractState<?, CodeBlock, IBoogieVar>> resultingStates = new ArrayList<>();

		for (int i = 0; i < domains.size(); i++) {
			final IAbstractDomain currentDomain = domains.get(i);
			final IAbstractState<?, CodeBlock, IBoogieVar> currentPreState = states.get(i);
			final CodeBlock currentTrans = transitionList.get(i);

			final List<IAbstractState> result =
					applyInternally(currentPreState, currentDomain.getPostOperator(), currentTrans);

			if (mLogger.isDebugEnabled()) {
				final StringBuilder sb = new StringBuilder(AbsIntPrefInitializer.DINDENT)
						.append(currentDomain.getClass().getSimpleName()).append(": ");
				result.stream().map(a -> a.toLogString())
						.forEach(a -> mLogger.debug(new StringBuilder(sb).append("post: ").append(a)));
			}

			if (result.isEmpty()) {
				return new ArrayList<>();
			}

			IAbstractState state = result.get(0);
			for (int j = 1; j < result.size(); j++) {
				state = applyMergeInternally(state, result.get(j), currentDomain.getMergeOperator());
			}

			if (state.isBottom()) {
				return new ArrayList<>();
			}

			resultingStates.add(state);
		}

		assert resultingStates.size() == domains.size();
		returnStates.add(new CompoundDomainState(mServices, domains, resultingStates));

		if (mUseSmtSolverChecks) {
			return returnStates.stream().filter(state -> checkSat(state)).collect(Collectors.toList());
		}

		return returnStates;
	}

	/**
	 * Checks satisfiability of a {@link CompoundDomainState}.
	 * 
	 * @param state
	 *            The state to check for satisfiability.
	 * @return <code>true</code> if and only if the term generated from {@link CompoundDomainState#getTerm(Script,
	 *         Boogie2SMT))} is satisfiable, <code>false</code> otherwise.
	 */
	private boolean checkSat(final CompoundDomainState state) {
		final Term stateTerm = state.getTerm(mScript, mBoogie2Smt);
		if (mLogger.isDebugEnabled()) {
			mLogger.debug(new StringBuilder().append("Checking state term for satisfiability: ").append(stateTerm)
					.toString());
		}
		final LBool result = SmtUtils.checkSatTerm(mScript, stateTerm);
		if (mLogger.isDebugEnabled()) {
			mLogger.debug(new StringBuilder().append("Result of satisfiability check is: ").append(result).toString());
		}
		if (result == LBool.UNSAT) {
			return false;
		}

		return true;
	}

	/**
	 * Computes the transition {@link CodeBlock} for each domain. If the option is enabled that each state should be
	 * assumed before each post, a new transition {@link CodeBlock} will be created which contains an assume statement
	 * at the top corresponding to the formula representation for each state.
	 * 
	 * @param transition
	 * @param states
	 * @return
	 */
	private List<CodeBlock> createTransitionList(final CodeBlock transition,
			final List<IAbstractState<?, CodeBlock, IBoogieVar>> states) {

		final List<CodeBlock> returnList = new ArrayList<>();

		if (mCreateStateAssumptions) {
			// If there is only one internal compound state, keep the transitions as they are and do nothing else.
			if (states.size() == 1) {
				returnList.add(transition);
			} else {
				for (int i = 0; i < states.size(); i++) {
					returnList.add(createBlockWithoutState(states, i, transition));
				}
			}
		} else {
			for (int i = 0; i < states.size(); i++) {
				returnList.add(transition);
			}
		}

		return returnList;
	}

	/**
	 * Creates a new {@link CodeBlock} that includes an assume statement of all states (except the i-th state) at the
	 * top and the given {@link CodeBlock} as rest.
	 * 
	 * @param states
	 * @param index
	 * @param transition
	 * @return
	 */
	private CodeBlock createBlockWithoutState(final List<IAbstractState<?, CodeBlock, IBoogieVar>> states,
			final int index, final CodeBlock transition) {

		assert !states.isEmpty();

		Term assumeTerm = null;
		for (int i = 0; i < states.size(); i++) {
			if (i == index) {
				continue;
			}
			final IAbstractState<?, CodeBlock, IBoogieVar> state = states.get(i);

			final Term stateTerm = state.getTerm(mScript, mBoogie2Smt);
			assumeTerm = assumeTerm == null ? stateTerm : mScript.term("and", assumeTerm, stateTerm);
		}

		if (mSimplifyAssumption) {
			assumeTerm = SmtUtils.simplify(mBoogie2Smt.getManagedScript(), assumeTerm, mServices, mSimplificationTechnique);
		}

		final Expression assumeExpression = mBoogie2Smt.getTerm2Expression().translate(assumeTerm);
		final AssumeStatement assumeStmt = new AssumeStatement(assumeExpression.getLocation(), assumeExpression);
		final List<Statement> stmtLists = new ArrayList<>();
		stmtLists.add(assumeStmt);
		stmtLists.addAll(mStatementExtractor.process(transition));
		final CodeBlock result =
				mCodeBlockFactory.constructStatementSequence(null, null, stmtLists, Origin.IMPLEMENTATION);
		mTransformulaBuilder.addTransitionFormulas(result, transition.getPrecedingProcedure(), mXnfConversionTechnique, mSimplificationTechnique);

		if (mLogger.isDebugEnabled()) {
			mLogger.debug(AbsIntPrefInitializer.INDENT + " Created new transition for domain " + index);
			mLogger.debug(AbsIntPrefInitializer.DINDENT + " term:       " + assumeTerm.toStringDirect());
			mLogger.debug(AbsIntPrefInitializer.DINDENT + " transition: " + result);
		}

		return result;
	}

	@Override
	public List<CompoundDomainState> apply(final CompoundDomainState stateBeforeLeaving,
			final CompoundDomainState stateAfterLeaving, final CodeBlock transition) {
		final List<CompoundDomainState> returnStates = new ArrayList<>();

		final List<IAbstractState<?, CodeBlock, IBoogieVar>> beforeStates = stateBeforeLeaving.getAbstractStatesList();
		final List<IAbstractState<?, CodeBlock, IBoogieVar>> afterStates = stateAfterLeaving.getAbstractStatesList();
		final List<IAbstractDomain> domainsBefore = stateBeforeLeaving.getDomainList();
		final List<IAbstractDomain> domainsAfter = stateAfterLeaving.getDomainList();

		assert domainsBefore.size() == domainsAfter.size();
		assert beforeStates.size() == afterStates.size();
		assert domainsBefore.size() == beforeStates.size();

		final List<IAbstractState<?, CodeBlock, IBoogieVar>> resultingStates = new ArrayList<>();

		for (int i = 0; i < domainsBefore.size(); i++) {
			final List<IAbstractState> result = applyInternally(beforeStates.get(i), afterStates.get(i),
					domainsBefore.get(i).getPostOperator(), transition);

			if (result.isEmpty()) {
				return new ArrayList<>();
			}

			IAbstractState state = result.get(0);
			for (int j = 1; j < result.size(); j++) {
				state = applyMergeInternally(state, result.get(j), domainsBefore.get(i).getMergeOperator());
			}

			if (state.isBottom()) {
				return new ArrayList<>();
			}
			resultingStates.add(state);
		}

		assert resultingStates.size() == domainsBefore.size();
		returnStates.add(new CompoundDomainState(mServices, domainsBefore, resultingStates));

		return returnStates;
	}

	private static List<IAbstractState> applyInternally(final IAbstractState<?, CodeBlock, IBoogieVar> currentState,
			final IAbstractPostOperator postOperator, final CodeBlock transition) {
		return postOperator.apply(currentState, transition);
	}

	private List<IAbstractState> applyInternally(final IAbstractState<?, CodeBlock, IBoogieVar> first,
			final IAbstractState<?, CodeBlock, IBoogieVar> second, final IAbstractPostOperator postOperator,
			final CodeBlock transition) {
		return postOperator.apply(first, second, transition);
	}

	private static <T extends IAbstractState, M extends IAbstractStateBinaryOperator<T>> T applyMergeInternally(final T first,
			final T second, final M mergeOperator) {
		return mergeOperator.apply(first, second);
	}

}
