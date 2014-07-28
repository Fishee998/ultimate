package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.logic.Util;
import de.uni_freiburg.informatik.ultimate.logic.simplification.SimplifyDDA;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.ModelCheckerUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.PartialQuantifierElimination.EqualityInformation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayUpdate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayUpdate.ArrayUpdateException;
import de.uni_freiburg.informatik.ultimate.util.ToolchainCanceledException;

/**
 * 
 * TODO: - elimination and output of remaining vars - nested store - storeTo and
 * storeFrom (LBE) - index equality testing - documentation
 * 
 * @author Matthias Heizmann
 * 
 */
public class ElimStore3 {

	private final int mQuantifier;
	private final Script mScript;
	private final IUltimateServiceProvider mServices;
	private final Logger mLogger;

	public ElimStore3(Script script, IUltimateServiceProvider services) {
		super();
		mQuantifier = QuantifiedFormula.EXISTS;
		mScript = script;
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(ModelCheckerUtils.sPluginID);
	}

	private MultiDimensionalStore getArrayStore(Term array, Term term) {
		List<MultiDimensionalStore> all = MultiDimensionalStore.extractArrayStoresDeep(term);
		MultiDimensionalStore result = null;
		for (MultiDimensionalStore asd : all) {
			if (asd.getArray().equals(array)) {
				if (result != null && !result.equals(asd)) {
					throw new UnsupportedOperationException("unsupported: several stores");
				} else {
					result = asd;
				}
			}
		}
		return result;
	}

	// private ArrayStoreDef getArrayStore(Term array, Term term) {
	// Set<ApplicationTerm> storeTerms =
	// (new ApplicationTermFinder("store", true)).findMatchingSubterms(term);
	// ArrayStoreDef result = null;
	// for (Term storeTerm : storeTerms) {
	// ArrayStoreDef asd;
	// try {
	// asd = new ArrayStoreDef(storeTerm);
	// } catch (MultiDimensionalSelect.ArrayReadException e) {
	// throw new UnsupportedOperationException("unexpected store term");
	// }
	// if (asd.getArray().equals(array)) {
	// if (result != null) {
	// throw new UnsupportedOperationException("unsupported: several stores");
	// } else {
	// result = asd;
	// }
	// }
	// }
	// return result;
	// }

