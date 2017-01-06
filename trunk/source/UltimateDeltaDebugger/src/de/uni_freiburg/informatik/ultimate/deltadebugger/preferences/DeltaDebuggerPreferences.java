/*
 * Copyright (C) 2017 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
 *
 * This file is part of the Ultimate Delta Debugger plug-in.
 *
 * The Ultimate Delta Debugger plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Ultimate Delta Debugger plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Ultimate Delta Debugger plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the Ultimate Delta Debugger plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the Ultimate Delta Debugger plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.deltadebugger.preferences;

import java.util.Arrays;
import java.util.stream.Collectors;

import de.uni_freiburg.informatik.ultimate.core.lib.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.lib.results.ExceptionOrErrorResult;
import de.uni_freiburg.informatik.ultimate.core.lib.results.SyntaxErrorResult;
import de.uni_freiburg.informatik.ultimate.core.lib.results.TypeErrorResult;
import de.uni_freiburg.informatik.ultimate.core.lib.results.UnsupportedSyntaxResult;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.BaseUltimatePreferenceItem.PreferenceType;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.preferences.UltimatePreferenceItem;
import de.uni_freiburg.informatik.ultimate.core.model.results.IResult;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.deltadebugger.Activator;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public final class DeltaDebuggerPreferences extends UltimatePreferenceInitializer {

	public enum ExternalComparison {
		NONE, CPACHECKER
	}

	public static final String LABEL_INTERESTING_RESULT_TYPE = "Look for result of type";
	private static final String DESC_INTERESTING_RESULT_TYPE =
			"Specify the name of a type that represents an Ultimate result "
					+ "(i.e., some class implementing IResult with this name). "
					+ "The delta debugger searchs for the presence of this result.";
	private static final Class<?>[] INTERESTING_RESULT_TYPE_CLASSES = new Class<?>[] { ExceptionOrErrorResult.class,
			SyntaxErrorResult.class, UnsupportedSyntaxResult.class, TypeErrorResult.class };
	private static final String[] VALUES_INTERESTING_RESULT_TYPE =
			Arrays.stream(INTERESTING_RESULT_TYPE_CLASSES).map(a -> a.getSimpleName()).collect(Collectors.toList())
					.toArray(new String[INTERESTING_RESULT_TYPE_CLASSES.length]);
	private static final String DEFAULT_INTERESTING_RESULT_TYPE = ExceptionOrErrorResult.class.getSimpleName();

	public static final String LABEL_RESULT_SHORT_DESC_PREFIX = "Result short description prefix";
	private static final String DESC_RESULT_SHORT_DESC_PREFIX =
			"The provided string must be the prefix of one of the results that are interesting. "
					+ "Use the empty string if all results of this type are interesting.";
	private static final String DEFAULT_RESULT_SHORT_DESC_PREFIX = "";

	public static final String LABEL_EXTERNAL_TOOL_TIMEOUT = "External tool timeout";
	private static final int DEFAULT_EXTERNAL_TOOL_TIMEOUT = 90000;
	private static final String DESC_EXTERNAL_TOOL_TIMEOUT =
			"Specify the timeout for an external tool in milliseconds.";

	public static final String LABEL_EXTERNAL_TOOL_MODE = "Use external tools";
	private static final String DESC_EXTERNAL_TOOL_MODE =
			"Use an external tool to determine if a run is interesting or not. "
					+ "Also specify, which external tool should be used.";

	public static final String LABEL_EXTERNAL_TOOL_WORKING_DIR = "Working directory of external tool";
	private static final String DESC_EXTERNAL_TOOL_WORKING_DIR = "The working directory of the external tool.";

	public static final String LABEL_EXTERNAL_TOOL_CPACHECKER_CMD = "Command to execute CPAChecker";
	private static final String DESC_EXTERNAL_TOOL_CPACHECKER_CMD =
			"Specifiy the command to execute CPAChecker (without timeout and input file, but with property file).";

	public static final String LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_INTERESTING =
			"CPAChecker stdout regex interesting";
	public static final String LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_OOM =
			"CPAChecker stdout regex out of memory";
	public static final String LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_TIMEOUT = "CPAChecker stdout regex timeout";
	public static final String LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_ERROR = "CPAChecker stdout regex error";
	private static final String DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_INTERESTING =
			"A regex that is applied to each line of CPAChecker's standard output. "
					+ "If it matches, the result of this run is interesting (delta debugger can reduce)";
	private static final String DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_OOM =
			"A regex that is applied to each line of CPAChecker's standard output. "
					+ "If it matches, the result of this run is out of memory.";
	private static final String DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_TIMEOUT =
			"A regex that is applied to each line of CPAChecker's standard output. "
					+ "If it matches, the result of this run is a timeout.";
	private static final String DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_ERROR =
			"A regex that is applied to each line of CPAChecker's standard output. "
					+ "If it matches, the result of this run is an error.";

	/**
	 * Default constructor
	 */
	public DeltaDebuggerPreferences() {
		super(Activator.PLUGIN_ID, Activator.PLUGIN_NAME);
	}

	@Override
	protected UltimatePreferenceItem<?>[] initDefaultPreferences() {
		return new UltimatePreferenceItem<?>[] {

				new UltimatePreferenceItem<>(LABEL_INTERESTING_RESULT_TYPE, DEFAULT_INTERESTING_RESULT_TYPE,
						DESC_INTERESTING_RESULT_TYPE, PreferenceType.Combo, VALUES_INTERESTING_RESULT_TYPE),
				new UltimatePreferenceItem<>(LABEL_RESULT_SHORT_DESC_PREFIX, DEFAULT_RESULT_SHORT_DESC_PREFIX,
						DESC_RESULT_SHORT_DESC_PREFIX, PreferenceType.String),

				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_MODE, ExternalComparison.NONE, DESC_EXTERNAL_TOOL_MODE,
						PreferenceType.Combo, ExternalComparison.values()),
				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_WORKING_DIR, "", DESC_EXTERNAL_TOOL_WORKING_DIR,
						PreferenceType.String),
				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_CPACHECKER_CMD, "", DESC_EXTERNAL_TOOL_CPACHECKER_CMD,
						PreferenceType.String),

				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_INTERESTING, "",
						DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_INTERESTING, PreferenceType.String),
				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_OOM, "",
						DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_OOM, PreferenceType.String),
				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_TIMEOUT, "",
						DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_TIMEOUT, PreferenceType.String),
				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_ERROR, "",
						DESC_EXTERNAL_TOOL_CPACHECKER_OUTPUT_REGEX_ERROR, PreferenceType.String),

				new UltimatePreferenceItem<>(LABEL_EXTERNAL_TOOL_TIMEOUT, DEFAULT_EXTERNAL_TOOL_TIMEOUT,
						DESC_EXTERNAL_TOOL_TIMEOUT, PreferenceType.Integer),

		};
	}

	/**
	 * Get the class of the IResult type the user is interested in.
	 * 
	 * @param services
	 *            The {@link IUltimateServiceProvider} instance of the current toolchain.
	 * @return The {@link Class} of the {@link IResult} type the user is interested in.
	 */
	@SuppressWarnings({ "squid:S1872", "unchecked" })
	public static Class<? extends IResult> getInterestingClass(final IUltimateServiceProvider services) {
		final IPreferenceProvider provider = services.getPreferenceProvider(Activator.PLUGIN_ID);
		final String interestingType = provider.getString(LABEL_INTERESTING_RESULT_TYPE);
		return (Class<? extends IResult>) Arrays.stream(INTERESTING_RESULT_TYPE_CLASSES)
				.filter(a -> a.getSimpleName().equals(interestingType)).findAny().orElseThrow(
						() -> new IllegalArgumentException("No result with name " + interestingType + " is known"));
	}

}
