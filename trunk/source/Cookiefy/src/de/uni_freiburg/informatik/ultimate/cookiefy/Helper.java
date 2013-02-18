package de.uni_freiburg.informatik.ultimate.cookiefy;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Procedure;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Specification;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;

/**
 * general helper methods
 * 
 */
public class Helper {

	/**
	 * Concatenates two Statement Arrays.
	 * 
	 * @param stmtArray1
	 * @param stmtArray2
	 * @return
	 */
	static Statement[] concatStatements(Statement[] stmtArray1,
			Statement[] stmtArray2) {
		Statement[] result = new Statement[stmtArray1.length
				+ stmtArray2.length];

		System.arraycopy(stmtArray1, 0, result, 0, stmtArray1.length);
		System.arraycopy(stmtArray2, 0, result, stmtArray1.length,
				stmtArray2.length);

		return result;
	}

	/**
	 * Adds all variables defined in vl as separate VarList entries in al.
	 * Variable identifiers are extended with the given prefix.
	 * 
	 * @param al
	 *            ArrayList, where we add all variable declarations
	 * @param vl
	 *            VarList with (possibly) multiple declarations
	 * @param prefix
	 *            prefix to be added to an variable identifier
	 */
	static void addVarListToArrayList(List<VarList> al, VarList vl,
			String prefix) {
		for (String identifier : vl.getIdentifiers()) {
			al.add(new VarList(LocationProvider.getLocation(),
					new String[] { prefix + identifier }, vl.getType()));
		}
	}

	static void addVarListToIdentifierList(List<Expression> al, VarList vl) {
		for (String identifier : vl.getIdentifiers()) {
			al.add(new IdentifierExpression(LocationProvider.getLocation(),
					identifier));
		}
	}

	/**
	 * Returns a new Procedure with additionally specifications (for example a
	 * new "modifies")
	 * 
	 * @param p
	 * @param specs
	 * @return
	 */
	static Procedure ExtendProcedure(Procedure p, Specification[] specs) {
		Specification[] newSpecs = new Specification[p.getSpecification().length
				+ specs.length];

		System.arraycopy(p.getSpecification(), 0, newSpecs, 0,
				p.getSpecification().length);
		System.arraycopy(specs, 0, newSpecs, p.getSpecification().length,
				specs.length);

		return new Procedure(LocationProvider.getLocation(), p.getAttributes(),
				p.getIdentifier(), p.getTypeParams(), p.getInParams(),
				p.getOutParams(), newSpecs, p.getBody());
	}
}