	public Term elim(TermVariable oldArr, Term term, final Set<TermVariable> newAuxVars) {
		ArrayUpdate writeInto = null;
		ArrayUpdate writtenFrom = null;
		Term[] conjuncts;
		Term othersT;

		while (true) {
			assert oldArr.getSort().isArraySort();
			if (!mServices.getProgressMonitorService().continueProcessing()) {
				throw new ToolchainCanceledException();
			}
			conjuncts = SmtUtils.getConjuncts(term);

			MultiDimensionalStore store = getArrayStore(oldArr, term);

			HashSet<Term> others = new HashSet<Term>();

			for (Term conjunct : conjuncts) {
				try {
					ArrayUpdate au = new ArrayUpdate(conjunct);
					if (au.getOldArray().equals(oldArr)) {
						if (writeInto != null) {
							throw new UnsupportedOperationException("unsupported: write into several arrays");
						}
						writeInto = au;
						if (au.getNewArray().equals(oldArr)) {
							throw new UnsupportedOperationException("unsupported: self update");
						}
					} else if (au.getNewArray().equals(oldArr)) {
						if (writtenFrom != null) {
							throw new UnsupportedOperationException("unsupported: written from several arrayas");
						}
						writtenFrom = au;
						others.add(conjunct);
					} else {
						others.add(conjunct);
					}
				} catch (ArrayUpdateException e) {
					others.add(conjunct);
				}
			}
			if (writtenFrom != null) {
				throw new UnsupportedOperationException("not yet implemented: written from");
			}

			othersT = Util.and(mScript, others.toArray(new Term[0]));

			if (store != null && writeInto == null) {
				TermVariable auxArray = oldArr.getTheory().createFreshTermVariable("arrayElim", oldArr.getSort());
				Map<Term, Term> auxMap = Collections.singletonMap((Term) store.getStoreTerm(), (Term) auxArray);
				SafeSubstitution subst = new SafeSubstitution(mScript, auxMap);
				Term auxTerm = subst.transform(term);
				Term auxVarDef = mScript.term("=", auxArray, store.getStoreTerm());
				auxTerm = Util.and(mScript, auxTerm, auxVarDef);
				Set<TermVariable> auxAuxVars = new HashSet<TermVariable>();
				Term auxRes = elim(oldArr, auxTerm, newAuxVars);

				term = auxRes;
				oldArr = auxArray;
				newAuxVars.addAll(auxAuxVars);
			} else {
				break;
			}
		}

		boolean write = (writeInto != null);

		Script script = mScript;
		;
		IndicesAndValues iav = new IndicesAndValues(oldArr, conjuncts);

		SafeSubstitution subst = new SafeSubstitution(script, iav.getMapping());

		Term intermediateResult = subst.transform(othersT);
		if (write) {
			ArrayList<Term> additionalConjuncsFromStore = new ArrayList<Term>();
			for (int i = 0; i < iav.getIndices().length; i++) {
				Term newSelect = SmtUtils.multiDimensionalSelect(mScript, writeInto.getNewArray(), iav.getIndices()[i]);
				IndexValueConnection ivc = new IndexValueConnection(iav.getIndices()[i], writeInto.getIndex(),
						iav.getValues()[i], newSelect, false);
				Term conjunct = ivc.getTerm();
				additionalConjuncsFromStore.add(conjunct);
				if (ivc.indexInequality() && !ivc.valueEquality()) {
					assert !ivc.valueInequality() : "term would be false!";
					// case where we have valueEquality hat is not true
					// do something useful...
					// e.g., mark newSelect as occurring or mark auxVar as
					// equal to something
				}
			}
			Term newConjunctsFromStore = subst.transform(Util.and(script,
					additionalConjuncsFromStore.toArray(new Term[0])));
			Term newData = subst.transform(writeInto.getValue());
			Term newWriteIndex[] = SmtUtils.substitutionElementwise(writeInto.getIndex(), subst);
			Term writeSubstituent = mScript.term("=",
					SmtUtils.multiDimensionalSelect(mScript, writeInto.getNewArray(), newWriteIndex), newData);
			intermediateResult = Util.and(mScript, intermediateResult, writeSubstituent, newConjunctsFromStore);
		}

		ArrayList<Term> additionalConjuncsFromSelect = new ArrayList<Term>();
		{
			Term[][] indices = new Term[iav.getIndices().length][];
			Term[] values = new Term[iav.getIndices().length];
			for (int i = 0; i < iav.getIndices().length; i++) {
				indices[i] = SmtUtils.substitutionElementwise(iav.getIndices()[i], subst);
				values[i] = subst.transform(iav.getValues()[i]);
			}

			for (int i = 0; i < indices.length; i++) {
				Term newConjunct = indexValueConnections(indices[i], values[i], indices, values, i + 1, script);
				additionalConjuncsFromSelect.add(newConjunct);
			}
		}
		Term newConjunctsFromSelect = Util.and(mScript, additionalConjuncsFromSelect.toArray(new Term[0]));
		Term result = Util.and(script, intermediateResult, newConjunctsFromSelect);

		result = (new SimplifyDDA(script)).getSimplifiedTerm(result);
		newAuxVars.addAll(iav.getNewAuxVars());

		return result;
	}

	public static Term indexValueConnections(Term[] ourIndex, Term ourValue, Term[][] othersIndices,
			Term[] othersValues, int othersPosition, Script script) {
		assert othersIndices.length == othersValues.length;
		ArrayList<Term> additionalConjuncs = new ArrayList<Term>();
		for (int i = othersPosition; i < othersIndices.length; i++) {
			Term[] othersIndex = othersIndices[i];
			assert ourIndex.length == othersIndex.length;
			Term indexEquality = Util.and(script, buildPairwiseEquality(ourIndex, othersIndices[i], null, script));
			Term valueEquality = SmtUtils.binaryEquality(script, ourValue, othersValues[i]);
			Term conjunct = Util.or(script, Util.not(script, indexEquality), valueEquality);
			additionalConjuncs.add(conjunct);
		}
		Term result = Util.and(script, additionalConjuncs.toArray(new Term[0]));
		return result;
	}

