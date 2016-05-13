/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Jan Leike (leike@informatik.uni-freiburg.de)
 * Copyright (C) 2013-2015 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni_freiburg.informatik.ultimate.core.services.model.IBacktranslationService;
import de.uni_freiburg.informatik.ultimate.models.IElement;
import de.uni_freiburg.informatik.ultimate.models.ILocation;
import de.uni_freiburg.informatik.ultimate.models.annotation.Check;
import de.uni_freiburg.informatik.ultimate.models.annotation.IAnnotations;
import de.uni_freiburg.informatik.ultimate.result.model.IResult;
import de.uni_freiburg.informatik.ultimate.translation.AtomicTraceElement;
import de.uni_freiburg.informatik.ultimate.translation.IProgramExecution;

/**
 * 
 * @author Matthias Heizmann
 * @author Jan Leike
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 */
public class ResultUtil {

	public static <TE extends IElement, E> List<ILocation> getLocationSequence(IProgramExecution<TE, E> pe) {
		List<ILocation> result = new ArrayList<>();
		for (int i = 0; i < pe.getLength(); i++) {
			AtomicTraceElement<TE> te = pe.getTraceElement(i);
			result.add(te.getTraceElement().getPayload().getLocation());
		}
		return result;
	}

	/**
	 * Returns new Collections that contains all IResults from ultimateIResults that are subclasses of the class
	 * resClass.
	 */
	public static <E extends IResult> Collection<E> filterResults(Map<String, List<IResult>> ultimateIResults,
			Class<E> resClass) {
		ArrayList<E> filteredList = new ArrayList<E>();
		for (Entry<String, List<IResult>> entry : ultimateIResults.entrySet()) {
			for (IResult res : entry.getValue()) {
				if (resClass.isAssignableFrom(res.getClass())) {
					// if (res.getClass().isAssignableFrom(resClass)) {
					@SuppressWarnings("unchecked")
					E benchmarkResult = (E) res;
					filteredList.add(benchmarkResult);
				}
			}
		}
		return filteredList;
	}

	public static <SE> String translateExpressionToString(IBacktranslationService translator, Class<SE> clazz,
			SE[] expression) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < expression.length; ++i) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(translator.translateExpressionToString(expression[i], clazz));
		}
		return sb.toString();
	}

	/**
	 * Return the checked specification that is checked at the error location.
	 */
	public static <ELEM extends IElement> Check getCheckedSpecification(ELEM element) {
		final IAnnotations check;
		if (element.getPayload().hasAnnotation()) {
			check = element.getPayload().getAnnotations().get(Check.getIdentifier());
			if (check instanceof Check) {
				return (Check) check;
			} else {
				return null;
			}
		}
		final ILocation loc = element.getPayload().getLocation();
		if (loc == null) {
			return null;
		}
		ILocation origin = loc.getOrigin();
		if (origin == null) {
			return null;
		}
		check = origin.getCheck();
		if (check instanceof Check) {
			return (Check) check;
		}
		return null;
	}
}