/*
 * Copyright (C) 2016 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2016 University of Freiburg
 *
 * This file is part of the ULTIMATE IcfgTransformer library.
 *
 * The ULTIMATE IcfgTransformer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE IcfgTransformer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE IcfgTransformer library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE IcfgTransformer library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE IcfgTransformer grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.icfgtransformer;

import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.IIcfgSymbolTable;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.structure.IIcfg;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.TransFormula;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.cfg.transitions.UnmodifiableTransFormula;

/**
 * A {@link ITransformulaTransformer} produces {@link UnmodifiableTransFormula}s for a certain {@link IIcfg}.
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
public interface ITransformulaTransformer {

	/**
	 * Do pre-processing that is required to transform {@link TransFormula}s. By default the preprocessing does not do
	 * anything.
	 * 
	 * Note that a preprocessing should always be non-destructive!
	 * 
	 * @param icfg
	 *            The {@link IIcfg} instance that contains all the transformulas this {@link ITransformulaTransformer}
	 *            will see during his life cycle.
	 */
	void preprocessIcfg(final IIcfg<?> icfg);

	/**
	 * Transform an {@link UnmodifiableTransFormula} to another (possibly equivalent) {@link UnmodifiableTransFormula}.
	 * 
	 * @param tf
	 *            the transformula that should be transformed.
	 * @return The result of the transformation through this transformer.
	 */
	TransforumlaTransformationResult transform(final UnmodifiableTransFormula tf);

	/**
	 * @return A human-friendly name that can be used during debugging, e.g., if many transformers run after another.
	 */
	String getName();

	/**
	 * 
	 * @return Symbol table of the result CFG. Can be obtained only after the translation.
	 */
	IIcfgSymbolTable getNewIcfgSymbolTable();

	/**
	 * The result of an {@link ITransformulaTransformer#transform(UnmodifiableTransFormula)} operation.
	 * 
	 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
	 *
	 */
	public static class TransforumlaTransformationResult {
		private final UnmodifiableTransFormula mTransformedTransformula;
		private final boolean mIsOverapproximation;

		/**
		 * The transformed transformula is equivalent.
		 * 
		 * @param transformedTransformula
		 *            the transformula.
		 */
		public TransforumlaTransformationResult(final UnmodifiableTransFormula transformedTransformula) {
			mTransformedTransformula = transformedTransformula;
			mIsOverapproximation = false;
		}

		/**
		 * State that the transformed transformula is an overapproximation or equivalent.
		 * 
		 * @param transformedTransformula
		 *            the transformula.
		 * @param isOverappoximation
		 *            true iff the transformula is an overapproximation of, false if it is equivalent to the original
		 *            transformula.
		 */
		public TransforumlaTransformationResult(final UnmodifiableTransFormula transformedTransformula,
				final boolean isOverappoximation) {
			mTransformedTransformula = transformedTransformula;
			mIsOverapproximation = isOverappoximation;
		}

		public UnmodifiableTransFormula getTransformula() {
			return mTransformedTransformula;
		}

		public boolean isOverapproximation() {
			return mIsOverapproximation;
		}
	}
}
