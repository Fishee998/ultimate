/* Parameter -- Automatically generated by TreeBuilder */

package local.stalin.astbuilder;

/**
 * Represents a parameter.
 */
public class Parameter {
    /**
     * The name of this parameter.
     */
    String name;

    /**
     * The type of this parameter.
     */
    String type;

    /**
     * The comment of this parameter.
     */
    String comment;

    /**
     * True iff this parameter is writeable.
     */
    boolean isWriteable;

    /**
     * True iff this parameter is optional.
     */
    boolean isOptional;

    /**
     * The constructor taking initial values.
     * @param name the name of this parameter.
     * @param type the type of this parameter.
     * @param comment the comment of this parameter.
     * @param isWriteable true iff this parameter is writeable.
     * @param isOptional true iff this parameter is optional.
     */
    public Parameter(String name, String type, String comment, boolean isWriteable, boolean isOptional) {
        this.name = name;
        this.type = type;
        this.comment = comment;
        this.isWriteable = isWriteable;
        this.isOptional = isOptional;
    }

    /**
     * Returns a textual description of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Parameter").append('[');
        sb.append(name);
        sb.append(',').append(type);
        sb.append(',').append(comment);
        sb.append(',').append(isWriteable);
        sb.append(',').append(isOptional);
        return sb.append(']').toString();
    }

    /**
     * Gets the name of this parameter.
     * @return the name of this parameter.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this parameter.
     * @return the type of this parameter.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this parameter.
     * @param type the type of this parameter.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the comment of this parameter.
     * @return the comment of this parameter.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Checks iff this parameter is writeable.
     * @return true iff this parameter is writeable.
     */
    public boolean isWriteable() {
        return isWriteable;
    }

    /**
     * Checks iff this parameter is optional.
     * @return true iff this parameter is optional.
     */
    public boolean isOptional() {
        return isOptional;
    }
}
