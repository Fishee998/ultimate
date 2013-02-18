package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa;

import org.apache.log4j.Logger;
import de.uni_freiburg.informatik.ultimate.automata.Activator;
import de.uni_freiburg.informatik.ultimate.automata.IOperation;
import de.uni_freiburg.informatik.ultimate.automata.OperationCanceledException;
import de.uni_freiburg.informatik.ultimate.automata.ResultChecker;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;

/**
 * Büchi complementation based on the method of Sistla, Vardi, Wolper: <br>
 * 
 *     “The Complementation Problem for Büchi Automata with Applications to
 *      Temporal Logic” (Elsevier, 1987) <br>
 *      
 * The actual implementation of this complementation method is located in the
 * class {@code BuchiComplementAutomatonSVW}.
 * 
 * @author Fabian Reiter
 *
 */
public class BuchiComplementSVW<LETTER,STATE> implements IOperation {
	
	private static Logger s_Logger = 
		UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
	
	private INestedWordAutomaton<LETTER,STATE> m_Operand;
	private BuchiComplementAutomatonSVW<LETTER,STATE> m_Result;

	
	@Override
	public String operationName() {
		return "buchiComplementSVW";
	}
	
	@Override
	public String startMessage() {
		return "Start " + operationName() + ". Operand " +
				m_Operand.sizeInformation();
	}
	
	@Override
	public String exitMessage() {
		return "Finished " + operationName() + ". Result " + 
				m_Result.sizeInformation();
	}
		
	public BuchiComplementSVW(INestedWordAutomaton<LETTER,STATE> operand)
			throws OperationCanceledException {
		m_Operand = operand;
		s_Logger.info(startMessage());
		m_Result = new BuchiComplementAutomatonSVW<LETTER, STATE>(operand);
		s_Logger.info(exitMessage());
	}

	@Override
	public INestedWordAutomaton<LETTER,STATE> getResult()
			throws OperationCanceledException {
		assert ResultChecker.buchiComplement(m_Operand, m_Result);
		return m_Result;
	}
	
}