package de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg;

import de.uni_freiburg.informatik.ultimate.model.boogie.ast.CallStatement;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.BoogieStatementPrettyPrinter;

/**
 * Edge in a recursive control flow graph that represents a procedure call.
 * Opposed to a Summary this represents only the execution from the position
 * directly before the call statement to the initial position of the called
 * procedure.
 * A Call object provides two auxiliary TransitionFormulas m_OldVarsAssignment
 * and m_GlobalVarsAssignment which are used for computing nested interpolants.
 * Let g_1,...,g_n be the global variables modified by the called procedure, 
 * then
 * m_OldVarsAssignment represents the update old(g_1), ... old(g_n):=g_1,...,g_n
 * and 
 * m_GlobalVarsAssignment represents the update g_1,...,g_n:=old(g_1), ... old(g_n)
 * @author heizmann@informatik.uni-freiburg.de
 */
public class Call extends CodeBlock {

	private static final long serialVersionUID = 5047439633229508126L;

	protected TransFormula m_OldVarsAssignment;
	protected TransFormula m_GlobalVarsAssignment;
	protected CallStatement m_CallStatement;
	protected String m_PrettyPrintedStatements;
	
	/**
	 * The published attributes.  Update this and getFieldValue()
	 * if you add new attributes.
	 */
	private final static String[] s_AttribFields = {
		"CallStatement", "PrettyPrintedStatements", "TransitionFormula",
		"GlobalVarsAssignment", "OldVarsAssignment", "OccurenceInCounterexamples"
	};
	
	
	public Call(ProgramPoint source, ProgramPoint target,
			CallStatement st, 
			TransFormula oldVarsAssignment, TransFormula globalVarsAssignment) {
		super(source, target);
		m_CallStatement = st;
		m_PrettyPrintedStatements = BoogieStatementPrettyPrinter.print(st);
		m_OldVarsAssignment = oldVarsAssignment;
		m_GlobalVarsAssignment = globalVarsAssignment;
		updatePayloadName();
	}
	


	@Override
	public CodeBlock getCopy(ProgramPoint source, ProgramPoint target) {
		CodeBlock copy;
		copy = new Call(source, target, 
				m_CallStatement, m_OldVarsAssignment, m_GlobalVarsAssignment);
		copy.setTransitionFormula(getTransitionFormula());
		return copy;
	}
	
	
	
	@Override
	public void updatePayloadName() {
		super.getPayload().setName("call");
	}

	@Override
	protected String[] getFieldNames() {
		return s_AttribFields;
	}
	
	@Override
	protected Object getFieldValue(String field) {
		if (field == "CallStatement") {
			return m_CallStatement;
		}
		else if (field == "PrettyPrintedStatements") {
			return m_PrettyPrintedStatements;
		}
		else if (field == "OldVarsAssignment") {
			return m_OldVarsAssignment;
		}
		else if (field == "GlobalVarsAssignment") {
			return m_GlobalVarsAssignment;
		}
		else {
			return super.getFieldValue(field);
		}
	}

	public CallStatement getCallStatement() {
		return m_CallStatement;
	}
	
	public String getPrettyPrintedStatements() {
		return m_PrettyPrintedStatements;
	}
	
	public TransFormula getOldVarsAssignment() {
		return m_OldVarsAssignment;
	}

	public TransFormula getGlobalVarsAssignment() {
		return m_GlobalVarsAssignment;
	}




}
