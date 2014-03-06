package de.uni_freiburg.informatik.ultimate.model.boogie;


/**
 * The class is used to store information about where we can find the 
 * declaration of an IdentifierExpression of a VariableLeftHandSide.  
 * @author Matthias Heizmann
 */
public class DeclarationInformation {
	
	/**
	 * Defines where the declaration of a variable/constant is stored. 
	 */
	public static enum StorageClass { 
		GLOBAL, 
		PROCEDURE_INPARAM, 
		PROCEDURE_OUTPARAM,
		IMPLEMENTATION_INPARAM, 
		IMPLEMENTATION_OUTPARAM,
		LOCAL, 
		QUANTIFIED, 
		IMPLEMENTATION,
		PROCEDURE	
	}
	
	private final StorageClass m_StorageClass;
	private final String m_Procedure;
	public DeclarationInformation(StorageClass storageClass,
			String procedure) {
		super();
		assert (isValid(storageClass, procedure));
		this.m_StorageClass = storageClass;
		this.m_Procedure = procedure;
	}
	public StorageClass getStorageClass() {
		return m_StorageClass;
	}
	public String getProcedure() {
		return m_Procedure;
	}
	
	/**
	 * A DeclarationInformation is valid, if the procedure is non-null exactly
	 * if the StorageClass corresponds to a procedure.
	 */
	private boolean isValid(StorageClass storageClass, String procedure) {
		final boolean result;
		switch (storageClass) {
		case IMPLEMENTATION:
		case PROCEDURE:
		case GLOBAL:
		case QUANTIFIED:
			result = (procedure == null);
			break;
		case PROCEDURE_INPARAM: 
		case PROCEDURE_OUTPARAM:
		case IMPLEMENTATION_INPARAM: 
		case IMPLEMENTATION_OUTPARAM:
		case LOCAL:
			result = (procedure != null);
			break;
		default:
			throw new AssertionError("unknown StorageClass");
		}
		return result;
	}
}