	private class IndicesAndValues {
		private final Term m_SelectTerm[];
		private final Term m_Indices[][];
		private final Term m_Values[];
		private final Set<TermVariable> m_NewAuxVars;
		private final Map<Term, Term> m_SelectTerm2Value = new HashMap<Term, Term>();

		public IndicesAndValues(TermVariable array, Term[] conjuncts) {
			Term term = Util.and(mScript, conjuncts);
			// FIXME: once I introduced Objects for the Index (or we use Lists)
			// this set can be removed.
			Set<MultiDimensionalSelect> set = new HashSet<MultiDimensionalSelect>();
			for (MultiDimensionalSelect mdSelect : MultiDimensionalSelect.extractSelectDeep(term, false)) {
				if (mdSelect.getArray().equals(array)) {
					set.add(mdSelect);
				}
			}
			MultiDimensionalSelect[] arrayReads = set.toArray(new MultiDimensionalSelect[0]);
			m_SelectTerm = new Term[arrayReads.length];
			m_Indices = new Term[arrayReads.length][];
			m_Values = new Term[arrayReads.length];
			m_NewAuxVars = new HashSet<TermVariable>();
			for (int i = 0; i < arrayReads.length; i++) {
				m_SelectTerm[i] = arrayReads[i].getSelectTerm();
				m_Indices[i] = arrayReads[i].getIndex();
				EqualityInformation eqInfo = PartialQuantifierElimination.getEqinfo(mScript,
						arrayReads[i].getSelectTerm(), conjuncts, array, mQuantifier, mLogger);
				if (eqInfo == null) {
					Term select = arrayReads[i].getSelectTerm();
					TermVariable auxVar = array.getTheory().createFreshTermVariable("arrayElim", select.getSort());
					m_NewAuxVars.add(auxVar);
					m_Values[i] = auxVar;
				} else {
					m_Values[i] = eqInfo.getTerm();
				}
				m_SelectTerm2Value.put(m_SelectTerm[i], m_Values[i]);
			}
		}

		public Term[] getSelectTerm() {
			return m_SelectTerm;
		}

		public Term[][] getIndices() {
			return m_Indices;
		}

		public Term[] getValues() {
			return m_Values;
		}

		public Set<TermVariable> getNewAuxVars() {
			return m_NewAuxVars;
		}

		public Map<Term, Term> getMapping() {
			return m_SelectTerm2Value;
		}
	}

	private class IndexValueConnection {
		private final Term[] m_fstIndex;
		private final Term[] m_sndIndex;
		private final Term m_fstValue;
		private final Term m_sndValue;
		private final boolean m_SelectConnection;
		private final Term m_IndexEquality;
		private final Term m_ValueEquality;

		public IndexValueConnection(Term[] fstIndex, Term[] sndIndex, Term fstValue, Term sndValue,
				boolean selectConnection) {
			m_fstIndex = fstIndex;
			m_sndIndex = sndIndex;
			m_fstValue = fstValue;
			m_sndValue = sndValue;
			m_SelectConnection = selectConnection;
			m_IndexEquality = Util.and(mScript, buildPairwiseEquality(fstIndex, sndIndex, null, mScript));
			m_ValueEquality = SmtUtils.binaryEquality(mScript, fstValue, sndValue);
		}

		/**
		 * Is equality of both indices already implied by context?
		 */
		public boolean indexEquality() {
			return m_IndexEquality.equals(mScript.term("true"));
		}

		/**
		 * Is inequality of both indices already implied by context?
		 */
		public boolean indexInequality() {
			return m_IndexEquality.equals(mScript.term("false"));
		}

		/**
		 * Is equality of both values already implied by context?
		 */
		public boolean valueEquality() {
			return m_ValueEquality.equals(mScript.term("true"));
		}

