package de.uni_freiburg.informatik.ultimate.automata.nwalibrary.visualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.model.AbstractEdgeNode;
import de.uni_freiburg.informatik.ultimate.model.DefaultAnnotations;
import de.uni_freiburg.informatik.ultimate.model.IAnnotations;
import de.uni_freiburg.informatik.ultimate.model.IEdge;
import de.uni_freiburg.informatik.ultimate.model.INode;
import de.uni_freiburg.informatik.ultimate.model.Payload;

/**
 * Ultimate model of an automaton state.
 * @author heizmann@informatik.uni-freiburg.de 
 */

public class AutomatonState extends AbstractEdgeNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 264254789648279608L;
	private List<IEdge> incoming = new ArrayList<IEdge>();
	private List<IEdge> outgoing = new ArrayList<IEdge>();
	
	public AutomatonState(Object content, boolean isAccepting) {
		
		Payload payload = new Payload();
		DefaultAnnotations acceptance = new DefaultAnnotations();
		acceptance.put("isAccepting",isAccepting);
		HashMap<String,IAnnotations> annotations = 
				new HashMap<String,IAnnotations>();
		annotations.put("isAccepting", acceptance);
		
		if (content instanceof IAnnotations) {
			annotations.put("Content", (IAnnotations) content);

		}
		if (content != null) {
			payload.setName(content.toString());
		}
		payload.setAnnotations(annotations);
		super.setPayload(payload);
	}
	
	public List<IEdge> getIncomingEdges() {
		return incoming;
	}
	
	public boolean addIncomingEdge(IEdge element) {
		return incoming.add(element);
	}
	
	public List<IEdge> getOutgoingEdges() {
		return outgoing;
	}
	
	public boolean addOutgoingEdge(IEdge element) {
		return outgoing.add(element);
	}
	
	public String toString() {
		return super.getPayload().getName();
	}

	@Override
	public boolean addOutgoingEdge(INode target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAllIncoming() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAllOutgoing() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIncomingEdge(IEdge element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeOutgoingEdge(IEdge element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addIncomingEdge(INode src) {
		throw new UnsupportedOperationException();
	}
	


}
