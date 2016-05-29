/*
 * Copyright (C) 2015 Christian Ortolf
 * Copyright (C) 2015 Christian Simon
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE command line interface.
 * 
 * The ULTIMATE command line interface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE command line interface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE command line interface. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE command line interface, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE command line interface grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.BasicToolchainJob;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.DefaultToolchainJob;
import de.uni_freiburg.informatik.ultimate.core.lib.toolchain.ToolchainListType;
import de.uni_freiburg.informatik.ultimate.core.model.IController;
import de.uni_freiburg.informatik.ultimate.core.model.ICore;
import de.uni_freiburg.informatik.ultimate.core.model.ISource;
import de.uni_freiburg.informatik.ultimate.core.model.ITool;
import de.uni_freiburg.informatik.ultimate.core.model.IToolchainData;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;

/**
 * Implements standard fallback controller for the command-line.
 * 
 * See OldCommandLineParser for valid command-line arguments.
 * 
 * @author Christian Ortolf
 * @author Christian Simon
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 */
public class CommandlineController implements IController<ToolchainListType> {

	private ILogger mLogger;
	private IToolchainData<ToolchainListType> mToolchain;

	@Override
	public int init(final ICore<ToolchainListType> core) {
		if (core == null) {
			return -1;
		}

		mLogger = core.getCoreLoggingService().getControllerLogger();
		mLogger.debug("Initializing CommandlineController...");

		// parse command line parameters and select ultimate mode
		final OldCommandLineParser cmdParser = new OldCommandLineParser();
		cmdParser.parse(Platform.getCommandLineArgs());
		
		final CommandLineParser newCmdParser = new CommandLineParser(core);
		newCmdParser.printHelp();
		newCmdParser.parse(Platform.getCommandLineArgs());

		// determine Ultimate's mode
		if (cmdParser.getExitSwitch()) {
			cmdParser.printUsage();
			return IApplication.EXIT_OK;
		}

		loadSettings(core, cmdParser);
		if (!loadToolchain(core, cmdParser)) {
			return -1;
		}

		final List<File> inputFiles;
		try {
			inputFiles = getInputFiles(cmdParser);
		} catch (final IllegalArgumentException e1) {
			mLogger.fatal("Input file not found. Paths were: " + String.join(",", cmdParser.getInputFile()), e1);
			return -1;
		}

		if (!startToolchain(core, inputFiles)) {
			return -1;
		}

		return IApplication.EXIT_OK;
	}

	private boolean startToolchain(final ICore<ToolchainListType> core, final List<File> inputFiles) {
		try {
			final BasicToolchainJob tcj = new DefaultToolchainJob("Processing Toolchain", core, this, mLogger,
					inputFiles.toArray(new File[0]));
			tcj.schedule();
			// in non-GUI mode, we must wait until job has finished!
			tcj.join();
			return true;

		} catch (final InterruptedException e) {
			mLogger.error("Exception in Toolchain", e);
			return false;
		}
	}

	private boolean loadToolchain(final ICore<ToolchainListType> core, final OldCommandLineParser cmdParser) {
		try {
			mToolchain = parseToolFile(cmdParser.getToolFile(), core);
			return true;
		} catch (final FileNotFoundException e1) {
			mLogger.fatal("Toolchain file not found. Path was: " + cmdParser.getToolFile(), e1);
			return false;
		} catch (final JAXBException e1) {
			mLogger.fatal("Toolchain file maformed. Path was: " + cmdParser.getToolFile(), e1);
			return false;
		} catch (final SAXException e1) {
			mLogger.fatal("Toolchain file maformed. Path was: " + cmdParser.getToolFile(), e1);
			return false;
		}
	}

	private void loadSettings(final ICore<ToolchainListType> core, final OldCommandLineParser cmdParser) {
		final String settingsfile = cmdParser.getSettings();
		if (settingsfile != null) {
			core.loadPreferences(settingsfile);
		} else {
			mLogger.info("No settings file supplied");
		}
	}

	private List<File> getInputFiles(final OldCommandLineParser cmdParser) {
		final List<File> inputFiles = new ArrayList<>();
		for (final String inputfilePath : cmdParser.getInputFile()) {
			final File inputFile = new File(inputfilePath);
			if (!inputFile.exists() || !inputFile.canRead()) {
				throw new IllegalArgumentException();
			}
			inputFiles.add(inputFile);
		}
		return inputFiles;
	}

	@Override
	public ISource selectParser(Collection<ISource> parser) {
		final Object[] parsers = parser.toArray();

		mLogger.info("Index\tParser ID");

		for (int i = 0; i < parsers.length; i++) {
			mLogger.info(String.valueOf(i) + "\t" + ((ISource) parsers[i]).getPluginID());
		}

		mLogger.info("Please choose a parser manually:");

		final InputStreamReader inp = new InputStreamReader(System.in, Charset.defaultCharset());
		final BufferedReader br = new BufferedReader(inp);

		while (true) {
			try {
				final String str = br.readLine();
				final int selection = Integer.parseInt(str);
				if (selection < parsers.length) {
					return (ISource) parsers[selection];
				} else {
					mLogger.error("Please make a valid selection.");
				}
			} catch (final NumberFormatException e) {
				mLogger.error("Please make a valid selection.");
			} catch (final IOException e) {
				mLogger.error("There was problem opening your console.");
			}
		}
	}

	@Override
	public String getPluginName() {
		return Activator.PLUGIN_NAME;
	}

	@Override
	public String getPluginID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public IToolchainData<ToolchainListType> selectTools(List<ITool> tools) {
		return mToolchain;
	}

	@Override
	public List<String> selectModel(List<String> modelNames) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void displayToolchainResultProgramIncorrect() {
		mLogger.info("RESULT: Ultimate proved your program to be incorrect!");
	}

	@Override
	public void displayToolchainResultProgramCorrect() {
		mLogger.info("RESULT: Ultimate proved your program to be correct!");
	}

	@Override
	public void displayToolchainResultProgramUnknown(String description) {
		mLogger.info("RESULT: Ultimate could not prove your program: " + description);
	}

	@Override
	public void displayException(final String description, final Throwable ex) {
		mLogger.fatal("RESULT: An exception occured during the execution of Ultimate.", ex);
	}

	@Override
	public IPreferenceInitializer getPreferences() {
		return null;
	}

	private IToolchainData<ToolchainListType> parseToolFile(String toolFile, ICore<ToolchainListType> core)
			throws JAXBException, SAXException, FileNotFoundException {
		if (toolFile == null || "".equals(toolFile)) {
			throw new FileNotFoundException();
		}
		return core.createToolchainData(toolFile);
	}
}
