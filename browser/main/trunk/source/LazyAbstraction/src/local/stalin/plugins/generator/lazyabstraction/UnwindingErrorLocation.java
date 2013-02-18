/**
 * 
 */
package local.stalin.plugins.generator.lazyabstraction;

import local.stalin.boogie.cfgreducer.CFGExplicitNode;
import local.stalin.boogie.cfgreducer.SMTNodeAnnotations;
import local.stalin.logic.Atom;
import local.stalin.logic.Formula;
import local.stalin.logic.Theory;
import local.stalin.model.Payload;

/**
 * @author alexander
 *
 */
public class UnwindingErrorLocation extends UnwindingNode {

	private static final long serialVersionUID = -3424915526982907232L;

	/**
	 * 
	 */
	public UnwindingErrorLocation() {
		Payload pl = new Payload();
		pl.setName("Error");
		SMTNodeAnnotations sna = new SMTNodeAnnotations();
		sna.setAssertion(Atom.TRUE);
		pl.getAnnotations().put("SMT", sna);
		this.setPayload(pl);
		m_cfgLocation = new CFGExplicitNode(null, null); //error nodes have no corresponding cfgExplicitNode
	}

	/**
	 * @param procRoot
	 * @param cfgNode
	 */
	public UnwindingErrorLocation(UnwindingProcRoot procRoot,
			CFGExplicitNode cfgNode) {
		super(procRoot, cfgNode);
		Payload pl = new Payload();
		pl.setName("Error");
		SMTNodeAnnotations sna = new SMTNodeAnnotations();
		sna.setAssertion(Atom.TRUE);
		pl.getAnnotations().put("SMT", sna);
		this.setPayload(pl);
		m_cfgLocation = new CFGExplicitNode(null, null); //error nodes have no corresponding cfgExplicitNode
	}

	/**
	 * @param theory
	 * @param formula
	 */
	public UnwindingErrorLocation(Theory theory, Formula formula) {
		super(theory, formula);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param theory
	 * @param formula
	 * @param procRoot
	 * @param cfgNode
	 */
//	public UnwindingErrorLocation(Theory theory, Formula formula,
//			UnwindingProcRoot procRoot, CFGExplicitNode cfgNode) {
//		super(theory, formula, procRoot, cfgNode);
//		// TODO Auto-generated constructor stub
//	}

}
