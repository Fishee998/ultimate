/* LoopInvariantSpecification -- Automatically generated by TreeBuilder */

package local.stalin.model.boogie.ast;

import java.util.List;
/**
 * Represents a loop invariant specification which is a special form of a specification.
 */
public class LoopInvariantSpecification extends Specification {
    private static final long serialVersionUID = 1L;
    /**
     * The formula of this loop invariant specification.
     */
    Expression formula;

    /**
     * The constructor taking initial values.
     * @param filename the filename of this specification.
     * @param lineNr the line nr of this specification.
     * @param isFree true iff this specification is free.
     * @param formula the formula of this loop invariant specification.
     */
    public LoopInvariantSpecification(String filename, int lineNr, boolean isFree, Expression formula) {
        super(filename, lineNr, isFree);
        this.formula = formula;
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LoopInvariantSpecification").append('[');
        sb.append(formula);
        return sb.append(']').toString();
    }

    /**
     * Gets the formula of this loop invariant specification.
     * @return the formula of this loop invariant specification.
     */
    public Expression getFormula() {
        return formula;
    }

    public List<Object> getChildren() {
        List<Object> children = super.getChildren();
        children.add(formula);
        return children;
    }
}
