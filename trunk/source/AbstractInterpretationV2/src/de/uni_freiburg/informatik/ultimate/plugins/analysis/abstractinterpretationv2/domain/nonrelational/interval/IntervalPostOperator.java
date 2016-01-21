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

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.boogie.symboltable.BoogieSymbolTable;
import de.uni_freiburg.informatik.ultimate.boogie.type.ArrayType;
import de.uni_freiburg.informatik.ultimate.boogie.type.PrimitiveType;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.boogie.IBoogieVar;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Procedure;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableLHS;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.algorithm.rcfg.RcfgStatementExtractor;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.model.IAbstractPostOperator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;

/**
 * The post operator of the interval domain.
 * 
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 */
public class IntervalPostOperator implements IAbstractPostOperator<IntervalDomainState, CodeBlock, IBoogieVar> {

	private final Logger mLogger;
	private final RcfgStatementExtractor mStatementExtractor;
	private final IntervalDomainStatementProcessor mStatementProcessor;
	private final BoogieSymbolTable mSymbolTable;

	public IntervalPostOperator(final Logger logger, final BoogieSymbolTable symbolTable) {
		mLogger = logger;
		mStatementExtractor = new RcfgStatementExtractor();
		mStatementProcessor = new IntervalDomainStatementProcessor(mLogger, symbolTable);
		mSymbolTable = symbolTable;
	}

	@Override
	public List<IntervalDomainState> apply(final IntervalDomainState oldstate, final CodeBlock transition) {
		assert oldstate != null;
		assert !oldstate.isBottom();
		assert transition != null;

		List<IntervalDomainState> currentStates = new ArrayList<>();
		currentStates.add(oldstate);
		final List<Statement> statements = mStatementExtractor.process(transition);

		for (final Statement stmt : statements) {
			final List<IntervalDomainState> afterProcessStates = new ArrayList<>();
			for (final IntervalDomainState currentState : currentStates) {
				final List<IntervalDomainState> processed = mStatementProcessor.process(currentState, stmt);
				assert processed.size() > 0;
				afterProcessStates.addAll(processed);
			}
			currentStates = afterProcessStates;
		}

		return currentStates;
	}

	@Override
	public IntervalDomainState apply(final IntervalDomainState stateBeforeLeaving,
	        final IntervalDomainState stateAfterLeaving, final CodeBlock transition) {
		assert transition instanceof Call || transition instanceof Return;

		if (transition instanceof Call) {
			final Call call = (Call) transition;
			CallStatement callStatement = (CallStatement) call.getCallStatement();
			final Expression[] args = callStatement.getArguments();

			Map<Integer, String> paramNames = getArgumentTemporaries(args.length, stateBeforeLeaving);

			final List<LeftHandSide> idents = new ArrayList<>();

			Map<String, IBoogieVar> paramVariables = new HashMap<>();
			for (int i = 0; i < args.length; i++) {
				final String name = paramNames.get(i);
				final IBoogieVar boogieVar = createTemporaryIBoogieVar(name, args[i].getType());
				paramVariables.put(name, boogieVar);

				final ILocation loc = callStatement.getLocation();
				idents.add(new VariableLHS(loc, name));
			}

			final AssignmentStatement assign = new AssignmentStatement(callStatement.getLocation(),
			        idents.toArray(new LeftHandSide[idents.size()]), args);

			final IntervalDomainState interimState = stateBeforeLeaving.addVariables(paramVariables);

			final List<IntervalDomainState> result = mStatementProcessor.process(interimState, assign);
			if (result.size() != 1) {
				throw new UnsupportedOperationException(
				        "The assingment operation resulted in 0 or more than one state.");
			}

			final IntervalDomainState resultState = result.get(0);

			final Procedure procedure = getProcedure(callStatement.getMethodName());
			assert procedure != null;

			final VarList[] inParams = procedure.getInParams();
			final List<String> paramIdentifiers = new ArrayList<>();

			for (final VarList varlist : inParams) {
				for (String var : varlist.getIdentifiers()) {
					paramIdentifiers.add(var);
				}
			}

			if (args.length != paramIdentifiers.size()) {
				throw new UnsupportedOperationException(
				        "The number of the expressions in the call statement arguments does not correspond to the length of the number of arguments in the symbol table.");
			}

			IntervalDomainState returnState = stateAfterLeaving.copy();

			for (int i = 0; i < paramIdentifiers.size(); i++) {
				final String tempName = paramNames.get(i);
				final String realName = paramIdentifiers.get(i);

				final IBoogieVar type = interimState.getVariableType(tempName);
				if (type.getIType() instanceof PrimitiveType) {
					final PrimitiveType primitiveType = (PrimitiveType) type.getIType();

					if (primitiveType.getTypeCode() == PrimitiveType.BOOL) {
						returnState = returnState.setBooleanValue(realName, resultState.getBooleanValue(tempName));
					} else {
						returnState = returnState.setValue(realName, resultState.getValue(tempName));
					}
				} else if (type.getIType() instanceof ArrayType) {
					// TODO:
					// We treat Arrays as normal variables for the time being.
					returnState = returnState.setValue(realName, resultState.getValue(tempName));
				} else {
					mLogger.warn("The IBoogieVar type " + type.getIType()
					        + " cannot be handled. Assuming normal variable type.");
					returnState = returnState.setValue(realName, resultState.getValue(tempName));
				}

			}

			return returnState;
		} else if (transition instanceof Return) {
			final Return ret = (Return) transition;
			final CallStatement correspondingCall = ret.getCallStatement();

			final List<IntervalDomainValue> vals = getOutParamValues(correspondingCall.getMethodName(),
			        stateBeforeLeaving);
			final VariableLHS[] lhs = correspondingCall.getLhs();

			if (vals.size() != lhs.length) {
				throw new UnsupportedOperationException("The expected number of return variables (" + lhs.length
				        + ") is different from the function's number of return variables (" + vals.size() + ").");
			}

			final List<String> updateVarNames = new ArrayList<>();
			for (final VariableLHS varLhs : lhs) {
				updateVarNames.add(varLhs.getIdentifier());
			}

			final IntervalDomainState returnState = stateAfterLeaving.setValues(
			        updateVarNames.toArray(new String[updateVarNames.size()]),
			        vals.toArray(new IntervalDomainValue[vals.size()]));

			return returnState;
		} else {
			throw new UnsupportedOperationException(
			        "IntervalDomain does not support context switches other than Call and Return (yet)");
		}
	}

