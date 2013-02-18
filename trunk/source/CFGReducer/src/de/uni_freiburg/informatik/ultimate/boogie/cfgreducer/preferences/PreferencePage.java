package de.uni_freiburg.informatik.ultimate.boogie.cfgreducer.preferences;

import java.io.IOException;

import de.uni_freiburg.informatik.ultimate.boogie.cfgreducer.Activator;
import de.uni_freiburg.informatik.ultimate.boogie.cfgreducer.preferences.PreferenceValues;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {


	public static final Logger logger =  Logger.getLogger(PreferencePage.class);
	
	
	private final ScopedPreferenceStore preferences;

	public PreferencePage(){
		super(GRID);
		preferences = new ScopedPreferenceStore(ConfigurationScope.INSTANCE,Activator.PLUGIN_ID);
		setPreferenceStore(preferences);
	}
	
	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor reduceGraph = new BooleanFieldEditor(PreferenceValues.NAME_REDUCEGRAPH,
				PreferenceValues.LABEL_REDUCEGRAPH,getFieldEditorParent());
		addField(reduceGraph); 
		BooleanFieldEditor mergeParallelEdges = new BooleanFieldEditor(PreferenceValues.NAME_MERGEPARALLELEDGES,
				PreferenceValues.LABEL_MERGEPARALLELEDGES,getFieldEditorParent());
		addField(mergeParallelEdges);
		BooleanFieldEditor idTagEdges = new BooleanFieldEditor(PreferenceValues.NAME_IDTAGEDGES,
				PreferenceValues.LABEL_IDTAGEDGES,getFieldEditorParent());
		addField(idTagEdges);
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean performOk() {
		try{
			preferences.save();
		}catch(IOException ioe){
			logger.warn(ioe);
		}
		return super.performOk();
	}
}
