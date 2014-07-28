package de.uni_freiburg.informatik.ultimate.LTL2aut;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.LTL2aut.preferences.PreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.access.IObserver;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.IGenerator;
import de.uni_freiburg.informatik.ultimate.model.GraphType;
import de.uni_freiburg.informatik.ultimate.model.IElement;

public class LTL2aut implements IGenerator {

	protected Logger mLogger;

	protected List<String> mFileNames = new ArrayList<String>();

	private DummyLTL2autObserver mObserver;

	private boolean mProcess;

	private IUltimateServiceProvider mServices;

	@Override
	public int init() {
		mProcess = false;
		return 0;
	}

	@Override
	public String getPluginName() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public GraphType getOutputDefinition() {
		List<String> filenames = new ArrayList<String>();
		filenames.add("Hardcoded");

		return new GraphType(Activator.PLUGIN_ID, GraphType.Type.AST, filenames);
	}

	@Override
	public boolean isGuiRequired() {
		return false;
	}

	@Override
	public QueryKeyword getQueryKeyword() {
		return QueryKeyword.ALL;
	}

	@Override
	public List<String> getDesiredToolID() {
		return null;
	}

	@Override
	public void setInputDefinition(GraphType graphType) {
		switch (graphType.getCreator()) {
		case "BoogiePLCupParser":
			mProcess = true;
			break;
		default:
			mProcess = false;
			break;
		}
	}

	@Override
	public List<IObserver> getObservers() {
		mObserver = new DummyLTL2autObserver(mLogger);
		ArrayList<IObserver> observers = new ArrayList<IObserver>();
		if (mProcess) {
			observers.add(mObserver);
		}
		return observers;
	}

	@Override
	public IElement getModel() {
		return mObserver.getRootNode();
	}

	@Override
	public UltimatePreferenceInitializer getPreferences() {
		return new PreferenceInitializer();
	}

	@Override
	public void setToolchainStorage(IToolchainStorage services) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setServices(IUltimateServiceProvider services) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
	}

}
