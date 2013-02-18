/* WildcardExpression -- Automatically generated by TreeBuilder */

package local.stalin.model.boogie.ast;
import local.stalin.model.IType;

import java.util.List;
/**
 * This can be used as call forall parameter, or as if or
 * while condition. In all other places it is forbidden.
 */
public class WildcardExpression extends Expression {
    private static final long serialVersionUID = 1L;
    /**
     * The default constructor.
     */
    public WildcardExpression() {
    }

    /**
     * The constructor taking initial values.
     * @param type the type of this expression.
     */
    public WildcardExpression(IType type) {
        super(type);
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        return "WildcardExpression";
    }

    public List<Object> getChildren() {
        List<Object> children = super.getChildren();
        return children;
    }
}
