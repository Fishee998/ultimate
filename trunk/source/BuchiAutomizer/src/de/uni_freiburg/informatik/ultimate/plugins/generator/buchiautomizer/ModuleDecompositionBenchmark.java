package de.uni_freiburg.informatik.ultimate.plugins.generator.buchiautomizer;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.uni_freiburg.informatik.ultimate.core.services.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.model.ITranslator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RcfgElement;
import de.uni_freiburg.informatik.ultimate.result.ResultUtil;
import de.uni_freiburg.informatik.ultimate.result.TerminationArgumentResult;
import de.uni_freiburg.informatik.ultimate.util.csv.ICsvProvider;
import de.uni_freiburg.informatik.ultimate.util.csv.ICsvProviderProvider;
import de.uni_freiburg.informatik.ultimate.util.csv.SimpleCsvProvider;

public class ModuleDecompositionBenchmark implements ICsvProviderProvider<Double>{
	
	private final TreeMap<Integer,Integer> m_ModuleSizeTrivial = new TreeMap<Integer,Integer>();
	private final TreeMap<Integer,Integer> m_ModuleSizeDeterministic = new TreeMap<Integer,Integer>();
	private final TreeMap<Integer,Integer> m_ModuleSizeNondeterministic = new TreeMap<Integer,Integer>();
	private final TreeMap<Integer,TerminationArgumentResult<RcfgElement>> m_RankingFunction = new TreeMap<Integer,TerminationArgumentResult<RcfgElement>>();
	/**
	 * Is there a remainder module? A remainder module contains remaining 
	 * traces if decomposition into modules failed.
	 * Null if yet unknown.
	 */
	private Boolean m_HasRemainderModule;
	private int m_RemainderModuleLocations;
	private boolean m_RemainderModuleNonterminationKnown;
	private IBacktranslationService mBacktranslationService;
	
	public ModuleDecompositionBenchmark(IBacktranslationService service) {
		mBacktranslationService = service;
	}
	
	
	void reportTrivialModule(Integer iteration, Integer size) {
		m_ModuleSizeTrivial.put(iteration, size);
	}
	
	void reportDeterminsticModule(Integer iteration, Integer size) {
		m_ModuleSizeDeterministic.put(iteration, size);
	}
	
	void reportNonDeterminsticModule(Integer iteration, Integer size) {
		m_ModuleSizeNondeterministic.put(iteration, size);
	}
	
	void reportRankingFunction(Integer iteration, TerminationArgumentResult<RcfgElement> tar) {
		m_RankingFunction.put(iteration, tar);
	}
	
	void reportRemainderModule(int numberLocations, boolean nonterminationKnown) {
		assert m_HasRemainderModule == null : "remainder module already reported";
		m_HasRemainderModule = true;
		m_RemainderModuleLocations = numberLocations;
		m_RemainderModuleNonterminationKnown = nonterminationKnown;
	}
	
	void reportNoRemainderModule() {
		assert m_HasRemainderModule == null : "remainder module already reported";
		m_HasRemainderModule = false;
	}
	
	private String prettyPrintRankingFunction(TerminationArgumentResult<RcfgElement> tar) {
		List<ITranslator<?, ?, ?, ?>> translatorSequence = 
				mBacktranslationService.getTranslatorSequence();
		return tar.getRankingFunctionDescription() + " ranking function " + 
						ResultUtil.backtranslationWorkaround(
								translatorSequence, tar.getRankingFunction());
	}
	
	@Override
	public String toString() {
		if (m_HasRemainderModule == null) {
			return "Decomposition not yet finished";
		}
		int modules = m_ModuleSizeTrivial.size() + m_ModuleSizeDeterministic.size() + m_ModuleSizeNondeterministic.size();
		if (modules == 0) {
			if (m_HasRemainderModule) {
				if (m_RemainderModuleNonterminationKnown) {
					return "Trivial decomposition into one nonterminating module.";
				} else {
					return "Trivial decomposition into one module whose termination is unknown.";
				}
			} else {
				return "Trivial decomposition. There is no loop in your program.";
			}
		}
		int maxNumberOfStatesOfModuleWithTrivialRankingFunction = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("Your program was decomposed into ");
		sb.append(modules);
		sb.append(" terminating modules ");
		sb.append("(");
		sb.append(m_ModuleSizeTrivial.size());
		sb.append(" trivial, ");
		sb.append(m_ModuleSizeDeterministic.size());
		sb.append(" deterministic, ");
		sb.append(m_ModuleSizeNondeterministic.size());
		sb.append(" nondeterministic)");
		if (m_HasRemainderModule) {
			if (m_RemainderModuleNonterminationKnown) {
				sb.append(" and one nonterminating remainder module.");
			} else {
				sb.append(" and one module whose termination is unknown.");
			}
		} else {
			sb.append(". ");
		}
		for (Entry<Integer, Integer> entry  : m_ModuleSizeDeterministic.entrySet()) {
			sb.append("One deterministic module has ");
			sb.append(prettyPrintRankingFunction(m_RankingFunction.get(entry.getKey())));
			sb.append(" and consists of ");
			sb.append(entry.getValue());
			sb.append(" locations. ");
		}
		for (Entry<Integer, Integer> entry : m_ModuleSizeNondeterministic.entrySet()) {
			sb.append("One nondeterministic module has ");
			sb.append(prettyPrintRankingFunction(m_RankingFunction.get(entry.getKey())));
			sb.append(" and consists of ");
			sb.append(entry.getValue());
			sb.append(" locations. ");
		}
		for (Entry<Integer, Integer> entry : m_ModuleSizeTrivial.entrySet()) {
			if (entry.getValue() > maxNumberOfStatesOfModuleWithTrivialRankingFunction) {
				maxNumberOfStatesOfModuleWithTrivialRankingFunction = entry.getValue();
			}
		}
		if (m_ModuleSizeTrivial.size() > 0) {
			sb.append(m_ModuleSizeTrivial.size());
			sb.append(" modules have a trivial ranking function, the largest among these consists of ");
			sb.append(maxNumberOfStatesOfModuleWithTrivialRankingFunction);
			sb.append(" locations.");
		}
		if (m_HasRemainderModule) {
			sb.append(" The remainder module has ");
			sb.append(m_RemainderModuleLocations);
			sb.append(" locations.");
		} 
		return sb.toString();
	}

	@Override
	public ICsvProvider<Double> createCvsProvider() {
		SimpleCsvProvider<Double> rtr = new SimpleCsvProvider<>(new String[] { });

		return rtr;
	}


}
