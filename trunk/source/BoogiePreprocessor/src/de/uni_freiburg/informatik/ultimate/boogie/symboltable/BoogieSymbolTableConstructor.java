package de.uni_freiburg.informatik.ultimate.boogie.symboltable;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.access.IUnmanagedObserver;
import de.uni_freiburg.informatik.ultimate.access.WalkerOptions;
import de.uni_freiburg.informatik.ultimate.boogie.preprocessor.Activator;
import de.uni_freiburg.informatik.ultimate.boogie.type.PreprocessorAnnotation;
import de.uni_freiburg.informatik.ultimate.core.api.UltimateServices;
import de.uni_freiburg.informatik.ultimate.model.IElement;
import de.uni_freiburg.informatik.ultimate.model.boogie.BoogieVisitor;
import de.uni_freiburg.informatik.ultimate.model.boogie.DeclarationInformation.StorageClass;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.FunctionDeclaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Procedure;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Unit;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableDeclaration;

/**
 * 
 * @author dietsch
 *
 */
public class BoogieSymbolTableConstructor extends BoogieVisitor implements IUnmanagedObserver {

	private Logger mLogger;
	private BoogieSymbolTable mSymbolTable;

	private Unit mRootNode;

	private StorageClass mCurrentScope;
	private Declaration mCurrentDeclaration;
	private String mCurrentScopeName;

	@Override
	public void init() throws Throwable {
		mLogger = UltimateServices.getInstance().getLogger(Activator.PLUGIN_ID);
		mSymbolTable = new BoogieSymbolTable();
		mCurrentScope = StorageClass.GLOBAL;
		mCurrentDeclaration = null;
		mCurrentScopeName = null;
		mRootNode = null;
	}

	@Override
	public void finish() throws Throwable {
		PreprocessorAnnotation pa = new PreprocessorAnnotation();
		pa.setSymbolTable(mSymbolTable);
		pa.annotate(mRootNode);
		mLogger.debug(String.format("SymbolTable\r%s", mSymbolTable.prettyPrintSymbolTable()));
	}

	@Override
	public WalkerOptions getWalkerOptions() {
		return null;
	}

	@Override
	public boolean performedChanges() {
		return false;
	}

	public BoogieSymbolTable getSymbolTable() {
		return mSymbolTable;

	}

	@Override
	public boolean process(IElement root) throws Throwable {
		if (root instanceof Unit) {
			mRootNode = (Unit) root;
			for (Declaration decl : mRootNode.getDeclarations()) {
				if (decl instanceof VariableDeclaration) {
					mCurrentScope = StorageClass.GLOBAL;
					mCurrentDeclaration = decl;
				}
				processDeclaration(decl);
			}
			return false;
		}
		return true;
	}

	@Override
	protected void visit(FunctionDeclaration decl) {
		mCurrentDeclaration = decl;
		mCurrentScope = StorageClass.PROCEDURE;
		mCurrentScopeName = decl.getIdentifier();
		mSymbolTable.addProcedureOrFunction(decl.getIdentifier(), decl);

		if (decl.getInParams() != null) {
			for (VarList vl : decl.getInParams()) {
				for (String name : vl.getIdentifiers()) {
					mSymbolTable.addInParams(decl.getIdentifier(), name, decl);
				}
			}
		}

		if (decl.getOutParam() != null) {
			for (String name : decl.getOutParam().getIdentifiers()) {
				mSymbolTable.addOutParams(decl.getIdentifier(), name, decl);
			}
		}

		super.visit(decl);
	}

	@Override
	protected void visit(Procedure decl) {
		mCurrentDeclaration = decl;
		mCurrentScope = StorageClass.PROCEDURE;
		mCurrentScopeName = decl.getIdentifier();
		mSymbolTable.addProcedureOrFunction(decl.getIdentifier(), decl);

		if (decl.getInParams() != null) {
			for (VarList vl : decl.getInParams()) {
				for (String name : vl.getIdentifiers()) {
					mSymbolTable.addInParams(decl.getIdentifier(), name, decl);
				}
			}
		}

		if (decl.getOutParams() != null) {
			for (VarList vl : decl.getOutParams()) {
				for (String name : vl.getIdentifiers()) {
					mSymbolTable.addOutParams(decl.getIdentifier(), name, decl);
				}
			}
		}

		// TODO What about type params?
		super.visit(decl);
	}

	@Override
	protected VariableDeclaration processLocalVariableDeclaration(VariableDeclaration local) {
		mCurrentDeclaration = local;
		mCurrentScope = StorageClass.LOCAL;
		return super.processLocalVariableDeclaration(local);
	}

	@Override
	protected VarList processVarList(VarList vl) {
		switch (mCurrentScope) {
		case LOCAL:
			for (String name : vl.getIdentifiers()) {
				mSymbolTable.addLocalVariable(mCurrentScopeName, name, mCurrentDeclaration);
			}
			break;
		case GLOBAL:
			for (String name : vl.getIdentifiers()) {
				mSymbolTable.addGlobalVariable(name, mCurrentDeclaration);
			}
			break;
		case PROCEDURE:
			break;
		default:
			throw new UnsupportedOperationException(String.format("Extend this method for the new scope %s",
					mCurrentScope));
		}
		return super.processVarList(vl);
	}

}
