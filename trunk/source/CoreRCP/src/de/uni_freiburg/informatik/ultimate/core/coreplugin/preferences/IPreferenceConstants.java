/*
 * 
 * Project:	CoreRCP
 * Package:	de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences
 * File:	IPreferenceConstants.java created on Jan 31, 2010 by Bj�rn Buchhold
 *
 */
package de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences;

import java.util.Arrays;

/**
 * IPreferenceConstants
 * 
 * @author Bj�rn Buchhold
 * 
 */
public interface IPreferenceConstants {

	/**
	 * Names of preferences in pref-tree.
	 */
	public static final String PREFID_ROOT = "ultimate.logging.root";
	public static final String PREFID_CORE = "ultimate.logging.core";
	public static final String PREFID_CONTROLLER = "ultimate.logging.controller";
	public static final String PREFID_TOOLS = "ultimate.logging.tools";
	public static final String PREFID_PLUGINS = "ultimate.logging.plugins";
	public static final String PREFID_DETAILS = "ultimate.logging.details";
	public static final String PREFID_TOOLDETAILS = "ultimate.logging.tooldetails";
	public static final String s_NAME_SHOWUSABLEPARSER = "showuseableparser";
	public static final String s_NAME_SHOWRESULTNOTIFIERPOPUP = "showresultnotifierpopup";
	public static final String PREFID_MM_DROP_MODELS = "ultimate.modelmanager.dropmodels";
	public static final String PREFID_MM_TMPDIRECTORY = "ultimate.modelmanager.tmpdirectory";
	
	/**
	 * others
	 */
	public static final String EXTERNAL_TOOLS_PREFIX = "external.";

	
	/**
	 * default values for preferences
	 */
	public static final boolean DEFAULT_MM_DROP_MODELS = true;

	
	/**
	 * values of preferences.
	 */
	public static final boolean s_VALUE_SHOWUSABLEPARSER_DEFAULT = false;
	public static final boolean s_VALUE_SHOWRESULTNOTIFIERPOPUP_DEFAULT = false;
	public static final String VALUE_DEFAULT_LOGGING_PREF = "";
	public static final String VALUE_FATAL_LOGGING_PREF = "FATAL";
	public static final String VALUE_ERROR_LOGGING_PREF = "ERROR";
	public static final String VALUE_WARN_LOGGING_PREF = "WARN";
	public static final String VALUE_INFO_LOGGING_PREF = "INFO";
	public static final String VALUE_DEBUG_LOGGING_PREF = "DEBUG";
	public static final String VALUE_TRACE_LOGGING_PREF = "TRACE";
	public static final String VALUE_DELIMITER_LOGGING_PREF = ";";
	public static final String[] VALUE_VALID_LOG_LEVELS = {
			IPreferenceConstants.VALUE_DEBUG_LOGGING_PREF,
			IPreferenceConstants.VALUE_ERROR_LOGGING_PREF,
			IPreferenceConstants.VALUE_FATAL_LOGGING_PREF,
			IPreferenceConstants.VALUE_INFO_LOGGING_PREF,
			IPreferenceConstants.VALUE_TRACE_LOGGING_PREF,
			IPreferenceConstants.VALUE_WARN_LOGGING_PREF };

	/**
	 * Labels for the preferences
	 */
	public static final String s_LABEL_SHOWUSABLEPARSER = "Show usable parsers";
	public static final String s_LABEL_SHOWRESULTNOTIFIERPOPUP = "Show Result in Pop-Up Window after Toolchain Execution";
	public static final String LABEL_ROOT_PREF = "Root Log Level";
	public static final String LABEL_TOOLS_PREF = " Log Level for External Tools";
	public static final String LABEL_CORE_PREF = "Log Level for Core Plugin";
	public static final String LABEL_CONTROLLER_PREF = "Log Level for Controller Plugin";
	public static final String LABEL_PLUGINS_PREF = "Log Level for Plugins";
	public static final String LABEL_PLUGIN_DETAIL_PREF = "Log Levels for Specific Plug-ins";
	public static final String LABEL_MM_DROPMODELS = "Drop models when Ultimate exits";
	public static final String LABEL_MM_TMPDIRECTORY = "Repository Directory";
	
	/**
	 * Messages
	 */
	public static final String INVALID_LOGLEVEL = "Valid levels: " + Arrays.toString(VALUE_VALID_LOG_LEVELS);
	public static final String INVALID_ENTRY = "Entry has to be of the form: \"<plug-in id>=<log level>\"";
	public static final String INVALID_TOOL_ENTRY = "Entry has to be of the form: \"<tool id>=<log level>\"";
	public static final String LOGGING_PREFERENCES_DESC = "Specify log levels for the certail plug-ins.\n \n Note that there is a hierarchy and specifying a less strict level for children will have no effect";
	public static final String ALL_PLUGINS_PRESENT = "All entered Plug-ins are in fact present!";
	public static final String PLUGINS_NOT_PRESENT = "The following Plug-ins are not present at the moment: \n";
	public static final String EMPTY_STRING = "";
	
}
