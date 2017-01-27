package de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.pathinvariants.internal;

import java.util.Collection;
import java.util.HashSet;
//import java.util.List;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IcfgLocation;
import de.uni_freiburg.informatik.ultimate.plugins.generator.traceabstraction.pathinvariants.PathInvariantsGenerator.PathInvariantsStatisticsDefinitions;
import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsDataProvider;
import de.uni_freiburg.informatik.ultimate.util.statistics.IStatisticsType;

public class PathInvariantsStatisticsGenerator implements IStatisticsDataProvider {
	private int mNumOfLocations = 0;
	private int mSizeOfBiggestTemplate = 0;
	private int mSumOfTemplateConjuncts = 0;
//	private Set<List<IcfgLocation>> mTransitionsInUnsatCore = new HashSet<>();
	private Set<IcfgLocation> mLocsInUnsatCore = new HashSet<>();

	public void setNumOfLocations(final int numOfLocations) {
		mNumOfLocations = numOfLocations;
	}

	public void addPathInvariantsData(final int sizeOfBiggestTemplate, final int sumOfTemplateConjuncts) {
		if (sizeOfBiggestTemplate > mSizeOfBiggestTemplate) {
			mSizeOfBiggestTemplate = sizeOfBiggestTemplate;
		}
		mSumOfTemplateConjuncts = mSumOfTemplateConjuncts + sumOfTemplateConjuncts;
	}

	@Override
	public Collection<String> getKeys() {
		return PathInvariantsStatisticsType.getInstance().getKeys();
	}

	@Override
	public Object getValue(String key) {
		final PathInvariantsStatisticsDefinitions keyEnum = Enum.valueOf(PathInvariantsStatisticsDefinitions.class, key);
		switch (keyEnum) {
		case SumOfLocs: return mNumOfLocations;
		case SumOfTemplateConjuncts: return mSumOfTemplateConjuncts;
		case MaxSizeOfTemplate: return mSizeOfBiggestTemplate;
		case LocsInUnsatCore: return mLocsInUnsatCore;
		default:
			throw new AssertionError("unknown key");
		}
	}

	@Override
	public IStatisticsType getBenchmarkType() {
		return PathInvariantsStatisticsType.getInstance();
	}

//	public void setTransitionsInUnsatCore(Set<List<IcfgLocation>> transitionsInUnsatCore) {
//		mTransitionsInUnsatCore = transitionsInUnsatCore;
//	}
	public void setLocsInUnsatCore(Set<IcfgLocation> locsInUnsatCore) {
		mLocsInUnsatCore = locsInUnsatCore;
	}
	

}
