package de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.base.cHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDesignatedInitializer;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFieldDesignator;

import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.CACSLLocation;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.InferredType;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CArray;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CNamed;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CStruct;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.container.c.CType;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.exception.IncorrectSyntaxException;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.exception.UnsupportedSyntaxException;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.Result;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ResultExpression;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.result.ResultExpressionListRec;
import de.uni_freiburg.informatik.ultimate.cdt.translation.implementation.util.SFO;
import de.uni_freiburg.informatik.ultimate.cdt.translation.interfaces.Dispatcher;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.ASTType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.ArrayType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.AssignmentStatement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Declaration;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.LeftHandSide;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Statement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.StructAccessExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.StructLHS;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.StructType;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VarList;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.VariableDeclaration;
import de.uni_freiburg.informatik.ultimate.result.SyntaxErrorResult.SyntaxErrorType;

/**
 * Class that handles translation of Structs.
 * 
 * @author Markus Lindenmann
 * @date 12.10.2012
 */
public class StructHandler {

    /**
     * Extracted method to handle IASTSimpleDeclaration holding a
     * StructDeclaration.
     * 
     * @param main
     *            the main dispatcher
     * @param arrayHandler
     *            Reference to the array handler.
     * @param loc
     *            the location of this struct's declaration.
     * @param t
     *            the type of the struct to initialize.
     * @param cvar
     *            the corresponding C variable description.
     * @param lhs
     *            the struct to initialize.
     * @param relr
     *            the initializer-list tree.
     * @param idc
     *            an array list, initially empty.
     * @param pos
     *            initially -1. The current dimension.
     * @return a list of assert and assign statements. Maybe there are also some
     *         declarations of temp. vars.
     */
    public ResultExpression handleStructInit(Dispatcher main,
            ArrayHandler arrayHandler, final CACSLLocation loc, StructType t,
            CStruct cvar, final LeftHandSide lhs, ResultExpressionListRec relr,
            ArrayList<Integer> idc, int pos) {
        ArrayList<Statement> stmt = new ArrayList<Statement>();
        ArrayList<Declaration> decl = new ArrayList<Declaration>();
		Map<VariableDeclaration, CACSLLocation> auxVars = 
				new HashMap<VariableDeclaration, CACSLLocation>();

        String fId = null;
        ASTType fType = null;
        if (pos >= 0) {
            if (relr.field != null) {
                fId = relr.field;
                assert !fId.equals(SFO.EMPTY);
                for (VarList f : t.getFields()) {
                    assert f.getIdentifiers().length == 1;
                    if (f.getIdentifiers()[0].equals(fId)) {
                        fType = f.getType();
                        break;
                    }
                }
                if (fType == null) {
                    String msg = "Field '" + fId + "' not found in type + '"
                            + t + "'";
                    Dispatcher.error(loc, SyntaxErrorType.IncorrectSyntax, msg);
                    throw new IncorrectSyntaxException(msg);
                }
            } else {
                assert idc.get(pos) >= 0 && idc.get(pos) < t.getFields().length;
                VarList field = t.getFields()[idc.get(pos)];
                // index 0 is OK; field only holds one ID by construction!
                assert field.getIdentifiers().length == 1;
                fId = field.getIdentifiers()[0];
                assert !fId.equals(SFO.EMPTY);
                fType = field.getType();
                assert fType != null;
            }
            if (fType instanceof StructType)
                t = (StructType) fType;
        }

        if (relr.list == null) {
            if (relr.decl != null)
                decl.addAll(relr.decl);
            if (relr.stmt != null)
                stmt.addAll(relr.stmt);
            if (relr.expr != null) {
                assert fType != null;
                assert fId != null;
                LeftHandSide assLhs = new StructLHS(loc,
                        new InferredType(fType), lhs, fId);
                relr.expr = main.typeHandler.checkBooleanAssignment(loc, fType,
                        relr.expr);
                stmt.add(new AssignmentStatement(loc,
                        new LeftHandSide[] { assLhs },
                        new Expression[] { relr.expr }));
            }
        } else {
            for (ResultExpressionListRec child : relr.list) {
                if (idc.size() <= pos + 1)
                    idc.add(-1);
                idc.set(pos + 1, idc.get(pos + 1) + 1);
                LeftHandSide newLhs = (fId != null ? new StructLHS(loc, lhs,
                        fId) : lhs);
                ResultExpression r;
                if (fType instanceof ArrayType) {
                    int[] indices = new int[((ArrayType) fType).getIndexTypes().length];
                    Arrays.fill(indices, -1);
                    r = arrayHandler.handleArrayInit(main, this, loc,
                            ((ArrayType) fType),
                            (CArray) cvar.getFieldType(fId), newLhs, child,
                            indices, -1);
                } else {
                    r = handleStructInit(main, arrayHandler, loc, t, cvar,
                            newLhs, child, idc, pos + 1);
                }
                decl.addAll(r.decl);
                stmt.addAll(r.stmt);
                auxVars.putAll(r.auxVars);
            }
            idc.set(pos + 1, -1);
        }
        assert (main.isAuxVarMapcomplete(decl, auxVars));
        return new ResultExpression(stmt, null, decl, auxVars);
    }

