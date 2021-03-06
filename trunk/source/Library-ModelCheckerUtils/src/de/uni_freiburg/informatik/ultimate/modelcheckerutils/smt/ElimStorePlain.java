/*
 * Copyright (C) 2017 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import de.uni_freiburg.informatik.ultimate.core.lib.exceptions.ToolchainCanceledException;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Script.LBool;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.ModelCheckerUtils;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.ElimStore3.IndicesAndValues;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SmtUtils.SimplificationTechnique;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayIndex;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayUpdate;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.ArrayUpdate.ArrayUpdateExtractor;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSelect;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalSort;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.arrays.MultiDimensionalStore;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.equalityanalysis.EqualityAnalysisResult.Equality;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.equalityanalysis.IncrementalEqualityAnalyzer;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.equalityanalysis.ThreeValuedEquivalenceRelation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.PrenexNormalForm;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.QuantifierPusher;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.QuantifierPusher.PqeTechniques;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.QuantifierSequence;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.linearTerms.QuantifierSequence.QuantifiedVariables;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.util.ConstructionCache;
import de.uni_freiburg.informatik.ultimate.util.ConstructionCache.IValueConstruction;
import de.uni_freiburg.informatik.ultimate.util.LexicographicCounter;
import de.uni_freiburg.informatik.ultimate.util.datastructures.Doubleton;
import de.uni_freiburg.informatik.ultimate.util.datastructures.UnionFind;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.HashRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.Pair;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.SymmetricHashRelation;
import de.uni_freiburg.informatik.ultimate.util.datastructures.relation.TreeRelation;

/**
 *
 * 
 *
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 *
 */
public class ElimStorePlain {

	private final int mQuantifier;
	private final Script mScript;
	private final ManagedScript mMgdScript;
	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final SimplificationTechnique mSimplificationTechnique;
	private final static String s_AUX_VAR_NEW_ARRAY = "arrayElimArr";
	private final static String s_AUX_VAR_INDEX = "arrayElimIndex";
	private final static String s_AUX_VAR_ARRAYCELL = "arrayElimCell";

	public ElimStorePlain(final ManagedScript mgdScript, final IUltimateServiceProvider services,
			final SimplificationTechnique simplificationTechnique, final int quantifier) {
		super();
		mQuantifier = quantifier;
		mScript = mgdScript.getScript();
		mMgdScript = mgdScript;
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(ModelCheckerUtils.PLUGIN_ID);
		mSimplificationTechnique = simplificationTechnique;
	}
	
	
	
	
	public Pair<Term, Collection<TermVariable>> elimAll(final Set<TermVariable> inputEliminatees, final Term inputTerm) {
		
		final Stack<AfEliminationTask> taskStack = new Stack();
		final ArrayList<Term> resultDisjuncts = new ArrayList<>();
		final Set<TermVariable> resultEliminatees = new LinkedHashSet<>();
		{
			final AfEliminationTask eliminationTask = new AfEliminationTask(inputEliminatees, inputTerm);
			pushTaskOnStack(eliminationTask, taskStack);
		}
		int numberOfRounds = 0;
		while (!taskStack.isEmpty()) {
			final AfEliminationTask currentEliminationTask = taskStack.pop();
			final TreeRelation<Integer, TermVariable> tr = classifyEliminatees(currentEliminationTask.getEliminatees());
			
			
			final LinkedHashSet<TermVariable> arrayEliminatees = getArrayTvSmallDimensionsFirst(tr);
			
			
			if (arrayEliminatees.isEmpty()) {
				// no array eliminatees left
				resultDisjuncts.add(currentEliminationTask.getTerm());
				resultEliminatees.addAll(currentEliminationTask.getEliminatees());
			} else {
				TermVariable thisIterationEliminatee;
				{
					final Iterator<TermVariable> it = arrayEliminatees.iterator();
					thisIterationEliminatee = it.next();
					it.remove();
				}
			
				final AfEliminationTask ssdElimRes = elim1(thisIterationEliminatee, currentEliminationTask.getTerm());
				arrayEliminatees.addAll(ssdElimRes.getEliminatees());
				// also add non-array eliminatees
				arrayEliminatees.addAll(tr.getImage(0));
				final AfEliminationTask eliminationTask1 = new AfEliminationTask(arrayEliminatees, ssdElimRes.getTerm());
				final AfEliminationTask eliminationTask2 = applyNonSddEliminations(eliminationTask1);
				if (mLogger.isInfoEnabled()) {
					mLogger.info("Start of round: " + printVarInfo(tr) + " End of round: " + 
							printVarInfo(classifyEliminatees(eliminationTask2.getEliminatees())) + " and " + 
							PartialQuantifierElimination.getXjunctsOuter(mQuantifier, eliminationTask2.getTerm()).length + " xjuncts.");
				}
				
				pushTaskOnStack(eliminationTask2, taskStack);
			}
			numberOfRounds++;
		}
		mLogger.info("Needed " + numberOfRounds + " rounds to eliminate " + inputEliminatees.size() + " variables, produced " + resultDisjuncts.size() + " xjuncts");
		// return term and variables that we could not eliminate
		return new Pair<>(PartialQuantifierElimination.applyCorrespondingFiniteConnective(mScript, mQuantifier, resultDisjuncts), resultEliminatees);
	}