	/**
	 * Creates a map of Integers -&gt; Identifiers for a given desired number of variables. The created identifiers are
	 * unique temporary variable identifiers. It is ensured that the created identifiers do not already occur in the
	 * current state.
	 * 
	 * @param argNum
	 *            The number of temporary variables to create.
	 * @param state
	 *            The current state.
	 * @return A map containing for each index of the call statement's argument
	 */
	private Map<Integer, String> getArgumentTemporaries(final int argNum, final IntervalDomainState state) {
		Map<Integer, String> returnMap = new HashMap<>();

		String paramPrefix = "param_";

		boolean uniqueFound = false;

		while (!uniqueFound) {
			for (int i = 0; i < argNum; i++) {
				final StringBuilder sb = new StringBuilder();
				sb.append(paramPrefix);
				sb.append(i);
				final String currentParamName = sb.toString();
				if (state.containsVariable(currentParamName)) {
					final StringBuilder paramBuilder = new StringBuilder();
					paramBuilder.append(paramPrefix);
					paramBuilder.append("_");
					paramPrefix = paramBuilder.toString();
					break;
				}
			}
			uniqueFound = true;
		}

		for (int i = 0; i < argNum; i++) {
			final StringBuilder sb = new StringBuilder();
			sb.append(paramPrefix);
			sb.append(i);
			returnMap.put(i, sb.toString());
		}

		return returnMap;
	}

	/**
	 * Creates a dummy {@link IBoogieVar} from a given type. This method is used to give generated temporary variables a
	 * boogie type.
	 * 
	 * @param identifier
	 *            The identifier of the variable.
	 * @param type
	 *            The type of the variable.
	 * @return An {@link IBoogieVar} according to the given identifier and {@link IType}.
	 */
	private IBoogieVar createTemporaryIBoogieVar(final String identifier, final IType type) {
		return new IBoogieVar() {
			@Override
			public String getIdentifier() {
				return identifier;
			}

			@Override
			public IType getIType() {
				return type;
			}

			@Override
			public ApplicationTerm getDefaultConstant() {
				throw new UnsupportedOperationException("Temporary IBoogieVars dont have default constants.");
			}
		};
	}

	private Procedure getProcedure(final String procedureName) {
		return mSymbolTable.getFunctionOrProcedureDeclaration(procedureName).stream()
		        .filter(decl -> decl instanceof Procedure).map(decl -> (Procedure) decl)
		        .filter(proc -> proc.getBody() != null).findFirst().get();
	}

	private List<IntervalDomainValue> getOutParamValues(final String procedureName,
	        final IntervalDomainState stateBeforeLeaving) {
		// functions are already inlined and if there are procedure and implementation declaration for a proc, we know
		// that we only get the implementation from the FXPE
		final Procedure procedure = getProcedure(procedureName);

		final List<IntervalDomainValue> vals = new ArrayList<>();
		for (final VarList list : procedure.getOutParams()) {
			for (final String s : list.getIdentifiers()) {
				vals.add(stateBeforeLeaving.getValue(s));
			}
		}
		return vals;
	}
}