    /**
     * Handle IASTFieldReference.
     * 
     * @param main
     *            a reference to the main dispatcher.
     * @param node
     *            the node to translate.
     * @return the translation results.
     */
    public Result handleFieldReference(Dispatcher main, IASTFieldReference node) {
        CACSLLocation loc = new CACSLLocation(node);
        Result r = main.dispatch(node.getFieldOwner());
        assert r instanceof ResultExpression;
        ResultExpression rex = (ResultExpression) r;
        String field = node.getFieldName().getRawSignature();
        // get the type for the accessed field
        InferredType it = main.dispatch(node.getExpressionType());
        StructAccessExpression sae = new StructAccessExpression(
                new CACSLLocation(node), it, rex.expr, field);
		assert (main.isAuxVarMapcomplete(rex.decl, rex.auxVars));
        ResultExpression result = new ResultExpression(rex.stmt, sae, rex.decl, rex.auxVars);
        main.cHandler.getSymbolTable();
        CType cvar = rex.cType;
        if (cvar instanceof CNamed) {
            cvar = ((CNamed) cvar).getUnderlyingType();
        }
        if (cvar == null || !(cvar instanceof CStruct)) {
            String msg = "Incorrect or unexpected field owner!";
            Dispatcher.error(loc, SyntaxErrorType.IncorrectSyntax, msg);
            throw new IncorrectSyntaxException(msg);
        }
        result.cType = ((CStruct) cvar).getFieldType(field);
        return result;
    }

    /**
     * Handle IASTDesignatedInitializer.
     * 
     * @param main
     *            a reference to the main dispatcher.
     * @param node
     *            the node to translate.
     * @return the translation result.
     */
    public Result handleDesignatedInitializer(Dispatcher main,
            CASTDesignatedInitializer node) {
        CACSLLocation loc = new CACSLLocation(node);
        assert node.getDesignators().length == 1;
        assert node.getDesignators()[0] instanceof CASTFieldDesignator;
        CASTFieldDesignator fr = (CASTFieldDesignator) node.getDesignators()[0];
        String id = fr.getName().getRawSignature();
        Result r = main.dispatch(node.getOperand());
        if (r instanceof ResultExpressionListRec) {
            ResultExpressionListRec relr = (ResultExpressionListRec) r;
            if (!relr.list.isEmpty()) {
                assert relr.stmt.isEmpty();
                assert relr.expr == null;
                assert relr.decl.isEmpty();
                ResultExpressionListRec named = new ResultExpressionListRec(id);
                named.list.addAll(relr.list);
                return named;
            }
            return new ResultExpressionListRec(id, relr.stmt, relr.expr,
                    relr.decl, relr.auxVars);
        } else if (r instanceof ResultExpression) {
            ResultExpression rex = (ResultExpression) r;
            return new ResultExpressionListRec(id, rex.stmt, rex.expr, rex.decl, rex.auxVars);
        } else {
            String msg = "Unexpected result";
            Dispatcher.error(loc, SyntaxErrorType.UnsupportedSyntax, msg);
            throw new UnsupportedSyntaxException(msg);
        }
    }
}
