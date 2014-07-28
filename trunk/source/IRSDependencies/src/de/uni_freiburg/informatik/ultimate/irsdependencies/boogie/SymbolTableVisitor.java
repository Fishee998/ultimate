package de.uni_freiburg.informatik.ultimate.irsdependencies.boogie;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieTransformer;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Body;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Procedure;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Unit;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableDeclaration;
import de.uni_freiburg.informatik.ultimate.model.structure.WrapperNode;

public class SymbolTableVisitor extends BoogieTransformer
{

	protected final Logger mLogger;
	protected SymbolTable mSymbolTable;

	protected String mCurrentScopeIdentifier;

	public SymbolTableVisitor(Logger logger)
	{
		super();
		mLogger = logger;
		mSymbolTable = new SymbolTable();
		mCurrentScopeIdentifier = "";
	}

	public SymbolTable getSymbolTable()
	{
		return mSymbolTable;
	}

	public boolean process(IElement root)
	{
		if (root instanceof WrapperNode) {
			Unit unit = (Unit) ((WrapperNode) root).getBacking();
			Declaration[] declarations = unit.getDeclarations();

			for (Declaration decl : declarations) {
				processDeclaration(decl);
			}
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	protected Declaration processDeclaration(Declaration decl)
	{
		if (decl instanceof Procedure) {
			Procedure proc = (Procedure) decl;
			mCurrentScopeIdentifier = proc.getIdentifier();
			addVariables(proc.getInParams());
			addVariables(proc.getOutParams());
		}
		else if (decl instanceof VariableDeclaration) {
			mCurrentScopeIdentifier = "global";
			VariableDeclaration vdecl = (VariableDeclaration) decl;
			
			for (VarList vl : vdecl.getVariables()) {
				for (String varName : vl.getIdentifiers()) {
					mSymbolTable.addGlobalVariable(varName, vl.getType()
							.getBoogieType());
				}
			}
		}
		return super.processDeclaration(decl);
	}

	@Override
	protected Body processBody(Body body)
	{
		addVariables(body.getLocalVars());
		return super.processBody(body);
	}

	private void addVariables(VariableDeclaration[] vdecls)
	{
		for (VariableDeclaration vdecl : vdecls) {
			addVariables(vdecl.getVariables());
		}
	}

	private void addVariables(VarList[] vlists)
	{
		for (VarList vl : vlists) {
			for (String varName : vl.getIdentifiers()) {
				mSymbolTable.addLocalVariable(varName, mCurrentScopeIdentifier,
						vl.getType().getBoogieType());
			}
		}
	}

}
