/*
 * Copyright (C) 2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE AbstractInterpretationV2 plug-in.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE AbstractInterpretationV2 plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE AbstractInterpretationV2 plug-in. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE AbstractInterpretationV2 plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE AbstractInterpretationV2 plug-in grant you additional permission 
 * to convey the resulting work.
 */

package de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.preferences;

import java.util.ArrayList;

import de.uni_freiburg.informatik.ultimate.core.preferences.AbstractUltimatePreferenceItem;
import de.uni_freiburg.informatik.ultimate.core.preferences.AbstractUltimatePreferenceItem.PreferenceType;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceItem;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceItem.IUltimatePreferenceItemValidator;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceItemContainer;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.empty.EmptyDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.congruence.CongruenceDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.congruence.CongruenceDomainPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval.IntervalDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.interval.IntervalDomainPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.nonrelational.sign.SignDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.relational.octagon.OctPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.relational.octagon.OctagonDomain;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.util.lpsolver.LpSolverPreferences;
import de.uni_freiburg.informatik.ultimate.plugins.analysis.abstractinterpretationv2.domain.vp.VPDomain;

/**
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * @author Marius Greitschus (greitsch@informatik.uni-freiburg.de)
 *
 */
public class AbsIntPrefInitializer extends UltimatePreferenceInitializer {

	public static final String[] VALUES_ABSTRACT_DOMAIN = new String[] { EmptyDomain.class.getSimpleName(),
	        SignDomain.class.getSimpleName(), IntervalDomain.class.getSimpleName(), OctagonDomain.class.getSimpleName(),
	        VPDomain.class.getSimpleName(), CongruenceDomain.class.getSimpleName() };

	public static final String LABEL_ITERATIONS_UNTIL_WIDENING = "Minimum iterations before widening";
	public static final String LABEL_STATES_UNTIL_MERGE = "Parallel states before merging";
	public static final String LABEL_DESCRIPTION_ABSTRACT_DOMAIN = "Settings for the abstract domain to use. Select the Abstract domain to use here.\n\nChange the settings for each abstract domain in the corresponding sub-page.";
	public static final String LABEL_ABSTRACT_DOMAIN = "Abstract domain";

	public static final String LABEL_RUN_AS_PRE_ANALYSIS = "Run as pre-analysis";
	public static final String TOOLTIP_RUN_AS_PRE_ANALYSIS = "Do not report any results, suppress all exceptions except OOM, use 20% of available time";

	public static final String LABEL_PERSIST_ABS_STATES = "Save abstract states as RCFG annotation";

	public static final int DEF_ITERATIONS_UNTIL_WIDENING = 3;
	public static final int DEF_STATES_UNTIL_MERGE = 2;
	public static final boolean DEF_RUN_AS_PRE_ANALYSIS = false;
	private static final boolean DEF_PERSIST_ABS_STATES = false;
	public static final String DEF_ABSTRACT_DOMAIN = VALUES_ABSTRACT_DOMAIN[0];

	public static final String INDENT = "   ";

	@Override
	protected AbstractUltimatePreferenceItem[] initDefaultPreferences() {
		final ArrayList<AbstractUltimatePreferenceItem> rtr = new ArrayList<>();
		rtr.add(new UltimatePreferenceItem<Integer>(LABEL_ITERATIONS_UNTIL_WIDENING, DEF_ITERATIONS_UNTIL_WIDENING,
		        PreferenceType.Integer, new IUltimatePreferenceItemValidator.IntegerValidator(1, 100000)));
		rtr.add(new UltimatePreferenceItem<Integer>(LABEL_STATES_UNTIL_MERGE, DEF_STATES_UNTIL_MERGE,
		        PreferenceType.Integer, new IUltimatePreferenceItemValidator.IntegerValidator(1, 100000)));
		rtr.add(new UltimatePreferenceItem<Boolean>(LABEL_RUN_AS_PRE_ANALYSIS, DEF_RUN_AS_PRE_ANALYSIS,
		        TOOLTIP_RUN_AS_PRE_ANALYSIS, PreferenceType.Boolean));
		rtr.add(new UltimatePreferenceItem<Boolean>(LABEL_PERSIST_ABS_STATES, DEF_PERSIST_ABS_STATES,
		        PreferenceType.Boolean));

		// Abstract Domains Container
		final UltimatePreferenceItemContainer abstractDomainContainer = new UltimatePreferenceItemContainer(
		        "Abstract Domains");
		abstractDomainContainer.addItem(
		        new UltimatePreferenceItem<String>(LABEL_DESCRIPTION_ABSTRACT_DOMAIN, null, PreferenceType.Label));
		abstractDomainContainer.addItem(new UltimatePreferenceItem<String>(LABEL_ABSTRACT_DOMAIN, DEF_ABSTRACT_DOMAIN,
		        PreferenceType.Combo, VALUES_ABSTRACT_DOMAIN));

		// Interval Domain
		final UltimatePreferenceItemContainer intervalContainer = new UltimatePreferenceItemContainer(
		        "Interval Domain");
		intervalContainer.addItems(IntervalDomainPreferences.getPreferences());
		abstractDomainContainer.addItem(intervalContainer);

		// Octagon Domain
		final UltimatePreferenceItemContainer octagonContainer = new UltimatePreferenceItemContainer("Octagon Domain");
		octagonContainer.addItems(OctPreferences.createPreferences());
		abstractDomainContainer.addItem(octagonContainer);

		// Congruence Domain
		final UltimatePreferenceItemContainer congruenceContainer = new UltimatePreferenceItemContainer(
		        "Congruence Domain");
		congruenceContainer.addItems(CongruenceDomainPreferences.getPreferences());
		abstractDomainContainer.addItem(congruenceContainer);

		rtr.add(abstractDomainContainer);

		// Linear Program solver container
		final UltimatePreferenceItemContainer lpSolverContainer = new UltimatePreferenceItemContainer("LP Solver");
		// Add ojAlgo preferences
		lpSolverContainer.addItems(LpSolverPreferences.getPreferences());
		
		rtr.add(lpSolverContainer);
		
		return rtr.toArray(new AbstractUltimatePreferenceItem[rtr.size()]);
	}

	@Override
	protected String getPlugID() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public String getPreferencePageTitle() {
		return Activator.PLUGIN_NAME;
	}

}
