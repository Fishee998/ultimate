/**
 * Container object to store inferred types.
 * TODO : add a reference to the corresponding CType. That would make type 
 * checking during translation easier!
 */
package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.cHandler.MemoryHandler;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.SFO;
import de.uni_freiburg.informatik.ultimate.model.IType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.ASTType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.ArrayType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.StructType;

/**
 * @author Markus Lindenmann
 * @date 16.06.2012
 */
public class InferredType implements IType {
    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = 4825293857474339818L;
    /**
     * The held type.
     */
    private Type type;

    /**
     * @author Markus Lindenmann
     * @date 16.06.2012
     */
    public enum Type {
        /**
         * Integer.
         */
        Integer,
        /**
         * Boolean.
         */
        Boolean,
        /**
         * Reals.
         */
        Real,
        /**
         * Strings.
         */
        String,
        /**
         * Cannot be inferred or not yet inferred.
         */
        Unknown,
        /**
         * Void type!
         */
        Void,
        /**
         * Struct type.
         */
        Struct,
        /**
         * Pointer type.
         */
        Pointer
    }

    /**
     * Constructor.
     * 
     * @param type
     *            the type.
     */
    public InferredType(Type type) {
        this.type = type;
    }

    /**
     * Constructor.
     * 
     * @param at
     *            the primitive type to convert.
     */
    public InferredType(ASTType at) {
        if (at == MemoryHandler.POINTER_TYPE) {
            this.type = Type.Pointer;
        } else if (at instanceof ArrayType) {
            InferredType it = new InferredType(((ArrayType) at).getValueType());
            this.type = it.getType();
        } else if (at instanceof StructType) {
            this.type = Type.Struct;
        } else { // Primitive Type
            if (at == null) {
                this.type = Type.Unknown;
            } else {
                String s = at.toString();
                s = s.replaceFirst(at.getClass().getSimpleName(), SFO.EMPTY);
                s = s.replace("[", SFO.EMPTY);
                s = s.replace("]", SFO.EMPTY);
                if (s.equals(SFO.BOOL)) {
                    this.type = Type.Boolean;
                } else if (s.equals(SFO.INT)) {
                    this.type = Type.Integer;
                } else if (s.equals(SFO.REAL)) {
                    this.type = Type.Real;
                } else {
                    this.type = Type.Unknown;
                }
            }
        }
    }

    /**
     * Returns the type.
     * 
     * @return the type.
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        switch (getType()) {
            case Boolean:
                return SFO.BOOL;
            case Integer:
                return SFO.INT;
            case Real:
                return SFO.REAL;
            case String:
                return SFO.STRING;
            case Pointer:
                return SFO.POINTER;
            case Struct:
            case Unknown:
            default:
                break;
        }
        return SFO.UNKNOWN;
    }
}
