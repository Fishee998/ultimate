/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE TraceAbstraction plug-in.
 * 
 * The ULTIMATE TraceAbstraction plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE TraceAbstraction plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE TraceAbstraction plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE TraceAbstraction plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE TraceAbstraction plug-in grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.singletracecheck;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.automata.nestedword.NestedWord;
import de.uni_freiburg.informatik.ultimate.core.model.translation.IProgramExecution.ProgramState;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieConst;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.BoogieVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.ICfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.variables.IProgramVar;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.util.RcfgProgramExecution;

public class RcfgProgramExecutionBuilder {

	private final ModifiableGlobalVariableManager mModifiableGlobalVariableManager;
	private final NestedWord<CodeBlock> mTrace;
	private final Map<IProgramVar, Map<Integer, Term>> mvar2pos2value;
	private final RelevantVariables mRelevantVariables;
	private RcfgProgramExecution mRcfgProgramExecution;
	private final Map<TermVariable, Boolean>[] mBranchEncoders;
	private final ICfgSymbolTable mSymbolTable;

	public RcfgProgramExecutionBuilder(final ModifiableGlobalVariableManager modifiableGlobalVariableManager,
			final NestedWord<CodeBlock> trace, final RelevantVariables relevantVariables, final ICfgSymbolTable symbolTable) {
		super();
		mModifiableGlobalVariableManager = modifiableGlobalVariableManager;
		mTrace = trace;
		mvar2pos2value = new HashMap<IProgramVar, Map<Integer, Term>>();
		mRelevantVariables = relevantVariables;
		mBranchEncoders = new Map[mTrace.length()];
		mRcfgProgramExecution = null;
		mSymbolTable = symbolTable;
	}

	public RcfgProgramExecution getRcfgProgramExecution() {
		if (mRcfgProgramExecution == null) {
			mRcfgProgramExecution = computeRcfgProgramExecution();
		}
		return mRcfgProgramExecution;
	}

	private boolean isReAssigned(final IProgramVar bv, final int position) {
		boolean result;
		if (mTrace.isInternalPosition(position) || mTrace.isReturnPosition(position)) {
			final UnmodifiableTransFormula tf = mTrace.getSymbolAt(position).getTransitionFormula();
			result = tf.getAssignedVars().contains(bv);
		} else if (mTrace.isCallPosition(position)) {
			final Call call = (Call) mTrace.getSymbolAt(position);
			final String callee = call.getCallStatement().getMethodName();
			if (bv.isGlobal()) {
				final Set<IProgramVar> modGlobals = mModifiableGlobalVariableManager.getGlobalVarsAssignment(callee)
						.getOutVars().keySet();
				final Set<IProgramVar> modOldGlobals = mModifiableGlobalVariableManager.getOldVarsAssignment(callee)
						.getOutVars().keySet();
				result = modGlobals.contains(bv) || modOldGlobals.contains(bv);
			} else {
				// TransFormula locVarAssign =
				// mTrace.getSymbolAt(position).getTransitionFormula();
				// result = locVarAssign.getAssignedVars().contains(bv);
				result = (callee.equals(bv.getProcedure()));
			}
		} else {
			throw new AssertionError();
		}
		return result;
	}

	void addValueAtVarAssignmentPosition(final IProgramVar bv, final int index, final Term value) {
		assert index >= -1;
		assert index == -1 || isReAssigned(bv, index) : "oldVar in procedure where it is not modified?";
		Map<Integer, Term> pos2value = mvar2pos2value.get(bv);
		if (pos2value == null) {
			pos2value = new HashMap<Integer, Term>();
			mvar2pos2value.put(bv, pos2value);
		}
		assert !pos2value.containsKey(index);
		pos2value.put(index, value);
	}

	public void setBranchEncoders(final int i, final Map<TermVariable, Boolean> beMapping) {
		mBranchEncoders[i] = beMapping;
	}

	private int indexWhereVarWasAssignedTheLastTime(final IProgramVar bv, final int pos) {
		assert pos >= -1;
		if (pos == -1) {
			return -1;
		}
		if (isReAssigned(bv, pos)) {
			return pos;
		}
		if (mTrace.isInternalPosition(pos) || mTrace.isCallPosition(pos)) {
			return indexWhereVarWasAssignedTheLastTime(bv, pos - 1);
		} else if (mTrace.isReturnPosition(pos)) {
			if (bv.isGlobal() && !bv.isOldvar()) {
				return indexWhereVarWasAssignedTheLastTime(bv, pos - 1);
			} else {
				final int callPos = mTrace.getCallPosition(pos);
				return indexWhereVarWasAssignedTheLastTime(bv, callPos - 1);
			}
		} else {
			throw new AssertionError();
		}

	}

	public Map<IProgramVar, Term> varValAtPos(final int position) {
		final Map<IProgramVar, Term> result = new HashMap<IProgramVar, Term>();
		final Set<IProgramVar> vars = mRelevantVariables.getForwardRelevantVariables()[position + 1];
		for (final IProgramVar bv : vars) {
			if (SmtUtils.isSortForWhichWeCanGetValues(bv.getTermVariable().getSort())) {
				final int assignPos = indexWhereVarWasAssignedTheLastTime(bv, position);
				final Term value = mvar2pos2value.get(bv).get(assignPos);
				assert value != null;
				result.put(bv, value);
			}
		}
		return result;
	}
	

	private RcfgProgramExecution computeRcfgProgramExecution() {
		final Map<Integer, ProgramState<Term>> partialProgramStateMapping = 
				new HashMap<Integer, ProgramState<Term>>();
		for (int i = 0; i < mTrace.length(); i++) {
			final Map<IProgramVar, Term> varValAtPos = varValAtPos(i);
			final Map<Term, Collection<Term>> variable2Values = 
					new HashMap<Term, Collection<Term>>();
			for (final Entry<IProgramVar, Term> entry : varValAtPos.entrySet()) {
				if (!(entry.getKey() instanceof BoogieVar) && !(entry.getKey() instanceof BoogieConst)) {
					throw new IllegalArgumentException("in backtranslation we need BoogieVars");
				}
				final IProgramVar bv = entry.getKey();
				variable2Values.put(bv.getTermVariable(), Collections.singleton(entry.getValue()));
			}
			final ProgramState<Term> pps = new ProgramState<Term>(variable2Values);
			partialProgramStateMapping.put(i, pps);
		}
		return new RcfgProgramExecution(mTrace.asList(), partialProgramStateMapping, mBranchEncoders);
	}
	

}