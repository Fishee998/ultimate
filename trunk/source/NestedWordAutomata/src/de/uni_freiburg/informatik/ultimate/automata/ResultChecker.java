package de.uni_freiburg.informatik.ultimate.automata;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import de.uni_freiburg.informatik.ultimate.automata.AtsDefinitionPrinter.Labeling;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.DoubleDeckerAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.INestedWordAutomatonOldApi;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordGenerator;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.NestedWordAutomatonReachableStates;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa.BuchiAccepts;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa.BuchiIsIncluded;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa.NestedLassoRun;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.buchiNwa.NestedLassoWord;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IsEmpty;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.ConcurrentProduct;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.IsIncluded;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.MinimizeDfa;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.PowersetDeterminizer;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operations.buchiReduction.BuchiReduce;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.ComplementDD;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.ComplementSadd;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DeterminizeDD;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DeterminizeSadd;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DifferenceDD;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.DifferenceSadd;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.IntersectDD;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.IntersectNodd;
import de.uni_freiburg.informatik.ultimate.automata.nwalibrary.operationsOldApi.ReachableStatesCopy;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.PetriNet2FiniteAutomaton;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.PetriNetRun;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.julian.PetriNetJulian;
import de.uni_freiburg.informatik.ultimate.automata.preferences.PreferenceConstants;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;

@SuppressWarnings("unchecked")
public class ResultChecker<LETTER,STATE> {
	
	private static Logger s_Logger = 
		UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
	
	private static int resultCheckStackHeight = 0;
	public static final int maxResultCheckStackHeight = 1;
	
	public final static boolean m_InvariantCheck_DetComplementBuchi = false;
	
	public static boolean m_AlreadyDoingInvariantCheck = false;
	
	public static boolean doingInvariantCheck() {
		return resultCheckStackHeight > 0;
	}

	public static boolean isEmpty(INestedWordAutomatonOldApi op,
								  NestedRun result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of emptinessCheck");

		boolean correct = true;
		if (result == null) {
			s_Logger.warn("Emptiness not double checked ");
		}
		else {
			correct = op.accepts(result.getWord());
		}

		s_Logger.debug("Finished testing correctness of emptinessCheck");
		resultCheckStackHeight--;
		return correct;
	}

	
	
	public static boolean determinize(INestedWordAutomatonOldApi op,
									INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of determinization");

		boolean correct = true;
//		NwaBasicOperations nbo = new NwaBasicOperations((NestedWordAutomaton) op);
//		INestedWordAutomaton resultJM = nbo.determinizeJM();
//		correct &= (resultJM.included(result) == null);
//		correct &= (result.included(resultJM) == null);
		INestedWordAutomatonOldApi resultSadd = (new DeterminizeSadd<String,String>(op)).getResult();
		correct &= (nwaLanguageInclusion(resultSadd,result) == null);
		correct &= (nwaLanguageInclusion(result,resultSadd) == null);
		INestedWordAutomatonOldApi resultDD = (new DeterminizeDD<String,String>(op)).getResult();
		correct &= (nwaLanguageInclusion(resultDD,result) == null);
		correct &= (nwaLanguageInclusion(result,resultDD) == null);
	
		s_Logger.debug("Finished testing correctness of determinization");
		resultCheckStackHeight--;
		return correct;
	}
	