	private String printVarInfo(final TreeRelation<Integer, TermVariable> tr) {
		final StringBuilder sb = new StringBuilder();
		for (final Integer dim : tr.getDomain()) {
			sb.append(tr.getImage(dim).size() + " dim-" + dim + " vars, ");
		}
		return sb.toString();
	}




	private void pushTaskOnStack(final AfEliminationTask eliminationTask, final Stack<AfEliminationTask> taskStack) {
		final Term term = eliminationTask.getTerm();
		final Term[] disjuncts = PartialQuantifierElimination.getXjunctsOuter(mQuantifier, term);
		if (disjuncts.length == 1) {
			taskStack.push(eliminationTask);
		} else {
			for (final Term disjunct : disjuncts) {
				taskStack.push(new AfEliminationTask(eliminationTask.getEliminatees(), disjunct));
			}
		}
	}




	private AfEliminationTask applyNonSddEliminations(final AfEliminationTask eliminationTask) throws AssertionError {
		final Term quantified = SmtUtils.quantifier(mScript, mQuantifier, eliminationTask.getEliminatees(), eliminationTask.getTerm());
		final Term pushed = new QuantifierPusher(mMgdScript, mServices, true, PqeTechniques.ALL_LOCAL).transform(quantified);

		final Term pnf = new PrenexNormalForm(mMgdScript).transform(pushed);
		final QuantifierSequence qs = new QuantifierSequence(mMgdScript.getScript(), pnf);
		final Term matrix = qs.getInnerTerm();
		final List<QuantifiedVariables> qvs = qs.getQuantifierBlocks();

		final Set<TermVariable> eliminatees1;
		if (qvs.size() == 0) {
			eliminatees1 = Collections.emptySet();
		} else if (qvs.size() == 1) {
			eliminatees1 = qvs.get(0).getVariables();
		} else if (qvs.size() > 1) {
			throw new UnsupportedOperationException("alternation not yet supported");
		} else {
			throw new AssertionError();
		}
		return new AfEliminationTask(eliminatees1, matrix);
	}




	private LinkedHashSet<TermVariable> getArrayTvSmallDimensionsFirst(final TreeRelation<Integer, TermVariable> tr) {
		final LinkedHashSet<TermVariable> result = new LinkedHashSet<>();
		for (final Integer dim : tr.getDomain()) {
			if (dim != 0) {
				result.addAll(tr.getImage(dim));
			}
		}
		return result;
	}


	private TreeRelation<Integer, TermVariable> classifyEliminatees(final Collection<TermVariable> eliminatees) {
		final TreeRelation<Integer, TermVariable> tr = new TreeRelation<>();
		for (final TermVariable eliminatee : eliminatees) {
			final MultiDimensionalSort mds = new MultiDimensionalSort(eliminatee.getSort());
			tr.addPair(mds.getDimension(), eliminatee);
		}
		return tr;
	}

