package de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.uni_freiburg.informatik.ultimate.core.api.PreludeProvider;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.Activator;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences.CorePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.IController;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ICore;
import de.uni_freiburg.informatik.ultimate.result.IResult;
import de.uni_freiburg.informatik.ultimate.result.ExceptionOrErrorResult;

/**
 * This class implements an Eclipse Job processing a Ultimate toolchain using
 * the methods publicly available from ICore.
 * 
 * @author Christian Simon
 * 
 */
public class ToolchainJob extends Job {

	/**
	 * How do you want the toolchain to be processed? RUN_TOOLCHAIN: Everything
	 * new from the scratch. RERUN_TOOLCHAIN: run old toolchain on old input
	 * files RUN_OLDTOOLCHAIN: run old toolchain on new input files
	 * RUN_NEWTOOLCHAIN: run new toolchain on old input files
	 * 
	 */
	public static enum Chain_Mode {
		RUN_TOOLCHAIN, RUN_NEWTOOLCHAIN, RERUN_TOOLCHAIN, RUN_OLDTOOLCHAIN
	};

	private Chain_Mode mJobMode;

	private ICore mCore;
	private IController mController;
	private Logger mLogger;
	private File mInputFile;

	private Toolchain mChain;

	private PreludeProvider mPreludeFile;

	/**
	 * Constructor for the new toolchain job to be executed.
	 * 
	 * @param name
	 *            - How do we want to call the job? Will be display in the
	 *            status bar!
	 * @param core
	 *            - Reference to currently active Ultimate Core.
	 * @param mWorkbenchWindow
	 *            - Do we have a workbench window? 'null' is fine!
	 * @param boogieFiles
	 *            - array of input boogie files
	 * @param mode_arg
	 *            - The desired mode for toolchain execution. See Chain_Mode.
	 * @param preludefile
	 *            - Do we want a prelude file to be passed to the parser?
	 */
	public ToolchainJob(String name, ICore core, IController controller,
			File boogieFiles, Chain_Mode mode_arg, PreludeProvider preludefile) {
		super(name);
		setUser(true);
		setSystem(false);
		mCore = core;
		mController = controller;
		mInputFile = boogieFiles;
		mJobMode = mode_arg;
		mPreludeFile = preludefile;
		mLogger = UltimateServices.getInstance().getLogger(
				Activator.s_PLUGIN_ID);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

	IStatus returnstatus = Status.OK_STATUS;

		try {
			boolean retval;
			
			setTimeout();
			UltimateServices.getInstance().initializeResultMap();
			UltimateServices.getInstance().initializeTranslatorSequence();

			if ((this.mJobMode == Chain_Mode.RERUN_TOOLCHAIN || this.mJobMode == Chain_Mode.RUN_OLDTOOLCHAIN)
					&& !this.mCore.canRerun()) {
				throw new Exception(
						"Rerun called without previous run! Aborting...");
			}
			// all modes requires this
			this.mCore.resetCore();

			// only RUN_TOOLCHAIN and RUN_OLDTOOLCHAIN require this
			if (this.mJobMode == Chain_Mode.RUN_TOOLCHAIN
					|| this.mJobMode == Chain_Mode.RUN_OLDTOOLCHAIN) {
				this.mCore.setInputFile(mInputFile);

			}

			// all but RERUN_TOOLCHAIN require this!
			if (this.mJobMode != Chain_Mode.RERUN_TOOLCHAIN) {
				retval = this.mCore.initiateParser(this.mPreludeFile);
				if (!retval)
					throw new Exception();
			}

			// only RUN_TOOLCHAIN and RUN_NEWTOOLCHAIN require this
			if (this.mJobMode == Chain_Mode.RUN_TOOLCHAIN
					|| this.mJobMode == Chain_Mode.RUN_NEWTOOLCHAIN) {
				this.mChain = this.mCore.makeToolSelection();
				if (this.mChain == null) {
					mLogger.warn("Toolchain selection failed, aborting...");
					return new Status(Status.CANCEL, Activator.s_PLUGIN_ID,
							"Toolchain selection canceled");
				}
			}

			this.mCore.letCoreRunParser();

			returnstatus = this.mCore.processToolchain(monitor);

		} catch (final Throwable e) {
			mLogger.fatal("The toolchain threw an exception:" + e.getMessage());
			mController.displayException("The toolchain threw an exception", e);
			returnstatus = Status.CANCEL_STATUS;
			String idOfCore = Activator.s_PLUGIN_ID;
			ExceptionOrErrorResult result = new ExceptionOrErrorResult(idOfCore, e); 
			UltimateServices.getInstance().reportResult(idOfCore, result);
			e.printStackTrace();
		} finally {
			monitor.done();
		}
		
		logResults();
		return returnstatus;
	}

	/**
	 * Write all IResults produced by the toolchain to the logger.
	 */
	private void logResults() {
		for (Entry<String, List<IResult>> entry : 
					UltimateServices.getInstance().getResultMap().entrySet()) {
			for (IResult result : entry.getValue()) {
				StringBuilder sb = new StringBuilder();
				sb.append(result.getClass().getSimpleName());
				sb.append(" from ");
				sb.append(entry.getKey());
				sb.append(".");
				sb.append(" Short description: ");
				sb.append(result.getShortDescription());
				boolean appendLongDescription = false;
				if (appendLongDescription) {
					sb.append(" Long description: ");
					sb.append(result.getLongDescription());
				}
				mLogger.info(sb.toString());
			}
		}
	}
	
	/**
	 * Use the timeout specified in the preferences of CoreRCP to set a
	 * deadline for the toolchain execution.
	 * Note that the ultimate core does not check that the toolchain execution 
	 * complies with the deadline. Plugins should check if the deadline is 
	 * overdue and abort.
	 * A timeout of 0 means that we do not set any deadline.
	 */
	public void setTimeout() {
		UltimatePreferenceStore ups = 
				new UltimatePreferenceStore(Activator.s_PLUGIN_ID);
		int timeoutInPreferences = 
				ups.getInt(CorePreferenceInitializer.LABEL_TIMEOUT);
		if (timeoutInPreferences == 0) {
			// do not set any timout
		} else {
			long timoutMilliseconds = timeoutInPreferences * 1000L;
			UltimateServices.getInstance().setDeadline(
					System.currentTimeMillis() + timoutMilliseconds);
		}
	}

}
