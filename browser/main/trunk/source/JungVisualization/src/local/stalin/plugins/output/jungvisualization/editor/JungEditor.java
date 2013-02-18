package local.stalin.plugins.output.jungvisualization.editor;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Frame;

import javax.swing.JPanel;

import local.stalin.model.IEdge;
import local.stalin.model.INode;
import local.stalin.plugins.output.jungvisualization.actions.MenuActions;
import local.stalin.plugins.output.jungvisualization.graph.GraphHandler;
import local.stalin.plugins.output.jungvisualization.graph.GraphListener;
import local.stalin.plugins.output.jungvisualization.selection.JungSelectionProvider;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;


/**
 * Defines an editor for the JungVisualization plug-in.
 * @see {@link EditorPart} 
 * @see {@link IPartListener}
 * @author lenalena
 */
public class JungEditor extends EditorPart implements IPartListener {

	public static final String ID = "local.stalin.plugins.output.jungvisualization.editor.JungEditor";
	public static final String VV_ID_EDITOR_PROPERTY_KEY = "VisualizationViewerID";
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(((JungEditorInput) input).getName());
		site.getWorkbenchWindow().getPartService().addPartListener(this);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {

		String currentVVID = GraphHandler.getInstance().getLastCreatedVisualizationViewerID();
		this.setPartProperty(VV_ID_EDITOR_PROPERTY_KEY, currentVVID);
		
		Composite comp = new Composite(parent,SWT.EMBEDDED);
		Frame awt = SWT_AWT.new_Frame(comp);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		final VisualizationViewer<INode, IEdge> vv = GraphHandler.getInstance().getVisualizationViewer(currentVVID);
		vv.setPreferredSize(panel.getSize());

		JungSelectionProvider jsp = new JungSelectionProvider();
		getSite().setSelectionProvider(jsp);
		
		GraphListener gl = new GraphListener(jsp);
	    
		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setZoomAtMouse(true);
		
		if (System.getProperty(MenuActions.SYSTEM_MODE) == null || System.getProperty(MenuActions.SYSTEM_MODE).equals(MenuActions.MODE_PICKING)){
			MenuActions.setMode(MenuActions.MODE_PICKING);
			graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		}
		else {
			MenuActions.setMode(MenuActions.MODE_TRANSFORMING);
			graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		}
		
		graphMouse.add(gl);

		vv.setGraphMouse(graphMouse);
		
		panel.add(vv, BorderLayout.CENTER);
		
        panel.setVisible(true);
        awt.add(panel);
        
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		GraphHandler.getInstance().setLastCreatedEditor(this);
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part == this){
			GraphHandler.getInstance().removeVisualizationViewer(this.getPartProperty(VV_ID_EDITOR_PROPERTY_KEY));
			part.getSite().getWorkbenchWindow().getPartService().removePartListener(this);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
		
}