	public AfEliminationTask elim1(final TermVariable eliminatee, final Term inputTerm) {
		final List<MultiDimensionalStore> stores = extractStores(eliminatee, inputTerm);
		if (stores.size() > 1) {
			throw new AssertionError("not yet supported");
		}
		final List<MultiDimensionalSelect> selects = extractSelects(eliminatee, inputTerm);
		
		checkForUnsupportedSelfUpdate(eliminatee, inputTerm, mQuantifier);
		
		
		final List<ApplicationTerm> selectTerms = extractSelects2(eliminatee, inputTerm);
		
		
		if (false && stores.isEmpty()) {
			if (!selectTerms.isEmpty()) {
				final IndicesAndValues iav = new IndicesAndValues(mMgdScript, mQuantifier, eliminatee, inputTerm);
				final Pair<List<ArrayIndex>, List<Term>> indicesAndValues = ElimStore3.buildIndicesAndValues(mMgdScript, iav);

				final ArrayList<Term> indexValueConstraintsFromEliminatee = ElimStore3.constructIndexValueConstraints(
						mMgdScript.getScript(), mQuantifier, indicesAndValues.getFirst(), indicesAndValues.getSecond());
				final Term indexValueConstraints = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier,
						indexValueConstraintsFromEliminatee);
				final Substitution subst = new SubstitutionWithLocalSimplification(mMgdScript, iav.getMapping());
				final Term replaced = subst.transform(inputTerm);
				final Term result = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier, Arrays.asList(new Term[] { replaced, indexValueConstraints }));
				assert !Arrays.asList(result.getFreeVars()).contains(eliminatee) : "var is still there";
				return new AfEliminationTask(iav.getNewAuxVars(), result);
			} else {
				throw new AssertionError("no case applies");
			}
		}
		
		final Term storeTerm;
		final Term storeIndex;
		final Term storeValue;
		if (stores.isEmpty()) {
			storeTerm = null;
			storeIndex = null;
			storeValue = null;
//			mLogger.info("store-free iteration");
		} else {
			final MultiDimensionalStore store = stores.iterator().next();
			storeTerm = store.getStoreTerm();
			storeIndex = store.getIndex().get(0);
			storeValue = ((ApplicationTerm) storeTerm).getParameters()[2];
//			mLogger.info("eliminating store to array of dimension " + new MultiDimensionalSort(eliminatee.getSort()).getDimension());
		}
		
		final Set<Term> indices = new HashSet<>();
		if (!stores.isEmpty()) {
			indices.add(storeIndex);
		}
		for (final ApplicationTerm entry : selectTerms) {
			indices.add(getIndexOfSelect(entry));
		}
		final ThreeValuedEquivalenceRelation<Term> inputEqualityInformation = new ThreeValuedEquivalenceRelation<>();
		final Pair<ThreeValuedEquivalenceRelation<Term>, List<Term>> analysisResult = analyzeIndexEqualities(indices, inputTerm, inputEqualityInformation);
		final ThreeValuedEquivalenceRelation<Term> equalityInformation = analysisResult.getFirst();
		
		
		final Set<TermVariable> newAuxVars = new LinkedHashSet<>();
		
		final Sort valueSort = eliminatee.getSort().getArguments()[1];
		final Map<Term, TermVariable> oldCellMapping = constructOldCellValueMapping(selectTerms, storeIndex, equalityInformation, valueSort);
		for (final Entry<?, TermVariable> entry : oldCellMapping.entrySet()) {
			newAuxVars.add(entry.getValue());
		}
		
		final Map<Term, TermVariable> indexMapping = constructIndexReplacementMapping(indices, eliminatee, equalityInformation);
		final List<Term> indexMappingDefinitions = new ArrayList<>();
		for (final Entry<Term, TermVariable> entry : indexMapping.entrySet()) {
			indexMappingDefinitions.add(PartialQuantifierElimination.equalityForExists(mScript, mQuantifier, entry.getValue(), entry.getKey()));	
		}
		for (final Entry<?, TermVariable> entry : indexMapping.entrySet()) {
			newAuxVars.add(entry.getValue());
		}
		
		final Term notEqualsDetectedBySolver = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier, analysisResult.getSecond());
		final Term indexDefinitionsTerm = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier, indexMappingDefinitions);
		final Term term = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier,
				Arrays.asList(new Term[] { indexDefinitionsTerm, inputTerm, notEqualsDetectedBySolver}));
		
		final TermVariable newAuxArray =
				mMgdScript.constructFreshTermVariable(s_AUX_VAR_NEW_ARRAY, eliminatee.getSort());
		newAuxVars.add(newAuxArray) ;
		
		final List<Term> disjuncts = new ArrayList<>();
		final CombinationIterator2 ci = new CombinationIterator2(indices, equalityInformation);
