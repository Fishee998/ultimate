/*
 * Copyright (C) 2009-2014 University of Freiburg
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
 * along with the ULTIMATE Automata Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.automata.AutomataLibraryException;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.LibraryIdentifiers;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.StateFactory;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IsEmpty;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;

public class ComplementDD<LETTER, STATE> implements IOperation<LETTER, STATE> {

	private final IUltimateServiceProvider m_Services;
	private final Logger s_Logger;

	protected INestedWordAutomatonOldApi<LETTER, STATE> m_Operand;
	protected INestedWordAutomatonOldApi<LETTER, STATE> m_DeterminizedOperand;
	protected INestedWordAutomatonOldApi<LETTER, STATE> m_Result;

	@Override
	public String operationName() {
		return "complementDD";
	}

	@Override
	public String startMessage() {
		return "Start " + operationName() + " Operand "
				+ m_Operand.sizeInformation();
	}

	@Override
	public String exitMessage() {
		return "Finished " + operationName() + " Result "
				+ m_Result.sizeInformation();
	}

	public INestedWordAutomatonOldApi<LETTER, STATE> getResult()
			throws AutomataLibraryException {
		return m_Result;
	}

	public ComplementDD(IUltimateServiceProvider services,
			StateFactory<STATE> stateFactory,
			INestedWordAutomatonOldApi<LETTER, STATE> operand)
			throws AutomataLibraryException {
		m_Services = services;
		s_Logger = m_Services.getLoggingService().getLogger(LibraryIdentifiers.s_LibraryID);
		m_Operand = operand;

		s_Logger.info(startMessage());
		if (!m_Operand.isDeterministic()) {
			m_DeterminizedOperand = 
				   (new DeterminizeDD<LETTER, STATE>(m_Services, stateFactory, m_Operand)).getResult();
		} else {
			m_DeterminizedOperand = m_Operand;
			s_Logger.debug("Operand is already deterministic");
		}
		m_Result = new ReachableStatesCopy<LETTER, STATE>(
				m_Services, m_DeterminizedOperand, true, true, false, false).getResult();
		s_Logger.info(exitMessage());
	}

	@Override
	public boolean checkResult(StateFactory<STATE> stateFactory)
			throws AutomataLibraryException {
		s_Logger.debug("Testing correctness of complement");
		boolean correct = true;
		INestedWordAutomatonOldApi intersectionOperandResult = (new IntersectDD(m_Services, false, m_Operand, m_Result)).getResult();
		correct &=  ((new IsEmpty(m_Services, intersectionOperandResult)).getResult() == true);
		s_Logger.debug("Finished testing correctness of complement");
		return correct;
	}

}