		/**
		 * Is inequality of both values already implied by context?
		 */
		public boolean valueInequality() {
			return m_ValueEquality.equals(mScript.term("false"));
		}

		public Term getTerm() {
			Term indexTerm = m_IndexEquality;
			if (m_SelectConnection) {
				indexTerm = Util.not(mScript, indexTerm);
			}
			return Util.or(mScript, indexTerm, m_ValueEquality);

		}
	}

	/**
	 * Return true if this is a nested select on arr. Throws exception if an
	 * index contains a select.
	 */
	private boolean isMultiDimensionalSelect(Term term, Term arr, int dimension) {
		Term subterm = term;
		for (int i = 0; i < dimension; i++) {
			if (!(term instanceof ApplicationTerm)) {
				return false;
			}
			ApplicationTerm subtermApp = (ApplicationTerm) subterm;
			if (!subtermApp.getFunction().getName().equals("select")) {
				return false;
			}
			subterm = subtermApp.getParameters()[0];
			Term index = subtermApp.getParameters()[1];
			Set<ApplicationTerm> selectTermsInIndex = (new ApplicationTermFinder("select", true))
					.findMatchingSubterms(index);
			if (!selectTermsInIndex.isEmpty()) {
				throw new UnsupportedOperationException("select in index not supported");
			}
		}
		return subterm.equals(arr);
	}

	// /**
	// * Return all selectTerms that read from the array given by arrayTv.
	// * @param selectTerms a[i],
	// * @return
	// */
	// private static Map<Term[], MultiDimensionalSelect> getArrayReads(
	// TermVariable arrayTv,
	// Set<ApplicationTerm> selectTerms) {
	// Map<Term[],MultiDimensionalSelect> index2mdSelect =
	// new HashMap<Term[],MultiDimensionalSelect>();
	// for (ApplicationTerm selectTerm : selectTerms) {
	// if (selectTerm.getFunction().getReturnSort().isArraySort()) {
	// // this is only a select nested in some other select or store
	// continue;
	// }
	// MultiDimensionalSelect ar = new MultiDimensionalSelect(selectTerm);
	// if (ar.getArray() == arrayTv) {
	// index2mdSelect.put(ar.getIndex(), ar);
	// } else {
	// // select on different array
	// continue;
	// }
	// }
	// return index2mdSelect;
	// }

	/**
	 * Given two lists of terms and a subsitution subst return the following
	 * conjunctions subst(first_1) == subst(second_1), ... ,subst(first_n) ==
	 * subst(second_n) if subst is null we use the identity function.
	 */
	static Term[] buildPairwiseEquality(Term[] first, Term[] second, SafeSubstitution subst, Script script) {
		assert first.length == second.length;
		Term[] equivalent = new Term[first.length];
		for (int i = 0; i < first.length; i++) {
			Term firstTerm, secondTerm;
			if (subst == null) {
				firstTerm = first[i];
				secondTerm = second[i];
			} else {
				firstTerm = subst.transform(first[i]);
				secondTerm = subst.transform(second[i]);
			}
			equivalent[i] = SmtUtils.binaryEquality(script, firstTerm, secondTerm);
		}
		return equivalent;
	}

	/**
	 * assert term, replace TermVariable by constants in advance, replace by
	 * constants defined by mapping, if no constant defined by mapping declare
	 * constant and add to mapping
	 */
	public void assertTermWithTvs(Map<TermVariable, Term> mapping, Script script, Term term) {
		for (TermVariable tv : term.getFreeVars()) {
			if (!mapping.containsKey(tv)) {
				String name = "arrayElim_" + tv.getName();
				script.declareFun(name, new Sort[0], tv.getSort());
				Term constant = script.term(name);
				mapping.put(tv, constant);
			}
		}
		Term renamed = (new Substitution(mapping, script)).transform(term);
		mScript.assertTerm(renamed);
	}

	// public static int getDimension(Sort sort) {
	// if (sort.isArraySort()) {
	// Sort[] arg = sort.getArguments();
	// assert arg.length == 2;
	// return 1 + getDimension(arg[1]);
	// } else {
	// return 0;
	// }
	// }

}
