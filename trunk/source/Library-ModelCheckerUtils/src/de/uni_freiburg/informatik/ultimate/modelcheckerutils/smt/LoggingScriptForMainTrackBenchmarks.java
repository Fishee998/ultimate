/*
 * Copyright (C) 2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2012-2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 * 
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uni_freiburg.informatik.ultimate.logic.AnnotatedTerm;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;

public class LoggingScriptForMainTrackBenchmarks extends LoggingScriptForNonIncrementalBenchmarks {
	
	private int mWrittenScriptCounter = 0;
	
	private final int mBenchmarkTooSimpleThreshold = 10 * 1000;
	private final boolean mWriteUnsolvedBenchmarks = true;

	public LoggingScriptForMainTrackBenchmarks(final Script script,
			final String baseFilename, final String directory) {
		super(script, baseFilename, directory);
	}

	@Override
	public LBool checkSat() throws SMTLIBException {
		final long timeBefore = System.nanoTime();
		final LBool sat = super.mScript.checkSat();
		final long durationInMilliseconds = (System.nanoTime() - timeBefore)/1000/1000;
		final boolean solved = (sat == LBool.SAT || sat == LBool.UNSAT);
		if (solved && durationInMilliseconds >= mBenchmarkTooSimpleThreshold
				|| !solved && mWriteUnsolvedBenchmarks) {
			final File file = constructFile('_' + String.valueOf(mWrittenScriptCounter));
			final List<ArrayList<ISmtCommand>> processedCommandStack = process(mCommandStack, sat);
			writeCommandStackToFile(file, processedCommandStack);
			mWrittenScriptCounter++;
		}
		return sat;
	}
	
	@Override
	public LBool assertTerm(final Term term) throws SMTLIBException {
		final Term nonNamedTerm;
		if (term instanceof AnnotatedTerm) {
			nonNamedTerm = ((AnnotatedTerm) term).getSubterm();
		} else {
			nonNamedTerm = term;
		}
		if (nonNamedTerm != mScript.term("true")) {
			addToCurrentAssertionStack(new AssertCommand(nonNamedTerm));
		}
		return mScript.assertTerm(term);
	}
	
	@Override
	public Map<Term, Term> getValue(final Term[] terms) throws SMTLIBException,
			UnsupportedOperationException {
		return mScript.getValue(terms);
	}

	private List<ArrayList<ISmtCommand>> process(final LinkedList<ArrayList<ISmtCommand>> commandStack, final LBool status) {
		final ArrayList<ISmtCommand> flattenedStack = new ArrayList<>();
		addInvarSynthCommands(flattenedStack, status);
		boolean toKeepCommandsReached = false;
		for (final ArrayList<ISmtCommand> list : commandStack) {
			for (final ISmtCommand command : list) {
				if (!toKeepCommandsReached) {
					if (command.toString().contains("declare-fun")) {
						toKeepCommandsReached = true;
					}
				}
				if (toKeepCommandsReached) {
					flattenedStack.add(command);
				}
			}
		}
		flattenedStack.add(new SmtCommandInStringRepresentation("(check-sat)" + System.lineSeparator()));
		flattenedStack.add(new SmtCommandInStringRepresentation("(exit)" + System.lineSeparator()));
		return Collections.singletonList(flattenedStack);
	}

	private void addInvarSynthCommands(final ArrayList<ISmtCommand> flattenedStack, final LBool status) {
		final String logic = "(set-logic " + getLogic() + ")" + System.lineSeparator();
		flattenedStack.add(new SmtCommandInStringRepresentation(logic));
		flattenedStack.add(new SmtCommandInStringRepresentation(getSourceInfo()));
		final String version = "(set-info :smt-lib-version 2.5)" + System.lineSeparator();
		flattenedStack.add(new SmtCommandInStringRepresentation(version));
		final String category = "(set-info :category \"industrial\")" + System.lineSeparator();
		flattenedStack.add(new SmtCommandInStringRepresentation(category));
		final String statusInfo = "(set-info :status " + status + ")" + System.lineSeparator();
		flattenedStack.add(new SmtCommandInStringRepresentation(statusInfo));
	}
	
	
	public static String getSourceInfo() {
//		return SOURCE_INVSYNTH;
//		return SOURCE_GNTA;
		return SOURCE_AUTOMIZER;
	}
	
	public static String getLogic() {
//		return "QF_NRA";
//		return "QF_NIA";
//		return "QF_AUFNIRA";
//		return "QF_ABV";
		return "BV";
	}
	
	public static String SOURCE_INVSYNTH =
			"(set-info :source |" + System.lineSeparator() +
			"SMT script generated by Ultimate Automizer [1,2]." + System.lineSeparator() +
			"Ultimate Automizer is a software verifier for C programs that implements an" + System.lineSeparator() +
			"automata-based approach [3]." + System.lineSeparator() +
			"The commands in this SMT scripts are used for a constraint-based synthesis" + System.lineSeparator() +
			"of invariants [4]." + System.lineSeparator() +
			"" + System.lineSeparator() +
			"2016-04-30, Matthias Heizmann (heizmann@informatik.uni-freiburg.de)" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"[1] http://http://ultimate.informatik.uni-freiburg.de/automizer/" + System.lineSeparator() +
			"[2] Matthias Heizmann, Daniel Dietsch, Marius Greitschus, Jan Leike," + System.lineSeparator() +
			"Betim Musa, Claus Schätzle, Andreas Podelski: Ultimate Automizer with" + System.lineSeparator() +
			"Two-track Proofs - (Competition Contribution). TACAS 2016: 950-953" + System.lineSeparator() +
			"[3] Matthias Heizmann, Jochen Hoenicke, Andreas Podelski: Software Model" + System.lineSeparator() +
			"Checking for People Who Love Automata. CAV 2013:36-52" + System.lineSeparator() +
			"[4] Michael Colon, Sriram Sankaranarayanan, Henny Sipma: Linear Invariant" + System.lineSeparator() +
			"Generation Using Non-linear Constraint Solving. CAV 2003: 420-432" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"|)" + System.lineSeparator();
	
	public static String SOURCE_GNTA =
			"(set-info :source |" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"SMT script generated by Ultimate LassoRanker [1]." + System.lineSeparator() +
			"Ultimate LassoRanker is a tool that analyzes termination and nontermination of"  + System.lineSeparator() +
			"lasso-shaped programs. This script contains the SMT commands that Ultimate " + System.lineSeparator() +
			"LassoRanker used while checking if a lasso-shaped program has a geometric " + System.lineSeparator() +
			"nontermination argument. (See [2] for a preliminary definition of" + System.lineSeparator() +
			"geometric nontermination argument.)" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"This SMT script belongs to a set of SMT scripts that was generated by applying" + System.lineSeparator() +
			"Ultimate Buchi Automizer [3,4] to benchmarks from the SV-COMP 2016 [5,6] " + System.lineSeparator() +
			"which are available at [7]. Ultimate Buchi Automizer takes omega-traces" + System.lineSeparator() +
			"(lasso-shaped programs) and uses LassoRanker in order to check if the " + System.lineSeparator() +
			"lasso-shaped program is terminating." + System.lineSeparator() +
			"" + System.lineSeparator() +
			"2016-04-30, Matthias Heizmann (heizmann@informatik.uni-freiburg.de)" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"[1] https://ultimate.informatik.uni-freiburg.de/LassoRanker/" + System.lineSeparator() +
			"[2] Jan Leike, Matthias Heizmann: Geometric Series as Nontermination"  + System.lineSeparator() +
			"Arguments for Linear Lasso Programs. CoRR abs/1405.4413 (2014)"  + System.lineSeparator() +
			"http://arxiv.org/abs/1405.4413" + System.lineSeparator() +
			"[3] http://ultimate.informatik.uni-freiburg.de/BuchiAutomizer/" + System.lineSeparator() +
			"[4] Matthias Heizmann, Jochen Hoenicke, Andreas Podelski: Software Model"  + System.lineSeparator() +
			"Checking for People Who Love Automata. CAV 2013:36-52" + System.lineSeparator() +
			"[5] http://sv-comp.sosy-lab.org/2016/" + System.lineSeparator() +
			"[6] Dirk Beyer: Reliable and Reproducible Competition Results with BenchExec"  + System.lineSeparator() +
			"and Witnesses (Report on SV-COMP 2016). TACAS 2016: 887-904" + System.lineSeparator() +
			"[7] https://github.com/dbeyer/sv-benchmarks" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"|)" + System.lineSeparator();
	
	public static String SOURCE_AUTOMIZER =
			"(set-info :source |" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"SMT script generated by Ultimate Automizer [1,2]." + System.lineSeparator() +
			"Ultimate Automizer is a software verifier for C programs that implements an" + System.lineSeparator() +
			"automata-based approach [3]." + System.lineSeparator() +
			"This SMT script belongs to a set of SMT scripts that was generated by applying" + System.lineSeparator() +
			"Ultimate Automizer to benchmarks from the SV-COMP 2016 [4,5] which are"  + System.lineSeparator() +
			"available at [6]. " + System.lineSeparator() +
			"" + System.lineSeparator() +
			"May 2016, Matthias Heizmann (heizmann@informatik.uni-freiburg.de)" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"[1] http://http://ultimate.informatik.uni-freiburg.de/automizer/" + System.lineSeparator() +
			"[2] Matthias Heizmann, Daniel Dietsch, Marius Greitschus, Jan Leike, " + System.lineSeparator() +
			"Betim Musa, Claus Schätzle, Andreas Podelski: Ultimate Automizer with"  + System.lineSeparator() +
			"Two-track Proofs - (Competition Contribution). TACAS 2016: 950-953" + System.lineSeparator() +
			"[3] Matthias Heizmann, Jochen Hoenicke, Andreas Podelski: Software Model"  + System.lineSeparator() +
			"Checking for People Who Love Automata. CAV 2013:36-52" + System.lineSeparator() +
			"[4] http://sv-comp.sosy-lab.org/2016/" + System.lineSeparator() +
			"[5] Dirk Beyer: Reliable and Reproducible Competition Results with BenchExec" + System.lineSeparator() +
			"and Witnesses (Report on SV-COMP 2016). TACAS 2016: 887-904" + System.lineSeparator() +
			"[6] https://github.com/dbeyer/sv-benchmarks" + System.lineSeparator() +
			"" + System.lineSeparator() +
			"|)" + System.lineSeparator();

}
