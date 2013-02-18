/*
 * Copyright (C) 2009-2012 University of Freiburg
 *
 * This file is part of SMTInterpol.
 *
 * SMTInterpol is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SMTInterpol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SMTInterpol.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_freiburg.informatik.ultimate.smtinterpol.proof;

import de.uni_freiburg.informatik.ultimate.smtinterpol.dpll.IAnnotation;

/**
 * Proof node representing a leaf in the proof tree. This might either be an
 * input clause or a theory lemma.
 * @author Juergen Christ
 */
public class LeafNode extends ProofNode {
	/// No theory created this node => input clause.
	public final static int NO_THEORY = -1;
	/// Quantifier Instantiation
	public final static int QUANT_INST = -2;
	/// EUF-lemma
	public final static int THEORY_CC = -3;
	/// LA(R/Z)-lemma
	public final static int THEORY_LA = -4;
	/// NO equality propagation
	public final static int EQ = ProofConstants.AUX_EQ;

	private final int m_LeafKind;
	private IAnnotation m_Annotation;
	
	public LeafNode(int leafKind, IAnnotation annot) {
		m_LeafKind = leafKind;
		m_Annotation = annot;
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	/**
	 * Which theory created this leaf node.
	 * @return Identifier for the theory which created this leaf.
	 */
	public int getLeafKind() {
		return m_LeafKind;
	}
	/**
	 * Is this the annotation for a tautology?
	 * @return <code>true</code> if this annotation represents a tautology.
	 */
	public boolean isTautology() {
		return m_LeafKind >= 0;
	}
	/**
	 * Get theory specific annotations.
	 * @return Theory specific annotations.
	 */
	public IAnnotation getTheoryAnnotation() {
		return m_Annotation;
	}
	/**
	 * Set theory specific annotations.
	 * @param annot New theory specific annotations.
	 */
	public void setTheoryAnnotation(IAnnotation annot) {
		m_Annotation = annot;
	}
	public String toString() {
		return "[" + m_Annotation + "]";
	}
}
