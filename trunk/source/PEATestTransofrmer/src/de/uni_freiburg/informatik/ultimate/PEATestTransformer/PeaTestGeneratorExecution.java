package de.uni_freiburg.informatik.ultimate.PEATestTransformer;

import java.util.HashSet;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.Expression;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.IdentifierExpression;
import de.uni_freiburg.informatik.ultimate.model.boogie.output.BoogiePrettyPrinter;
import de.uni_freiburg.informatik.ultimate.result.AtomicTraceElement;
import de.uni_freiburg.informatik.ultimate.result.model.IProgramExecution;
import pea.BoogieBooleanExpressionDecision;
import pea.CDD;
import pea.Phase;
import pea_to_boogie.translator.CDDTranslator;

public class PeaTestGeneratorExecution implements IProgramExecution<BoogieASTNode, Expression> {
	
	private List<ProgramState<Expression>> states;
	private SystemInformation sysInfo;
	private List<HashSet<Phase>> phases;
	
	public PeaTestGeneratorExecution(List<ProgramState<Expression>> states, List<HashSet<Phase>> phases, SystemInformation sysInfo){
		this.sysInfo = sysInfo;
		this.states = states;
		this.phases = phases;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		//for each state makes a list of variables
		out.append("####################################### Test Sequence ###############################################\n");
		for(int i = 1; i < this.states.size() ; i++){
			out.append("-------------------------------- Step ------------------------------------------------------\n");
			out.append(this.reportChanges(this.states.get(i-1), this.states.get(i)));
			out.append(this.reportOracle(this.phases.get(i)));
			out.append("\n");
		}
		return out.toString();
	}
	
	private String reportOracle(HashSet<Phase> phases){
		StringBuilder out = new StringBuilder();
		out.append(" \n");
		for(Phase p: phases){
			out.append(" ------ from Req ");
			out.append(((BoogieBooleanExpressionDecision)p.getStateInvariant().getDecision()).getExpression().getLocation().getStartLine());
			out.append(" :			");
			out.append(BoogiePrettyPrinter.print(new CDDTranslator().CDD_To_Boogie(p.getStateInvariant(),"",null)));
			
			out.append(" \n");
		}
		return out.toString();
	}
	
	// compare last state with next state and only print the non internal non primed variabels
	private String reportChanges(ProgramState<Expression> oldState,ProgramState<Expression> state){
		StringBuilder out = new StringBuilder("");
		String ident;
		//TODO: this loop makes variable selection hacky, make nicer!
		out.append("INPUT :");
		for(Expression variable: state.getVariables()){
			ident = ((IdentifierExpression)variable).getIdentifier();
			if(!this.sysInfo.isInput(ident)|| ident.endsWith("'")) continue;
			if(!oldState.getVariables().contains(variable) || oldState.getValues(variable) != state.getValues(variable)){
				out.append(BoogiePrettyPrinter.print(variable) +"="+ 
			BoogiePrettyPrinter.print(state.getValues(variable).stream().findFirst().get())
				+"    ");
			}
		}
		out.append("\nOUTPUT:");
		for(Expression variable: state.getVariables()){
			ident = ((IdentifierExpression)variable).getIdentifier();
			if(!this.sysInfo.isOutput(ident)|| ident.endsWith("'")) continue;
			if(!oldState.getVariables().contains(variable) || oldState.getValues(variable) != state.getValues(variable)){
				out.append(BoogiePrettyPrinter.print(variable) +"="+ 
			BoogiePrettyPrinter.print(state.getValues(variable).stream().findFirst().get())
				+"    "); 
			}
		}
		out.append("\nINTERNALS:");
		for(Expression variable: state.getVariables()){
			ident = ((IdentifierExpression)variable).getIdentifier();
			if(!this.sysInfo.isInternal(ident)|| ident.endsWith("'")) continue;
			if(!oldState.getVariables().contains(variable) || oldState.getValues(variable) != state.getValues(variable)){
				out.append(BoogiePrettyPrinter.print(variable) +"="+ 
			BoogiePrettyPrinter.print(state.getValues(variable).stream().findFirst().get())
				+"    ");
			}
		}
		return out.toString();
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AtomicTraceElement<BoogieASTNode> getTraceElement(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProgramState<Expression> getProgramState(
			int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProgramState<Expression> getInitialProgramState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<Expression> getExpressionClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<BoogieASTNode> getTraceElementClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSVCOMPWitnessString() {
		// TODO Auto-generated method stub
		return null;
	}

}
