package de.uni_freiburg.informatik.ultimate.core.coreplugin.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ToolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{

	@Override
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
		
	}

	@Override
	protected Control createContents(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
