/*
 * Copyright (C) 2017 Alexander Nutz (nutz@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
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
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.AnnotatedTerm;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.LetTerm;
import de.uni_freiburg.informatik.ultimate.logic.NonRecursive;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.TransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.normalForms.Nnf;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.normalForms.Nnf.QuantifierHandling;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqFunction;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqNode;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqNodeAndFunctionFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.states.EqConstraint;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.states.EqConstraintFactory;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.states.EqDisjunctiveConstraint;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.states.EqTransitionRelation;

/**
 * 
 * @author Alexander Nutz (nutz@informatik.uni-freiburg.de)
 *
 * @param <ACTION>
 */
public class ConvertTransformulaToEqTransitionRelation<ACTION extends IIcfgTransition<IcfgLocation>> 
		extends NonRecursive {

	private final TransFormula mTf;
	private EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> mResultConstraint;
	
	private final EqConstraintFactory<ACTION, EqNode, EqFunction> mEqConstraintFactory;
	private final EqNodeAndFunctionFactory mEqNodeAndFunctionFactory;
	private final ManagedScript mMgdScript;
	private final IUltimateServiceProvider mServices;
	
	private final VPDomainPreanalysis mPreAnalysis;

	/**
	 * stores intermediate results of the "recursion"
	 */
//	private final ArrayDeque<EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>> mResultStack = new ArrayDeque<>();
	private final ArrayDeque<EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>> mResultStack = new ArrayDeque<>();
	
	public ConvertTransformulaToEqTransitionRelation(TransFormula tf, 
			EqConstraintFactory<ACTION, EqNode, EqFunction> eqConstraintFactory, 
			EqNodeAndFunctionFactory eqNodeAndFunctionFactory, VPDomainPreanalysis preAnalysis) {
		mTf = tf;
		mEqConstraintFactory = eqConstraintFactory;
		mEqNodeAndFunctionFactory = eqNodeAndFunctionFactory;
		
		mPreAnalysis = preAnalysis;
		mMgdScript = preAnalysis.getManagedScript();
		mServices = preAnalysis.getServices();
		
		computeResult();
	}
	
	private void computeResult() {
		final Term transFormulaInNnf = 
				new Nnf(mMgdScript, mServices, QuantifierHandling.CRASH).transform(mTf.getFormula());
		run(new ConvertTfToEqDisjConsWalker(transFormulaInNnf));
		assert mResultStack.size() == 1;
		mResultConstraint = mResultStack.pop();
	}

	public EqTransitionRelation<ACTION> getResult() {
		assert mResultConstraint != null;
		return new EqTransitionRelation<>(mResultConstraint, mTf);
	}
	
	class ConvertTfToEqDisjConsWalker extends TermWalker {

		public ConvertTfToEqDisjConsWalker(Term term) {
			super(term);
		}

		@Override
		public void walk(NonRecursive walker, ConstantTerm term) {
			if ("Bool".equals(term.getSort().getName())) {
				assert false : "TODO: implement";
			} else {
				assert false : "we should have caught this before, right?";
			}
		}

		@Override
		public void walk(NonRecursive walker, AnnotatedTerm term) {
			walker.enqueueWalker(new ConvertTfToEqDisjConsWalker(term.getSubterm()));
		}

		@Override
		public void walk(NonRecursive walker, ApplicationTerm term) {
			if ("=".equals(term.getFunction().getName())) {
				handleXquality(term.getParameters()[0], term.getParameters()[1], true);
			} else if ("distinct".equals(term.getFunction().getName())) {
				handleXquality(term.getParameters()[0], term.getParameters()[1], false);
			} else if ("not".equals(term.getFunction().getName()) 
							&& term.getParameters()[0] instanceof ApplicationTerm
							&& "=".equals(((ApplicationTerm) term.getParameters()[0]).getFunction().getName())) {
				final ApplicationTerm innerEqualsTerm = (ApplicationTerm) term.getParameters()[0];
				handleXquality(innerEqualsTerm.getParameters()[0], innerEqualsTerm.getParameters()[1], false);
			} else if ("not".equals(term.getFunction().getName()) 
							&& term.getParameters()[0] instanceof TermVariable) {
				handleBooleanVariable((TermVariable) term.getParameters()[0], false);
			} else if ("or".equals(term.getFunction().getName())) {
				walker.enqueueWalker(new MakeDisjunctionWalker(term.getParameters().length));

				for (Term param : term.getParameters()) {
					walker.enqueueWalker(new ConvertTfToEqDisjConsWalker(param));
				}
				
			} else if ("and".equals(term.getFunction().getName())) {
				walker.enqueueWalker(new MakeConjunctionWalker(term.getParameters().length));

				for (Term param : term.getParameters()) {
					walker.enqueueWalker(new ConvertTfToEqDisjConsWalker(param));
				}
				

			} else if ("false".equals(term.getFunction().getName())) {
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(
						Collections.emptySet()));
			} else if ("true".equals(term.getFunction().getName())) {
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(
						Collections.singleton(mEqConstraintFactory.getEmptyConstraint())));
			} else {
				// we don't recognize this function symbol -- overapproximating its effects by "top"
				// TODO: perhaps we could make some checks here if it is trivially bottom or something like that..
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(
						Collections.singleton(mEqConstraintFactory.getEmptyConstraint())));
			}
			
		}

		private void handleBooleanVariable(TermVariable termVariable, boolean polarity) {
			assert "Bool".equals(termVariable.getSort().getName());
			final EqConstraint<ACTION, EqNode, EqFunction> emptyConstraint = 
					mEqConstraintFactory.getEmptyConstraint();
			final EqNode tvNode = mEqNodeAndFunctionFactory.getOrConstructEqNode(termVariable);
			if (polarity) {
				mMgdScript.lock(this);
				final EqNode trueNode = mEqNodeAndFunctionFactory.getOrConstructEqNode(mMgdScript.term(this, "true"));
				mMgdScript.unlock(this);
				final EqConstraint<ACTION, EqNode, EqFunction> tvEqualsTrue = 
						mEqConstraintFactory.addEqualityFlat(tvNode, trueNode, emptyConstraint);
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(
						Collections.singleton(tvEqualsTrue)));
			} else {
				mMgdScript.lock(this);
				final EqNode falseNode = mEqNodeAndFunctionFactory.getOrConstructEqNode(mMgdScript.term(this, "false"));
				mMgdScript.unlock(this);
				final EqConstraint<ACTION, EqNode, EqFunction> tvEqualsTrue = 
						mEqConstraintFactory.addEqualityFlat(tvNode, falseNode, emptyConstraint);
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(
						Collections.singleton(tvEqualsTrue)));
			}
		}

		private void handleXquality(Term arg1, Term arg2, boolean polarity) {
			
			final EqConstraint<ACTION, EqNode, EqFunction> emptyConstraint = 
					mEqConstraintFactory.getEmptyConstraint();
			final EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> emptyDisjunctiveConstraint = 
					mEqConstraintFactory.getDisjunctiveConstraint(Collections.singleton(emptyConstraint));
			
			if (arg1.getSort().isArraySort()) {
				// we have an array equality
				
				if (!isFunctionTracked(arg1) || !isFunctionTracked(arg2)) {
					// we don't track both sides of the equation --> return an empty constraint
					mResultStack.push(emptyDisjunctiveConstraint);
					return;
				}
				
				final EqFunction func1 = mEqNodeAndFunctionFactory.getOrConstructEqFunction(arg1);
				final EqFunction func2 = mEqNodeAndFunctionFactory.getOrConstructEqFunction(arg2);
				
			
				final EqConstraint<ACTION, EqNode, EqFunction> newConstraint;
				if (polarity) {
					newConstraint = 
							mEqConstraintFactory.addFunctionEqualityFlat(func1, func2, emptyConstraint);
				} else {
					newConstraint = 
							mEqConstraintFactory.addFunctionDisequalityFlat(func1, func2, emptyConstraint);
				}
				
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(Collections.singleton(newConstraint)));
				return;
			} else {
				// we have an "normal", element equality
				
				if (!isElementTracked(arg1) || !isElementTracked(arg2)) {
					// we don't track both sides of the equation --> return an empty constraint
					mResultStack.push(emptyDisjunctiveConstraint);
					return;
				}
				
				final EqNode node1 = mEqNodeAndFunctionFactory.getOrConstructEqNode(arg1);
				final EqNode node2 = mEqNodeAndFunctionFactory.getOrConstructEqNode(arg2);
				
				final EqConstraint<ACTION, EqNode, EqFunction> newConstraint;
				if (polarity) {
					newConstraint = 
							mEqConstraintFactory.addEqualityFlat(node1, node2, emptyConstraint);
				} else {
					newConstraint = 
							mEqConstraintFactory.addDisequalityFlat(node1, node2, emptyConstraint);
				}
				mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(Collections.singleton(newConstraint)));
				return;
			}
		}

		private boolean isElementTracked(Term term) {
			return mPreAnalysis.isElementTracked(term, mTf);
		}

		private boolean isFunctionTracked(Term term) {
			return mPreAnalysis.isArrayTracked(term, mTf.getInVars(), mTf.getOutVars());
		}

		@Override
		public void walk(NonRecursive walker, LetTerm term) {
			assert false : "TODO unlet first (or implement let handling..)";
		}

		@Override
		public void walk(NonRecursive walker, QuantifiedFormula term) {
			throw new UnsupportedOperationException("quantifiers in Transformulas are currently not supported in the"
					+ " equality domain");
		}

		@Override
		public void walk(NonRecursive walker, TermVariable term) {
			if ("Bool".equals(term.getSort().getName())) {
				handleBooleanVariable(term, true);
				return;
			}
			throw new AssertionError("we should have caught this before, right?");
		}
	}
	
	class MakeDisjunctionWalker implements Walker {
		/**
		 * arity of the disjunction (i.e. how many elements to pop from result stack
		 */
		private int mArity;

		public MakeDisjunctionWalker(int arity) {
			mArity = arity;
		}

		@Override
		public void walk(NonRecursive engine) {
			final Set<EqConstraint<ACTION, EqNode, EqFunction>> allConjunctiveConstraints = new HashSet<>();
			for (int i = 0; i < mArity; i++) {
				allConjunctiveConstraints.addAll(mResultStack.pop().getConstraints());
			}
			mResultStack.push(mEqConstraintFactory.getDisjunctiveConstraint(allConjunctiveConstraints));
		}
	}
	
	class MakeConjunctionWalker implements Walker {
		/**
		 * arity of the disjunction (i.e. how many elements to pop from result stack
		 */
		private int mArity;

		public MakeConjunctionWalker(int arity) {
			mArity = arity;
		}

		@Override
		public void walk(NonRecursive engine) {
			final ArrayList<EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>> conjuncts = new ArrayList<>();
			for (int i = 0; i < mArity; i++) {
				conjuncts.add(mResultStack.pop());
			}
			mResultStack.push(mEqConstraintFactory.conjoinDisjunctiveConstraints(conjuncts));
		}
	}
}
