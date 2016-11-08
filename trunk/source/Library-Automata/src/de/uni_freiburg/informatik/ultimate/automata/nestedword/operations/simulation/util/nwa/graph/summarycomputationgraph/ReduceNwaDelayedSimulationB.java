/*
 * Copyright (C) 2016 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 * 
 * This file is part of the ULTIMATE Automata Library.
 * 
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Automata Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.simulation.util.nwa.graph.summarycomputationgraph;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryServices;
import de.uni_freiburg.informatik.ultimate.automata.AutomataOperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.IDoubleDeckerAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.buchi.TestBuchiEquivalence;
import de.uni_freiburg.informatik.ultimate.automata.nestedword.operations.minimization.IMinimizeNwa;
import de.uni_freiburg.informatik.ultimate.automata.statefactory.IStateFactory;

/**
 * 
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 *
 * @param <LETTER>
 * @param <STATE>
 */
public class ReduceNwaDelayedSimulationB<LETTER, STATE> extends ReduceNwaSimulationBased<LETTER, STATE> implements IMinimizeNwa<LETTER, STATE> {

	public ReduceNwaDelayedSimulationB(final AutomataLibraryServices services, final IStateFactory<STATE> stateFactory,
			final IDoubleDeckerAutomaton<LETTER, STATE> operand) throws AutomataOperationCanceledException {
		super(services, stateFactory, operand, new DelayedSimulationInfoProvider<>());
	}
	
	
	@Override
	public boolean checkResult(final IStateFactory<STATE> stateFactory) throws AutomataLibraryException {
		mLogger.info("Start testing correctness of " + operationName());
		final boolean correct = (new TestBuchiEquivalence<LETTER, STATE>(mServices, stateFactory, getOperand(),
				getResult())).getResult();
		mLogger.info("Finished testing correctness of " + operationName());
		return correct;
	}
}