//		mLogger.info("Considering " + ci.size() + " cases while eliminating array variable of dimension " + new MultiDimensionalSort(eliminatee.getSort()).getDimension());
		for (final Set<Doubleton<Term>> equalDoubletons : ci) {
			final Map<Term, Term> substitutionMapping = new HashMap<>();
			if (!stores.isEmpty()) {
				substitutionMapping.put(storeTerm, newAuxArray);
			}
			final List<Term> indexEqualityTerms = new ArrayList<>();
			final List<Term> valueEqualityTerms = new ArrayList<>();
			for (final Doubleton<Term> doubleton : buildListOfNonDisjointDoubletons(indices, equalityInformation)) {
				final Term indexEqualityTerm;
				if (equalDoubletons.contains(doubleton)) {
					indexEqualityTerm = PartialQuantifierElimination.equalityForExists(mScript, mQuantifier,
							getNewIndex(doubleton.getOneElement(), indexMapping, eliminatee),
							getNewIndex(doubleton.getOtherElement(), indexMapping, eliminatee));
					final Term oneOldCellVariable = oldCellMapping.get(doubleton.getOneElement());
					final Term otherOldCellVariable = oldCellMapping.get(doubleton.getOtherElement());
					if (oneOldCellVariable != null && otherOldCellVariable != null) {
						final Term valueEqualityTerm = PartialQuantifierElimination.equalityForExists(mScript, mQuantifier,oneOldCellVariable, otherOldCellVariable);
						valueEqualityTerms.add(valueEqualityTerm);
					}
				} else {
					indexEqualityTerm = PartialQuantifierElimination.notEqualsForExists(mScript, mQuantifier,
							getNewIndex(doubleton.getOneElement(), indexMapping, eliminatee),
							getNewIndex(doubleton.getOtherElement(), indexMapping, eliminatee));
				}
				indexEqualityTerms.add(indexEqualityTerm);
			}
			
			for (final ApplicationTerm selectTerm : selectTerms) {
				final Term indexOfSelect = getIndexOfSelect(selectTerm); 
				final boolean selectIndexEquivalentToStoreIndex;
				if (stores.isEmpty()) {
					selectIndexEquivalentToStoreIndex = true;
				} else {
					selectIndexEquivalentToStoreIndex = 
						(equalityInformation.getEquality(indexOfSelect, storeIndex) == Equality.EQUAL) || 
						equalDoubletons.contains(new Doubleton<Term>(indexOfSelect, storeIndex)) ||  
						equalDoubletons.contains(new Doubleton<Term>(storeIndex, indexOfSelect));
				}
				if (selectIndexEquivalentToStoreIndex) {
					final Term oldCell = oldCellMapping.get(indexOfSelect);
					assert oldCell != null : "no oldcell for " + indexOfSelect;
					substitutionMapping.put(selectTerm, oldCell);
				} else {
					final Term newSelect = constructNewSelectWithPossiblyReplacedIndex(newAuxArray, selectTerm, indexMapping, eliminatee);
					assert !Arrays.asList(newSelect.getFreeVars()).contains(eliminatee) : "var is still there: " + eliminatee;
					substitutionMapping.put(selectTerm, newSelect);
				}
			}
			
			final Term storedValueInformation;
			if (stores.isEmpty()) {
				if (mQuantifier == QuantifiedFormula.EXISTS) {
					storedValueInformation = mScript.term("true");
				} else if (mQuantifier == QuantifiedFormula.FORALL) {
					storedValueInformation = mScript.term("false");
				} else {
					throw new AssertionError();
				}
			} else {
				storedValueInformation = PartialQuantifierElimination.equalityForExists(mScript, mQuantifier,
					mScript.term("select", newAuxArray, getNewIndex(storeIndex, indexMapping, eliminatee)), 
					new SubstitutionWithLocalSimplification(mMgdScript, substitutionMapping).transform(storeValue));
			}
			
			final Term transformedTerm = new SubstitutionWithLocalSimplification(mMgdScript, substitutionMapping).transform(term);
			final Term indexEqualityTerm = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier, indexEqualityTerms);
			final Term valueEqualityTerm = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier, valueEqualityTerms);
			
			final Term disjuct = PartialQuantifierElimination.applyDualFiniteConnective(mScript, mQuantifier,
					indexEqualityTerm, valueEqualityTerm, transformedTerm, storedValueInformation);
			assert !Arrays.asList(disjuct.getFreeVars()).contains(eliminatee) : "var is still there: " + eliminatee
					+ " term size " + new DagSizePrinter(disjuct);
			if (mQuantifier == QuantifiedFormula.EXISTS) {
				final LBool sat = SmtUtils.checkSatTerm(mScript, disjuct);
				if (sat == LBool.UNSAT) {
					mLogger.info("saved disjunct");
					if (ci.size() == 1) {
						final LBool inputsat = SmtUtils.checkSatTerm(mScript, inputTerm);
						throw new AssertionError("input must have been unsat " + " term size " + new DagSizePrinter(inputTerm));
					}
					continue;
				}
			}
			disjuncts.add(disjuct);
			
		}

		final Term result = PartialQuantifierElimination.applyCorrespondingFiniteConnective(mScript, mQuantifier, disjuncts);
		if (Arrays.asList(result.getFreeVars()).contains(eliminatee)) {
			throw new AssertionError("var is still there " + eliminatee + "  quantifier " + result + "  term size "
					+ (new DagSizePrinter(term)) + "   " + term);
		}
		return new AfEliminationTask(newAuxVars, result);
		
	}




	private Pair<ThreeValuedEquivalenceRelation<Term>, List<Term>> analyzeIndexEqualities(final Set<Term> indices,
			final Term inputTerm, final ThreeValuedEquivalenceRelation<Term> inputTveR) {
		final Term[] dualFiniteJunctsArray = PartialQuantifierElimination.getXjunctsInner(mQuantifier, inputTerm);
		// TODO: bring each in commuhash normal form
		final Set<Term> dualFiniteJuncts = new HashSet<>(Arrays.asList(dualFiniteJunctsArray));

		final ThreeValuedEquivalenceRelation<Term> tver = new ThreeValuedEquivalenceRelation<>(inputTveR);
		for (final Term index : indices) {
			tver.addElement(index);
		}

		final List<Term> notEqualsDetectedViaSolver = new ArrayList<>();
		final ArrayList<Term> indicesList = new ArrayList<Term>(indices);
		// TODO: filter non-represenatives not only during iterations but also in advance
		final IncrementalEqualityAnalyzer iea = new IncrementalEqualityAnalyzer(mMgdScript, inputTerm);
		for (int i = 0; i < indicesList.size(); i++) {
			if (!tver.isRepresentative(indicesList.get(i))) {
				continue;
			}
			for (int j = i + 1; j < indicesList.size(); j++) {
				if (!tver.isRepresentative(indicesList.get(j))) {
					continue;
				}
				final Term eq = SmtUtils.binaryEquality(mScript, indicesList.get(i), indicesList.get(j));
				if (SmtUtils.isTrue(eq)) {
					tver.reportEquality(indicesList.get(i), indicesList.get(j));
				} else if (SmtUtils.isFalse(eq)) {
					tver.reportNotEquals(indicesList.get(i), indicesList.get(j));
				} else {
					// TODO: bring eq in commuhash normal form
					if (dualFiniteJuncts.contains(eq)) {
						tver.reportEquality(indicesList.get(i), indicesList.get(j));
						mLogger.info("found equality in dual finite juncts");
					} else {
						if (dualFiniteJuncts.contains(SmtUtils.not(mScript, eq))) {
							tver.reportNotEquals(indicesList.get(i), indicesList.get(j));
							mLogger.info("found not equals in dual finite juncts");
						} else {
							final Equality isEqual = iea.checkEquality(indicesList.get(i), indicesList.get(j));
							switch (isEqual) {
							case EQUAL:
								tver.reportEquality(indicesList.get(i), indicesList.get(j));
								mLogger.info("detected equality via solver");
								break;
							case NOT_EQUAL:
								tver.reportNotEquals(indicesList.get(i), indicesList.get(j));
								mLogger.info("detected not equals via solver");
								notEqualsDetectedViaSolver.add(SmtUtils.not(mScript, eq));
								break;
							case UNKNOWN:
								// do nothing
								break;
							default:
								throw new AssertionError();
							}
						}
					}
				}
			}
		}
		iea.unlockSolver();
		return new Pair<ThreeValuedEquivalenceRelation<Term>, List<Term>>(tver, notEqualsDetectedViaSolver);
	}






	private void checkForUnsupportedSelfUpdate(final TermVariable eliminatee, final Term inputTerm,
			final int quantifier) {
		final Set<ApplicationTerm> equalities = new ApplicationTermFinder("=", false).findMatchingSubterms(inputTerm);
		final ArrayUpdateExtractor aue = new ArrayUpdateExtractor(quantifier == QuantifiedFormula.FORALL, false,
				equalities.toArray(new Term[equalities.size()]));
		for (final ArrayUpdate au : aue.getArrayUpdates()) {
			if (au.getNewArray().equals(eliminatee)) {
				if (Arrays.asList(au.getMultiDimensionalStore().getStoreTerm().getFreeVars()).contains(eliminatee)) {
					throw new UnsupportedOperationException("Want to eliminate " + eliminatee
							+ " but encountered yet unsupported self-update " + au.getArrayUpdateTerm());
				}
			}
		}
	}


	/**
	 * Let eliminatee be the array that is eliminated and (select eliminatee idx
	 * v) a select term. If idx contains eliminatee, we have to replace idx by
	 * an auxiliary variable. As an optimization, we only construct one
	 * auxiliary variable for each equivalence class of indices.
	 */
	private Map<Term, TermVariable> constructIndexReplacementMapping(final Set<Term> indices,
			final TermVariable eliminatee, final ThreeValuedEquivalenceRelation<Term> equalityInformation)
			throws AssertionError {
		final IValueConstruction<Term, TermVariable> valueConstruction = new IValueConstruction<Term, TermVariable>() {

			@Override
			public TermVariable constructValue(final Term index) {
				final TermVariable indexReplacement = mMgdScript.constructFreshTermVariable(s_AUX_VAR_INDEX,
						index.getSort());
				return indexReplacement;
			}

		};
		final ConstructionCache<Term, TermVariable> cc = new ConstructionCache<>(valueConstruction);
		final Map<Term, TermVariable> indexReplacementMapping = new HashMap<>();
		for (final Term index : indices) {
			if (Arrays.asList(index.getFreeVars()).contains(eliminatee)) {
				// need to replace index
				final Term indexRepresentative = equalityInformation.getRepresentative(index);
				final TermVariable indexReplacement = cc.getOrConstruct(indexRepresentative);
				indexReplacementMapping.put(index, indexReplacement);
			}
		}
		return indexReplacementMapping;
	}

	private void constructIndexReplacementIfNecessary(final TermVariable eliminatee, final Set<TermVariable> newAuxVars,
			final Map<Term, Term> indexMapping, final List<Term> indexMappingDefinitions, final Term index) {
		if (Arrays.asList(index.getFreeVars()).contains(eliminatee)) {
			// need to replace index
			final TermVariable newAuxIndex =
					mMgdScript.constructFreshTermVariable(s_AUX_VAR_INDEX, index.getSort());
			newAuxVars.add(newAuxIndex);
			indexMapping.put(index, newAuxIndex);
			indexMappingDefinitions.add(PartialQuantifierElimination.equalityForExists(mScript, mQuantifier, newAuxIndex, index));
		}
	}

	private Term constructNewSelectWithPossiblyReplacedIndex(final TermVariable newAuxArray,
			final ApplicationTerm oldSelectTerm, final Map<Term, TermVariable> indexMapping, final TermVariable eliminatee) {
		final Term newIndex = getNewIndex(getIndexOfSelect(oldSelectTerm), indexMapping, eliminatee);
		final Term newSelect = mMgdScript.getScript().term("select", newAuxArray, newIndex);
		return newSelect;
	}


	private Term getNewIndex(final Term originalIndex, final Map<Term, TermVariable> indexMapping, final TermVariable eliminatee) {
		final Term newIndex;
		final Term replacementIndex = indexMapping.get(originalIndex);
		if (replacementIndex == null) {
			newIndex = originalIndex;
			assert !Arrays.asList(originalIndex.getFreeVars()).contains(eliminatee);
		} else {
			newIndex = replacementIndex;
			assert Arrays.asList(originalIndex.getFreeVars()).contains(eliminatee);
		}
		return newIndex;
	}
	

	/**
	 * Let eliminatee be the array that is eliminated and (select eliminatee idx v) a select term.
	 * Construct mapping that assigns the select term an auxiliary variable
	 * the represents this array cell.
	 * If we know that the index of the store that we currently process is disjoint from idx,
	 * we do not add the auxiliary variable (the new cell will have same value as old cell).
	 * As an optimization, we only construct one auxiliary variable for each equivalence class of indices.
	 */
	private Map<Term, TermVariable> constructOldCellValueMapping(final List<ApplicationTerm> selectTerms,
			final Term storeIndex, final ThreeValuedEquivalenceRelation<Term> equalityInformation, final Sort valueSort) {
		final IValueConstruction<Term, TermVariable> valueConstruction = new IValueConstruction<Term, TermVariable>() {

			@Override
			public TermVariable constructValue(final Term index) {
				final TermVariable oldCell = mMgdScript.constructFreshTermVariable(s_AUX_VAR_ARRAYCELL, valueSort);
				return oldCell;
			}

		};
		final ConstructionCache<Term, TermVariable> cc = new ConstructionCache<>(valueConstruction);
		final Map<Term, TermVariable> oldCellMapping = new HashMap<>();
		for (final ApplicationTerm selectTerm : selectTerms) {
			assert selectTerm.getSort().equals(valueSort);
			final Term selectIndex = getIndexOfSelect(selectTerm);
			if ((storeIndex != null) && equalityInformation.getEquality(selectIndex, storeIndex) == Equality.NOT_EQUAL) {
				// do nothing
			} else {
				final Term indexRepresentative = equalityInformation.getRepresentative(selectIndex);
				final TermVariable oldCellVariable = cc
						.getOrConstruct(indexRepresentative);
				oldCellMapping.put(selectIndex, oldCellVariable);
			}
		}
		return oldCellMapping;
	}

	private void constructAndAddOldCellValueTermVariable(final Map<Term, TermVariable> oldCellMapping,
			final ApplicationTerm entry, final UnionFind<Term> indices) throws AssertionError {
		final Term indexRepresentative = indices.find(getIndexOfSelect(entry));
		TermVariable oldCell = oldCellMapping.get(indexRepresentative);
		if (oldCell == null) {
			oldCell = mMgdScript.constructFreshTermVariable(s_AUX_VAR_ARRAYCELL, entry.getSort());
			final Term oldValue = oldCellMapping.put(indexRepresentative, oldCell);
			if (oldValue != null) {
				throw new AssertionError("must not insert twice");
			}

		}
		

	}
	
	private Term getIndexOfSelect(final ApplicationTerm appTerm) {
		assert (appTerm.getParameters().length == 2) : "no select";
		assert (appTerm.getFunction().getName().equals("select")) : "no select";
		return appTerm.getParameters()[1];
	}
	
	private Term getArrayOfSelect(final ApplicationTerm appTerm) {
		assert (appTerm.getParameters().length == 2) : "no select";
		assert (appTerm.getFunction().getName().equals("select")) : "no select";
		return appTerm.getParameters()[0];
	}
	

	private List<MultiDimensionalStore> extractStores(final TermVariable eliminatee, final Term term) {
		final List<MultiDimensionalStore> result = new ArrayList<>();
		final Set<ApplicationTerm> stores = new ApplicationTermFinder("store", false).findMatchingSubterms(term);
		for (final ApplicationTerm appTerm : stores) {
			if (appTerm.getParameters()[0].equals(eliminatee)) {
				result.add(new MultiDimensionalStore(appTerm));
			}
		}
		return result;
	}
	
	private List<MultiDimensionalSelect> extractSelects(final TermVariable eliminatee, final Term term) {
		final List<MultiDimensionalSelect> result = new ArrayList<>();
		final Set<ApplicationTerm> stores = new ApplicationTermFinder("select", false).findMatchingSubterms(term);
		for (final ApplicationTerm appTerm : stores) {
			if (appTerm.getParameters()[0].equals(eliminatee)) {
				result.add(new MultiDimensionalSelect(appTerm));
			}
		}
		return result;
	}
	
	private List<ApplicationTerm> extractSelects2(final TermVariable eliminatee, final Term term) {
		final List<ApplicationTerm> result = new ArrayList<>();
		final Set<ApplicationTerm> stores = new ApplicationTermFinder("select", false).findMatchingSubterms(term);
		for (final ApplicationTerm appTerm : stores) {
			if (appTerm.getParameters()[0].equals(eliminatee)) {
				result.add(appTerm);
			}
		}
		return result;
	}
	
	
	public static <E> boolean isClosedUnderTransitivity(final HashRelation<E, E> relation) {
		for (final Entry<E, E> entry : relation.entrySet()) {
			for (final E image : relation.getImage(entry.getValue())) {
				if (!relation.containsPair(entry.getKey(), image)) {
					return false;
				}
			}
		}
		return true;
	}


	public static List<Doubleton<Term>> buildListOfNonDisjointDoubletons(final Collection<Term> indices, final ThreeValuedEquivalenceRelation<Term> equalityInformation) {
		final List<Doubleton<Term>> doubeltons = new ArrayList<>();
		final List<Term> indexList = new ArrayList<>(indices);
		for (int i = 0; i < indexList.size(); i++) {
			if (!equalityInformation.isRepresentative(indexList.get(i))) {
				continue;
			}
			for (int j = i+1; j < indexList.size(); j++) {
				if (!equalityInformation.isRepresentative(indexList.get(j))) {
					continue;
				}
				if (equalityInformation.getEquality(indexList.get(i), indexList.get(j)) == Equality.NOT_EQUAL) {
					// do nothing
				} else {
					doubeltons.add(new Doubleton<Term>(indexList.get(i), indexList.get(j)));
				}
			}
		}
		return doubeltons;
	}
	
	
	private class CombinationIterator implements Iterable<Set<Doubleton<Term>>> {

		private final List<Set<Doubleton<Term>>> mResult = new ArrayList<>();

		public CombinationIterator(final Collection<Term> indices, final ThreeValuedEquivalenceRelation<Term> equalityInformation) {
			super();
			final List<Doubleton<Term>> doubeltons = buildListOfNonDisjointDoubletons(indices, equalityInformation);

			final int[] numberOfValues = new int[doubeltons.size()];
			Arrays.fill(numberOfValues, 2);
			final LexicographicCounter lc = new LexicographicCounter(numberOfValues);

			do {
				if (!mServices.getProgressMonitorService().continueProcessing()) {
					throw new ToolchainCanceledException(this.getClass(),
							"iterating over LexicographicCounter " + lc);
				}
				final HashRelation<Term, Term> relationCandidate = new HashRelation<>();
				for (final Term index : indices) {
					if (equalityInformation.isRepresentative(index)) {
						relationCandidate.addPair(index, index);
					}
				}
				final Set<Doubleton<Term>> resultCandidate = new HashSet<>();
				for (int i = 0; i < doubeltons.size(); i++) {
					if (lc.getCurrentValue()[i] == 1) {
						final Doubleton<Term> doubleton = doubeltons.get(i);
						relationCandidate.addPair(doubleton.getOneElement(), doubleton.getOtherElement());
						relationCandidate.addPair(doubleton.getOtherElement(), doubleton.getOneElement());
						resultCandidate.add(doubleton);
					}
				}
				if (isClosedUnderTransitivity(relationCandidate)) {
					mResult.add(resultCandidate);
				}

				lc.increment();
			} while (!lc.isZero());
		}

		public int size() {
			return mResult.size();
		}

		@Override
		public Iterator<Set<Doubleton<Term>>> iterator() {
			return mResult.iterator();
		}

	}
	
	/**
	 * TODO do not always rebuild relation, but store relation on stack
	 * and make copy for modifications
	 *
	 */
	private class CombinationIterator2 implements Iterable<Set<Doubleton<Term>>> {

		private final List<Set<Doubleton<Term>>> mResult = new ArrayList<>();
		
		private final LinkedList<Boolean> mStack = new LinkedList<>();
		private SymmetricHashRelation<Term> mCurrentRelation;
		private final List<Doubleton<Term>> mNonDisjointDoubletons;
		
		private final ThreeValuedEquivalenceRelation<Term> mEqualityInformation;

		public CombinationIterator2(final Collection<Term> indices, final ThreeValuedEquivalenceRelation<Term> equalityInformation) {
			super();
			mNonDisjointDoubletons = buildListOfNonDisjointDoubletons(indices, equalityInformation);
			mEqualityInformation = equalityInformation;
			mCurrentRelation = new SymmetricHashRelation<>();

			while(true) {
				if (mStack.size() == mNonDisjointDoubletons.size()) {
					addRelationToResult();
					if (mCurrentRelation.isEmpty()) {
						break;
					}
				}
				advance();
			}
			assert checkResultWithOldCombinationIterator(indices,
					equalityInformation) : "result of CombinationIterator and CombinationIterator2 is different";
		}

		private boolean checkResultWithOldCombinationIterator(final Collection<Term> indices, final ThreeValuedEquivalenceRelation<Term> equalityInformation) {
			final Set<Set<Doubleton<Term>>> newResult = new HashSet<>(mResult);
			final Set<Set<Doubleton<Term>>> oldResult = new HashSet<>();
			final CombinationIterator ci = new CombinationIterator(indices, equalityInformation);
			for (final Set<Doubleton<Term>> e : ci) {
				oldResult.add(e);
			}
			assert newResult.equals(oldResult) : "result of CombinationIterator and CombinationIterator2 is different "
					+ newResult.size() + " vs. " + oldResult.size();
			return newResult.equals(oldResult);
		}

		private void advance() {
			if (mStack.size() == mNonDisjointDoubletons.size()) {
				remove1true();
				rebuildCurrentRelation();
				tryToPush1False();
			} else {
				tryToPush1True();
			}
			
		}

		/**
		 * Try to push 'false' on the stack. If the relation becomes 
		 * inconsistent, backtrack to the last 'true' (i.e., remove elements
		 * until we reached the last 'true', including the last 'true') and
		 * push 'false'. Continue until we reached a consistent stack.
		 * Note that there has is at least one consistent stack, namely the
		 * one that contains only 'false' elements. 
		 */
		private void tryToPush1False() {
			final Doubleton<Term> d = mNonDisjointDoubletons.get(mStack.size());
			if (mCurrentRelation.containsPair(d.getOneElement(), d.getOtherElement())) {
				// we cannot add false
				remove1true();
				rebuildCurrentRelation();
				tryToPush1False();
			} else {
				mStack.add(false);
			}
		}

		/**
		 * Push 'true' on the stack. If the relation becomes inconsistent
		 * remove the 'true' and call the {@link CombinationIterator2#tryToPush1False()}
		 * method which iterates until it was able to push 'false' to the stack.
		 */
		private void tryToPush1True() {
			final Doubleton<Term> d = mNonDisjointDoubletons.get(mStack.size());
			if (mEqualityInformation.getEquality(d.getOneElement(), d.getOtherElement()) == Equality.NOT_EQUAL) {
				// we cannot add true
			} else {
				mStack.add(true);
				mCurrentRelation.addPair(d.getOneElement(), d.getOtherElement());
				final Set<Doubleton<Term>> newPairs = mCurrentRelation.makeTransitive();
				final boolean containsDisjointPair = containsNotEqualsPair(newPairs);
				if (containsDisjointPair) {
					remove1true();
					rebuildCurrentRelation();
					tryToPush1False();
				}
			}
		}

		private boolean containsNotEqualsPair(final Set<Doubleton<Term>> pairs1) {
			for (final Doubleton<Term> pairFrom1 : pairs1) {
				if (mEqualityInformation.getEquality(pairFrom1.getOneElement(), pairFrom1.getOtherElement()) == Equality.NOT_EQUAL) {
					return true;
				}
			}
			return false;
		}

		private void rebuildCurrentRelation() {
			mCurrentRelation = new SymmetricHashRelation<>();
			int offset = 0;
			for (final Boolean bool : mStack) {
				if (bool) {
					final Doubleton<Term> doubleton = mNonDisjointDoubletons.get(offset);
					mCurrentRelation.addPair(doubleton.getOneElement(), doubleton.getOtherElement());
				}
				offset++;
			}
			mCurrentRelation.makeTransitive();
		}

		/**
		 * Remove elements from the stack until one 'true' element was removed.
		 */
		private void remove1true() {
			while (!mStack.peekLast()) {
				mStack.removeLast();
			}
			mStack.removeLast();
		}

		private void addRelationToResult() {
			mResult.add(mCurrentRelation.buildSetOfNonSymmetricDoubletons());
		}

		public int size() {
			return mResult.size();
		}

		@Override
		public Iterator<Set<Doubleton<Term>>> iterator() {
			return mResult.iterator();
		}

	}
	
	
	
	
	/**
	 * Alternation-free (quantifier) elimination task
	 *
	 */
	private static class AfEliminationTask {
		private final LinkedHashSet<TermVariable> mEliminatees;
		private final Term mTerm;
		
		public AfEliminationTask(final Set<TermVariable> eliminatees, final Term term) {
			super();
			mEliminatees = new LinkedHashSet<>();
			for (final TermVariable freeVar : term.getFreeVars()) {
				if (eliminatees.contains(freeVar)) {
					mEliminatees.add(freeVar);
				}
			}
			mTerm = term;
		}
		
		public Set<TermVariable> getEliminatees() {
			return Collections.unmodifiableSet(mEliminatees);
		}
		
		public Term getTerm() {
			return mTerm;
		}
		
		
		
	}

}
