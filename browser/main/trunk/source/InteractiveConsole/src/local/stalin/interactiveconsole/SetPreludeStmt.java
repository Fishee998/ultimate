package local.stalin.interactiveconsole;

import local.stalin.core.api.PreludeProvider;

/**
 * interactive console statement responsible for setting the prelude file
 * 
 * @author Christian Simon
 *
 */
public class SetPreludeStmt extends Stmt {

	private String prelude;
	
	public SetPreludeStmt(String preludefile) {
		this.prelude = preludefile;
	}
	
	@Override
	public void execute() {
		this.controller.setPrelude(new PreludeProvider(this.prelude));
	}

}
