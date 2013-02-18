/* RealLiteral -- Automatically generated by TreeBuilder */

package local.stalin.model.boogie.ast;
import local.stalin.model.IType;

import java.util.List;
/**
 * Represents a real literal which is a special form of a expression.
 */
public class RealLiteral extends Expression {
    private static final long serialVersionUID = 1L;
    /**
     * The value given as String. This representation is used to support
     * arbitrarily large numbers. We do not need to compute with them but
     * give them 1-1 to the decision procedure.
     */
    String value;

    /**
     * The constructor taking initial values.
     * @param value the value given as String.
     */
    public RealLiteral(String value) {
        this.value = value;
    }

    /**
     * The constructor taking initial values.
     * @param type the type of this expression.
     * @param value the value given as String.
     */
    public RealLiteral(IType type, String value) {
        super(type);
        this.value = value;
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("RealLiteral").append('[');
        sb.append(value);
        return sb.append(']').toString();
    }

    /**
     * Gets the value given as String. This representation is used to support
     * arbitrarily large numbers. We do not need to compute with them but
     * give them 1-1 to the decision procedure.
     * @return the value given as String.
     */
    public String getValue() {
        return value;
    }

    public List<Object> getChildren() {
        List<Object> children = super.getChildren();
        children.add(value);
        return children;
    }
}