	public static boolean complement(INestedWordAutomatonOldApi op,
									  INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of complement");

		boolean correct = true;
//		INestedWordAutomaton complementJM = (new Complement()).new ComplementJM(op).getResult();
//		correct &=  ((new Intersect(false, false, op, complementJM)).getNwa().getAcceptingNestedRun() == null);
		INestedWordAutomatonOldApi complementSadd = (new ComplementSadd(op)).getResult();
		INestedWordAutomatonOldApi intersectionWithSadd = (new IntersectDD(false, op, complementSadd)).getResult();
		correct &=  ((new IsEmpty(intersectionWithSadd)).getResult() == true);
		INestedWordAutomatonOldApi complementDD = (new ComplementDD(op)).getResult();
		INestedWordAutomatonOldApi intersectionWithDD = (new IntersectDD(false, op, complementDD)).getResult();
		correct &= (new IsEmpty(intersectionWithDD).getResult() == true);

		s_Logger.debug("Finished testing correctness of complement");
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean intersect(INestedWordAutomatonOldApi operand1,
									INestedWordAutomatonOldApi operand2,
									INestedWordAutomatonOldApi result) throws OperationCanceledException {
		s_Logger.warn("Correctness of Intersection not checked at the moment.");
		return true;
	}
	
	public static boolean difference(INestedWordAutomatonOldApi fst,
									 INestedWordAutomatonOldApi snd,
									 INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of difference");

		INestedWordAutomatonOldApi sndComplementDD = 
			(new ComplementDD(snd)).getResult();
		INestedWordAutomatonOldApi resultDD = 
			(new IntersectNodd(fst,sndComplementDD)).getResult();
		boolean correct = true;
		correct &= (nwaLanguageInclusion(result,resultDD) == null);
		assert correct;
		correct &= (nwaLanguageInclusion(resultDD,result) == null);
		assert correct;
		
		INestedWordAutomatonOldApi sndComplementSadd = 
			(new ComplementSadd(snd)).getResult();
		INestedWordAutomatonOldApi resultSadd = 
			(new IntersectNodd(fst,sndComplementSadd)).getResult();
		correct &= (nwaLanguageInclusion(result,resultSadd) == null);
		assert correct;
		correct &= (nwaLanguageInclusion(resultSadd,result) == null);
		assert correct;
		
		s_Logger.info("Finished testing correctness of difference");
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean differenceCheckWithSadd(INestedWordAutomatonOldApi fst,
			 INestedWordAutomatonOldApi snd,
			 INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of difference");
		
		INestedWordAutomatonOldApi resultSadd = (new DifferenceSadd(fst, snd)).getResult();

		boolean correct = true;
		try {
			NestedRun subsetCounterexample = nwaLanguageInclusion(resultSadd, result);
			if (subsetCounterexample != null) {
				s_Logger.error("Word accepted by resultSadd, but not by result: " + subsetCounterexample.getWord());
				correct = false;
				String message = "// Problem with run " + subsetCounterexample.toString();
				writeToFileIfPreferred(fst, "FailedDifferenceCheck-Minuend-", message);
				writeToFileIfPreferred(snd, "FailedDifferenceCheck-Subtrahend-", message);
			}
			NestedRun supersetCounterexample = nwaLanguageInclusion(result, resultSadd);
			if (supersetCounterexample != null) {
				s_Logger.error("Word accepted by result, but not by resultSadd: " + supersetCounterexample.getWord());
				correct = false;
				String message = "// Problem with run " + supersetCounterexample.toString();
				writeToFileIfPreferred(fst, "FailedDifferenceCheck-Minuend-", message);
				writeToFileIfPreferred(snd, "FailedDifferenceCheck-Subtrahend-", message);
			}
		} catch (OperationCanceledException e) {
			s_Logger.warn("ResultChecker canceled");
		}

		s_Logger.debug("Finished testing correctness of minimizeDfa");
		resultCheckStackHeight--;
		return correct;
	}
	
	
	
	public static boolean minimize(INestedWordAutomatonOldApi operand,
									  INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of minimizeDfa");

		boolean correct = true;
		try {
			NestedRun subsetCounterexample = nwaLanguageInclusion(operand, result);
			if (subsetCounterexample != null) {
				s_Logger.error("Word accepted by operand, but not by result: " + subsetCounterexample.getWord());
				correct = false;
				String message = "// Problem with run " + subsetCounterexample.toString();
				writeToFileIfPreferred(operand, "FailedNwaEquivalenceCheck", message);
			}
			NestedRun supersetCounterexample = nwaLanguageInclusion(result, operand);
			if (supersetCounterexample != null) {
				s_Logger.error("Word accepted by result, but not by operand: " + supersetCounterexample.getWord());
				correct = false;
				String message = "// Problem with run " + supersetCounterexample.toString();
				writeToFileIfPreferred(operand, "FailedNwaEquivalenceCheck", message);
			}

		
//		NestedLassoRun inOperandButNotInResultBuchi = operand.buchiIncluded(result);
//		if (inOperandButNotInResult != null) {
//			s_Logger.error("Lasso word accepted by operand, but not by result: " + 
//					inOperandButNotInResultBuchi.getNestedLassoWord());
//			correct = false;
//		}
//		NestedLassoRun inResultButNotInOperatndBuchi = result.buchiIncluded(operand);
//		if (inResultButNotInOperatnd != null) {
//			s_Logger.error("Lasso word accepted by result, but not by operand: " + 
//					inResultButNotInOperatndBuchi.getNestedLassoWord());
//			correct = false;
//		}
		
		} catch (OperationCanceledException e) {
			s_Logger.warn("ResultChecker canceled");
		}

		s_Logger.debug("Finished testing correctness of minimizeDfa");
		resultCheckStackHeight--;
		return correct;
	}
	
	public static boolean reduceBuchi(INestedWordAutomatonOldApi operand,
			INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight)
			return true;
		resultCheckStackHeight++;
		s_Logger.debug("Testing correctness of reduceBuchi");
		
		INestedWordAutomatonOldApi minimizedOperand = (new MinimizeDfa(operand)).getResult();

		boolean correct = true;
		NestedLassoRun inOperandButNotInResultBuchi = nwaBuchiLanguageInclusion(minimizedOperand,result);
		if (inOperandButNotInResultBuchi != null) {
			s_Logger.error("Lasso word accepted by operand, but not by result: " + 
					inOperandButNotInResultBuchi.getNestedLassoWord());
			correct = false;
		}
		NestedLassoRun inResultButNotInOperatndBuchi = nwaBuchiLanguageInclusion(result,minimizedOperand);
		if (inResultButNotInOperatndBuchi != null) {
			s_Logger.error("Lasso word accepted by result, but not by operand: " + 
					inResultButNotInOperatndBuchi.getNestedLassoWord());
			correct = false;
		}

		s_Logger.debug("Finished testing correctness of reduceBuchi");
		resultCheckStackHeight--;
		return correct;
	}
	
	public static boolean buchiEmptiness(INestedWordAutomatonOldApi operand,
										 NestedLassoRun result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of buchiEmptiness");

		boolean correct = true;
		if (result == null) {
			s_Logger.warn("No check for positive buchiEmptiness");
		} else {
			correct = (new BuchiAccepts(operand, result.getNestedLassoWord())).getResult();
		}

		s_Logger.info("Finished testing correctness of buchiEmptiness");
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean buchiIntersect(INestedWordAutomatonOldApi operand1,
			INestedWordAutomatonOldApi operand2,
			INestedWordAutomatonOldApi result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of buchiIntersect");

		boolean correct = true;
		s_Logger.warn("No test for buchiIntersection available yet");

		s_Logger.info("Finished testing correctness of buchiIntersect");
		resultCheckStackHeight--;
		return correct;
	}
	

	
	public static boolean buchiComplement(INestedWordAutomatonOldApi operand,
										  INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of complementBuchi");
		
		int maxNumberOfStates = Math.max(operand.size(), result.size());
		NestedWordGenerator gen = new NestedWordGenerator(
				operand.getInternalAlphabet(), operand.getCallAlphabet(), operand.getReturnAlphabet());
		boolean correct = true;
		for (int i=0; i<10; i++) {
						NestedLassoWord lasso = gen.generateNestedLassoWord(maxNumberOfStates, 0, 0);
			boolean operandAccepts = (new BuchiAccepts(operand, lasso)).getResult();
			boolean resultAccepts = (new BuchiAccepts(result, lasso)).getResult();
			if (operandAccepts ^ resultAccepts) {
				// check passed
			} else {
				correct = false;
				String message = "// Problem with lasso " + lasso.toString();
				writeToFileIfPreferred(operand, "FailedBuchiComplementCheck", message);
				break;
			}
		}
		
//		INestedWordAutomaton intersection = (new Intersect(true, false, operand, result)).getResult();
//		NestedLassoRun ctx = new EmptinessCheck().getAcceptingNestedLassoRun(intersection);
//		boolean correct = (ctx == null);
//		assert (correct);
		
		s_Logger.info("Finished testing correctness of complementBuchi");
		nwaInvarintChecks(result);
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean buchiComplementSVW(INestedWordAutomatonOldApi operand,
			INestedWordAutomatonOldApi result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of complementBuchiSVW");
		
		int maxNumberOfStates = Math.max(operand.size(), result.size());
		NestedWordGenerator gen = new NestedWordGenerator(
				operand.getInternalAlphabet(), operand.getCallAlphabet(), operand.getReturnAlphabet());
		boolean correct = true;
		for (int i=0; i<10; i++) {
						NestedLassoWord lasso = gen.generateNestedLassoWord(maxNumberOfStates, maxNumberOfStates, 0, 0);
			boolean operandAccepts = (new BuchiAccepts(operand, lasso)).getResult();
			boolean resultAccepts = (new BuchiAccepts(operand, lasso)).getResult();
			if (operandAccepts ^ resultAccepts) {
				// ok
			} else {
				correct = false;
				break;
			}
		}


//		{
//			INestedWordAutomaton intersection = (new Intersect(true, false, operand, result)).getResult();
//			NestedLassoRun ctx = new EmptinessCheck().getAcceptingNestedLassoRun(intersection);
//			correct = (ctx == null);
//			assert (correct);
//		}
//		
//		{
//			INestedWordAutomaton operandComplement = (new BuchiComplementFKV(operand)).getResult();
//			INestedWordAutomaton resultComplement = (new BuchiComplementFKV(result)).getResult();
//			INestedWordAutomaton intersection = (new Intersect(true, false, operandComplement, resultComplement)).getResult();
//			NestedLassoRun ctx = new EmptinessCheck().getAcceptingNestedLassoRun(intersection);
//			correct = (ctx == null);
//			assert (correct);
//		}

		s_Logger.info("Finished testing correctness of complementBuchiSVW");
		nwaInvarintChecks(result);
		resultCheckStackHeight--;
		return correct;
	}
	
	
	
	public static boolean petriNetJulian(INestedWordAutomatonOldApi op,
										 PetriNetJulian result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of PetriNetJulian constructor");

		INestedWordAutomatonOldApi resultAutomata = 
							(new PetriNet2FiniteAutomaton(result)).getResult();
		boolean correct = true;
		correct &= (nwaLanguageInclusion(resultAutomata,op) == null);
		correct &= (nwaLanguageInclusion(op,resultAutomata) == null);

		s_Logger.info("Finished testing correctness of PetriNetJulian constructor");
		netInvarintChecks(result);
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean accepts(PetriNetJulian net,
								  Word word,
								  boolean result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of accepts");
		
		NestedWord nw = new NestedWord(word);
		boolean resultAutomata = (new PetriNet2FiniteAutomaton(net)).getResult().accepts(nw);
		boolean correct = (result == resultAutomata);

		s_Logger.info("Finished testing correctness of accepts");
		netInvarintChecks(net);
		resultCheckStackHeight--;
		return correct;
	}
	
	@Deprecated
	public static boolean isEmpty(PetriNetJulian net,
								  NestedRun result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of emptinessCheck");

		boolean correct = true;
		if (result == null) {
			NestedRun automataRun = (new IsEmpty((new PetriNet2FiniteAutomaton(net)).getResult())).getNestedRun();
			correct = (automataRun == null);
		} else {
			correct =  net.accepts(result.getWord());
		}
		s_Logger.info("Finished testing correctness of emptinessCheck");
		netInvarintChecks(net);
		resultCheckStackHeight--;
		return correct;
	}
	
	public static boolean isEmpty(PetriNetJulian net,
									PetriNetRun result) {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of emptinessCheck");

		boolean correct = true;
		if (result == null) {
			NestedRun automataRun = (new IsEmpty((new PetriNet2FiniteAutomaton(net)).getResult())).getNestedRun();
			if (automataRun != null) {
				correct = false;
				s_Logger.error("EmptinessCheck says empty, but net accepts: " + automataRun.getWord());
			}
			correct = (automataRun == null);
		} else {
			Word word = result.getWord();
			if (net.accepts(word)) {
				correct = true;
			}
			else {
				s_Logger.error("Result of EmptinessCheck, but not accepted: " + word);
				correct = false;
			}
		}
		s_Logger.info("Finished testing correctness of emptinessCheck");
		netInvarintChecks(net);
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean prefixProduct(PetriNetJulian operand1,
										NestedWordAutomaton operand2,
										PetriNetJulian result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of prefixProduct");

		INestedWordAutomatonOldApi op1AsNwa = (new PetriNet2FiniteAutomaton(operand1)).getResult();
		INestedWordAutomatonOldApi resultAsNwa = (new PetriNet2FiniteAutomaton(result)).getResult();
		INestedWordAutomatonOldApi nwaResult = (new ConcurrentProduct(op1AsNwa, operand2, true)).getResult();
		boolean correct = true;
		correct &= (new IsIncluded(resultAsNwa,nwaResult)).getResult();
		correct &= (new IsIncluded(nwaResult,resultAsNwa)).getResult();

		s_Logger.info("Finished testing correctness of prefixProduct");
		netInvarintChecks(result);
		resultCheckStackHeight--;
		return correct;
	}
	
	
	public static boolean differenceBlackAndWhite(PetriNetJulian operand1,
								  				  NestedWordAutomaton operand2,
								  				  PetriNetJulian result) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing correctness of differenceBlackAndWhite");

		INestedWordAutomatonOldApi op1AsNwa = (new PetriNet2FiniteAutomaton(operand1)).getResult();
		INestedWordAutomatonOldApi rcResult = (new DifferenceDD(op1AsNwa, operand2)).getResult();
		INestedWordAutomatonOldApi resultAsNwa = (new PetriNet2FiniteAutomaton(result)).getResult();
		boolean correct = true;
		correct &= (nwaLanguageInclusion(resultAsNwa,rcResult) == null);
		correct &= (nwaLanguageInclusion(rcResult,resultAsNwa) == null);

		s_Logger.info("Finished testing correctness of differenceBlackAndWhite");
		netInvarintChecks(operand1);
		resultCheckStackHeight--;
		return correct;
	}
	
	public static boolean petriNetLanguageEquivalence(PetriNetJulian net1, PetriNetJulian net2) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		s_Logger.info("Testing Petri net language equivalence");
		INestedWordAutomatonOldApi finAuto1 = (new PetriNet2FiniteAutomaton(net1)).getResult();
		INestedWordAutomatonOldApi finAuto2 = (new PetriNet2FiniteAutomaton(net2)).getResult();
		NestedRun subsetCounterex = nwaLanguageInclusion(finAuto1, finAuto2);
		boolean subset = subsetCounterex == null;
		if (!subset) {
			s_Logger.error("Only accepted by first: " + subsetCounterex.getWord());
		}
		NestedRun supersetCounterex = nwaLanguageInclusion(finAuto2, finAuto1);
		boolean superset = supersetCounterex == null;
		if (!superset) {
			s_Logger.error("Only accepted by second: " + supersetCounterex.getWord());
		}
		boolean result = subset && superset;
		s_Logger.info("Finished Petri net language equivalence");
		resultCheckStackHeight--;
		return result;
	}
	
	public static <LETTER, STATE> boolean removeUnreachable(NestedWordAutomatonReachableStates<LETTER, STATE> result, INestedWordAutomatonOldApi<LETTER, STATE> operand) throws OperationCanceledException {
		if (resultCheckStackHeight >= maxResultCheckStackHeight) return true;
		resultCheckStackHeight++;
		boolean correct = true;
		s_Logger.info("Testing removeUnreachable");
		try {
		{
			NestedRun subsetCounterex = nwaLanguageInclusion(result, operand);
			correct &= (subsetCounterex == null);
			assert correct;
			NestedRun supersetCounterex = nwaLanguageInclusion(operand, result);
			correct &= (subsetCounterex == null);
			assert correct;
		}
		DoubleDeckerAutomaton<LETTER, STATE> reachalbeStatesCopy = (DoubleDeckerAutomaton<LETTER, STATE>) (new ReachableStatesCopy(operand)).getResult();
		correct &= isSubset(reachalbeStatesCopy.getStates(),result.getStates());
		correct &= isSubset(result.getStates(),reachalbeStatesCopy.getStates());
		for (STATE state : reachalbeStatesCopy.getStates()) {
			Set<STATE> rCSdownStates = reachalbeStatesCopy.getDownStates(state);
			Set<STATE> rCAdownStates = result.getDownStates(state);
			correct &= isSubset(rCAdownStates, rCSdownStates);
			assert correct;
			correct &= isSubset(rCSdownStates, rCAdownStates);
			assert correct;
		}
		} catch (Error e) {
			String message = "// Problem with  removeUnreachable";
			writeToFileIfPreferred(operand, "FailedremoveUnreachable", message);
			throw e;
		} catch (RuntimeException e) {
			String message = "// Problem with  removeUnreachable";
			writeToFileIfPreferred(operand, "FailedremoveUnreachable", message);
			throw e;
		} finally {
			s_Logger.info("Finished removeUnreachable");
			resultCheckStackHeight--;
		}
		return correct;
	}
	
	public static <E> boolean isSubset(Collection<E> lhs, Collection<E> rhs) {
		for (E elem : lhs) {
			if (!rhs.contains(elem)) {
				return false;
			}
		}
		return true;
	}


//	private static boolean nwaLanguageEquivalence(INestedWordAutomaton nwa1, INestedWordAutomaton nwa2) throws OperationCanceledException {
//		s_Logger.info("Testing nwa equivalence");
//		
//		boolean correct = true;
//		INestedWordAutomaton nwa1MinusNwa2 = (new Difference(nwa1, nwa2)).getResult();
//		NestedRun inNwa1ButNotInNwa2 = (new BfsEmptiness(nwa1MinusNwa2)).getResult();
//		if (inNwa1ButNotInNwa2 != null) {
//			s_Logger.error("Word accepted by nwa1, but not by nwa2: " + 
//					inNwa1ButNotInNwa2.getWord());
//			correct = false;
//		}
//		INestedWordAutomaton nwa2MinusNwa1 = (new Difference(nwa2, nwa1)).getResult();
//		NestedRun inNwa2ButNotInNwa1 = (new BfsEmptiness(nwa2MinusNwa1)).getResult();
//		if (inNwa2ButNotInNwa1 != null) {
//			s_Logger.error("Word accepted by nwa2, but not by nwa1: " + 
//					inNwa2ButNotInNwa1.getWord());
//			correct = false;
//		}
//		
//		s_Logger.info("Finished testing nwa equivalence");
//		return correct;
//	}
	
	private static NestedRun nwaLanguageInclusion(INestedWordAutomatonOldApi nwa1, INestedWordAutomatonOldApi nwa2) throws OperationCanceledException {
		INestedWordAutomatonOldApi nwa1MinusNwa2 = (new DifferenceDD(nwa1, nwa2)).getResult();
		NestedRun inNwa1ButNotInNwa2 = (new IsEmpty(nwa1MinusNwa2)).getNestedRun();
		return inNwa1ButNotInNwa2;
//		if (inNwa1ButNotInNwa2 != null) {
//			s_Logger.error("Word accepted by nwa1, but not by nwa2: " + 
//					inNwa1ButNotInNwa2.getWord());
//			correct = false;
//		}
	}
	
	private static NestedLassoRun nwaBuchiLanguageInclusion(INestedWordAutomatonOldApi nwa1, INestedWordAutomatonOldApi nwa2) throws OperationCanceledException {
		return (new BuchiIsIncluded(nwa1, nwa2)).getCounterexample();
	}
	
	
	
	public static void nwaInvarintChecks(INestedWordAutomatonOldApi nwa) throws OperationCanceledException {
		if (m_AlreadyDoingInvariantCheck) {
			return;
		}
		m_AlreadyDoingInvariantCheck = true;
		if (m_InvariantCheck_DetComplementBuchi) {
			s_Logger.debug("Start additional invariant checks.");
			if (nwa.getCallAlphabet().isEmpty()) {
				new BuchiReduce(nwa).getResult();
			}
//			if (nwa.isDeterministic()) {
//				INestedWordAutomaton complement = new BuchiComplementDeterministic(nwa).getResult();
//				assert (buchiComplement(nwa, complement));
//			}
		}
		m_AlreadyDoingInvariantCheck = false;
	}
	
	
	public static void netInvarintChecks(PetriNetJulian net) {
		if (m_AlreadyDoingInvariantCheck) {
			return;
		}
		m_AlreadyDoingInvariantCheck = true;

		// enter checks here.
		
		m_AlreadyDoingInvariantCheck = false;
	}
	
    private static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    public static void writeToFileIfPreferred(IAutomaton automaton, String filenamePrefix, String message) {
		ConfigurationScope scope = new ConfigurationScope();
		IEclipsePreferences prefs = scope.getNode(Activator.PLUGIN_ID);
		boolean writeToFile = prefs.getBoolean(PreferenceConstants.Name_Write, PreferenceConstants.Default_Write);
		if (writeToFile) {
			String directory = prefs.get(PreferenceConstants.Name_Path, PreferenceConstants.Default_Path); 
			String filename = directory + File.separator+filenamePrefix + getDateTime() + ".fat";
			new AtsDefinitionPrinter(automaton, filename, Labeling.QUOTED, message);
		}
    }
	
}
