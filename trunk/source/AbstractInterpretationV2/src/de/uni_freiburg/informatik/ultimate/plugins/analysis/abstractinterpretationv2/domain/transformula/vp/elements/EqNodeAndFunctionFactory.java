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
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.CommuhashNormalForm;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.Substitution;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.VPDomainPreanalysis;

/**
 * 
 * @author Alexander Nutz (nutz@informatik.uni-freiburg.de)
 *
 */
public class EqNodeAndFunctionFactory {

	ManagedScript mMgdScript;

	private final Map<Term, EqNode> mTermToEqNode = new HashMap<>();

	private final Map<Term, EqFunction> mTermToEqFunction = new HashMap<>();

	private final VPDomainPreanalysis mPreAnalysis;

	public EqNodeAndFunctionFactory(VPDomainPreanalysis preAnalysis, ManagedScript script) {
		mPreAnalysis = preAnalysis;
		mMgdScript = script;
	}

	public ManagedScript getScript() {
		return mMgdScript;
	}

	public EqNode getOrConstructEqNode(Term term) {
		if (term instanceof ApplicationTerm && ((ApplicationTerm) term).getParameters().length > 0) {
			if ("select".equals(((ApplicationTerm) term).getFunction().getName())) {
				return getOrConstructEqFunctionNode((ApplicationTerm) term);
			} else if (((ApplicationTerm) term).getFunction().isIntern()) {
				return getOrConstructNonAtomicBaseNode(term);
			} else {
				throw new UnsupportedOperationException();
			}
		} else if (term instanceof ApplicationTerm && ((ApplicationTerm) term).getParameters().length == 0) {
			return getOrConstructEqAtomicBaseNode(term);
		} else if (term instanceof TermVariable) {
			return getOrConstructEqAtomicBaseNode(term);
		} else if (term instanceof ConstantTerm) {
			return getOrConstructEqAtomicBaseNode(term);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private EqNode getOrConstructNonAtomicBaseNode(Term term) {
		mMgdScript.lock(this);
		final Term normalizedTerm = new CommuhashNormalForm(
				mPreAnalysis.getServices(), mMgdScript.getScript()).transform(term);
		mMgdScript.unlock(this);
		EqNode result = mTermToEqNode.get(normalizedTerm);
		if (result == null) {
			result = new EqNonAtomicBaseNode(normalizedTerm, this);
			mTermToEqNode.put(normalizedTerm, result);
		}
		assert result instanceof EqNonAtomicBaseNode;
		return result;		
	}

	private EqNode getOrConstructEqFunctionNode(ApplicationTerm selectTerm) {
		EqNode result = mTermToEqNode.get(selectTerm);

		if (result == null) {
			final MultiDimensionalSelect mds = new MultiDimensionalSelect(selectTerm);

			final EqFunction function = getOrConstructEqFunction(mds.getArray());

			final List<EqNode> args = new ArrayList<>();
			for (Term index : mds.getIndex()) {
				args.add(getOrConstructEqNode(index));
			}

			result = new EqFunctionNode(function, args, selectTerm, this);
			mTermToEqNode.put(selectTerm, result);
		}
		assert result instanceof EqFunctionNode;
		return result;
	}

	private EqNode getOrConstructEqAtomicBaseNode(Term term) {
		EqNode result = mTermToEqNode.get(term);
		if (result == null) {
			result = new EqAtomicBaseNode(term, this);
			mTermToEqNode.put(term, result);
		}
		assert result instanceof EqAtomicBaseNode;
		return result;

	}

	public EqFunction getOrConstructEqFunction(Term term) {
		if (term instanceof ApplicationTerm && ((ApplicationTerm) term).getParameters().length > 0) {
			if ("store".equals(((ApplicationTerm) term).getFunction().getName())) {
				return getOrConstructEqStoreFunction((ApplicationTerm) term);
			} else {
				throw new UnsupportedOperationException();
			}
		} else if (term instanceof ApplicationTerm && ((ApplicationTerm) term).getParameters().length == 0) {
			return getOrConstructAtomicEqFunction(term);
		} else if (term instanceof TermVariable) {
			return getOrConstructAtomicEqFunction(term);
		} else if (term instanceof ConstantTerm) {
			return getOrConstructAtomicEqFunction(term);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private EqFunction getOrConstructAtomicEqFunction(Term term) {
		assert !(term instanceof ApplicationTerm) || !"store".equals(((ApplicationTerm) term).getFunction().getName());
		EqFunction result = mTermToEqFunction.get(term);
		if (result == null) {
			result = new EqFunction(term, this);
			mTermToEqFunction.put(term, result);
		}
		assert !(result instanceof EqStoreFunction);
		return result;
	}

	private EqFunction getOrConstructEqStoreFunction(ApplicationTerm storeTerm) {
		assert "store".equals(storeTerm.getFunction().getName());
		EqFunction result = mTermToEqFunction.get(storeTerm);
		if (result == null) {
			final MultiDimensionalStore mds = new MultiDimensionalStore(storeTerm);

			final EqFunction function = getOrConstructEqFunction(mds.getArray());

			final List<EqNode> indices = new ArrayList<>();
			for (Term index : mds.getIndex()) {
				indices.add(getOrConstructEqNode(index));
			}

			final EqNode value = getOrConstructEqNode(mds.getValue());

			result = new EqStoreFunction(function, indices, value, storeTerm, this);
			mTermToEqFunction.put(storeTerm, result);
		}
		return result;
	}

	public EqFunction constructRenamedEqFunction(EqFunction eqFunction, Map<Term, Term> substitutionMapping) {
		final Term substitutedTerm = new Substitution(mMgdScript, substitutionMapping).transform(eqFunction.getTerm());
		return getOrConstructEqFunction(substitutedTerm);
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public EqNode getExistingEqNode(Term term) {
		final Term normalizedTerm;
		if (term instanceof ApplicationTerm && ((ApplicationTerm) term).getParameters().length > 0) {
			mMgdScript.lock(this);
			normalizedTerm = new CommuhashNormalForm(
					mPreAnalysis.getServices(), mMgdScript.getScript()).transform(term);
			mMgdScript.unlock(this);
		} else {
			normalizedTerm = term;
		}
		return mTermToEqNode.get(normalizedTerm);
	}

	/**
	 * 
	 * @param term
	 * @return
	 */
	public EqFunction getExistingEqFunction(Term term) {
		return mTermToEqFunction.get(term);
	}

}
