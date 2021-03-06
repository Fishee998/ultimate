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
package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.states;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfgTransition;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.predicates.IDomainSpecificOperationProvider;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqFunction;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.transformula.vp.elements.EqNode;

/**
 * 
 * @author Alexander Nutz (nutz@informatik.uni-freiburg.de)
 *
 * @param <ACTION>
 */
public class EqOperationProvider<ACTION extends IIcfgTransition<IcfgLocation>> implements 
 		IDomainSpecificOperationProvider<
 			EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>, 
 			EqPredicate<ACTION>, 
 			EqTransitionRelation<ACTION>> {
	
	private final EqConstraintFactory<ACTION, EqNode, EqFunction> mEqConstraintFactory;
	
	public EqOperationProvider(EqConstraintFactory<ACTION, EqNode, EqFunction> eqConstraintFactory) {
		mEqConstraintFactory = eqConstraintFactory;
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> getConstraint(EqPredicate<ACTION> p) {
		return p.getEqConstraint();
	}

	@Override
	public boolean isConstaintUnsatisfiable(EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constraint) {
		return constraint.isBottom();
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> getConstaintFromTransitionRelation(
			EqTransitionRelation<ACTION> transRel) {
		return transRel.getEqConstraint();
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> renameVariables(
			Map<Term, Term> substitutionMapping,
			EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constraint) {
		return constraint.renameVariables(substitutionMapping);
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constructConjunction(
			List<EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>> conjuncts) {
		return mEqConstraintFactory.conjoinDisjunctiveConstraints(conjuncts);
	}
	
	


	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> projectExistentially(Set<TermVariable> varsToProjectAway,
			EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constraint) {
		return constraint.projectExistentially(varsToProjectAway);
	}


	@Override
	public boolean isConstaintValid(EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constraint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constructDisjunction(
			List<EqDisjunctiveConstraint<ACTION, EqNode, EqFunction>> disjuncts) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constructNegation(
			EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> operand) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> projectUniversally(Set<TermVariable> varsToProjectAway,
			EqDisjunctiveConstraint<ACTION, EqNode, EqFunction> constraint) {
		throw new UnsupportedOperationException();
	}


}
