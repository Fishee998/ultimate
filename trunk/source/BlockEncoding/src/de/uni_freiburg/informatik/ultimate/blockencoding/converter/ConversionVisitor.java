/**
 * 
 */
package de.uni_freiburg.informatik.ultimate.blockencoding.converter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.blockencoding.algorithm.visitor.IMinimizationVisitor;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.ConjunctionEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.DisjunctionEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.MinimizedNode;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.ShortcutErrEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.interfaces.IBasicEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.interfaces.ICompositeEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.model.interfaces.IMinimizedEdge;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.interfaces.IRating;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.interfaces.IRatingHeuristic;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.metrics.DisjunctMultiStatementRating;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.metrics.DisjunctVariablesRating;
import de.uni_freiburg.informatik.ultimate.blockencoding.rating.util.EncodingStatistics;
import de.uni_freiburg.informatik.ultimate.core.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.AssumeStatement;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BoogieASTNode;
import de.uni_freiburg.informatik.ultimate.model.boogie.ast.BooleanLiteral;
import de.uni_freiburg.informatik.ultimate.model.location.BoogieLocation;
import de.uni_freiburg.informatik.ultimate.model.location.ILocation;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.Boogie2SMT;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.boogie.ModifiableGlobalVariableManager;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.TransFormulaBuilder;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Call;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CfgBuilder.GotoEdge;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.InterproceduralSequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ParallelComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Return;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.RootNode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.SequentialComposition;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.StatementSequence;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.Summary;

/**
 * This special visitor class is responsible for the conversion from
 * MinimizedEdges and MinimizedNodes, back to ProgramPoint and CodeBlock-Edges.
 * 
 * @author Stefan Wissert
 * 
 */
public class ConversionVisitor implements IMinimizationVisitor {

	private final Logger mLogger;

	private ProgramPoint mStartNode;

	private HashMap<MinimizedNode, ProgramPoint> mRefNodeMap;

	private HashMap<ProgramPoint, ProgramPoint> mOrigToNewMap;

	private HashMap<String, HashMap<String, ProgramPoint>> mLocNodesForAnnot;

	private HashSet<IMinimizedEdge> mVisitedEdges;

	private Boogie2SMT mBoogie2SMT;

	private TransFormulaBuilder mTransFormBuilder;

	private ModifiableGlobalVariableManager mModGlobalVarManager;

	private HashMap<IMinimizedEdge, Integer> mCheckForMultipleFormula;

	private IRatingHeuristic mHeuristic;

	private boolean mLBE;

	private Stack<ArrayList<CodeBlock>> mSeqComposedBlocks;

	private HashSet<IMinimizedEdge> mHasConjunctionAsParent;

	private final IUltimateServiceProvider mServices;

	/**
	 * @param boogie2smt
	 * @param root
	 */
	public ConversionVisitor(Boogie2SMT boogie2smt, RootNode root, IRatingHeuristic heuristic,
			IUltimateServiceProvider services) {
		mServices = services;
		mLogger = mServices.getLoggingService().getLogger(Activator.PLUGIN_ID);
		mRefNodeMap = new HashMap<MinimizedNode, ProgramPoint>();
		mOrigToNewMap = new HashMap<ProgramPoint, ProgramPoint>();
		mLocNodesForAnnot = new HashMap<String, HashMap<String, ProgramPoint>>();
		mVisitedEdges = new HashSet<IMinimizedEdge>();
		mBoogie2SMT = boogie2smt;
		mCheckForMultipleFormula = new HashMap<IMinimizedEdge, Integer>();
		mTransFormBuilder = new TransFormulaBuilder(boogie2smt, mServices);
		mModGlobalVarManager = root.getRootAnnot().getModGlobVarManager();
		mSeqComposedBlocks = new Stack<ArrayList<CodeBlock>>();
		mHasConjunctionAsParent = new HashSet<IMinimizedEdge>();
		if (heuristic == null) {
			mLBE = true;
		} else {
			mLBE = false;
			mHeuristic = heuristic;
		}
	}

	/**
	 * This method have to be called before the visitNode-Method!
	 * 
	 * @param startNode
	 *            initial start point for the conversion
	 */
	public void init(ProgramPoint startNode) {
		mStartNode = startNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.blockencoding.interfaces.visitor.
	 * IRCFGVisitor
	 * #visitNode(de.uni_freiburg.informatik.ultimate.blockencoding.model
	 * .MinimizedNode)
	 */
	@Override
	public void visitNode(MinimizedNode node) {
		this.mVisitedEdges.clear();
		if (mStartNode == null) {
			mLogger.warn("Illegal Execution Behaviour," + "init have to be called, before visitNode()!");
			throw new IllegalStateException("No valid state that startNode == null");
		}
		if (!mRefNodeMap.containsKey(node)) {
			mRefNodeMap.put(node, mStartNode);
		}
		// Start recursion here
		internalVisitNode(node);
	}

	/**
	 * This method runs recursively over all minimized nodes, which are
	 * reachable from the initial node (function head). While doing this we
	 * convert every edge into a valid CodeBlock and every node in a
	 * ProgramPoint. In the end the whole function is translated in a RCFG.
	 * 
	 * @param node
	 *            MinimizedNode to convert
	 */
	private void internalVisitNode(MinimizedNode node) {
		// We have no outgoing edges, so we reached an end of the recursion
		if (node.getOutgoingEdges() == null) {
			return;
		}
		// We now get the Edges according to the rating!
		ArrayList<IMinimizedEdge> edgeList = getEdgesAccordingToRating(node);
		// if edgeList has no entries, we reached an end of the graph
		if (edgeList.size() == 0) {
			return;
		}
		for (IMinimizedEdge edge : edgeList) {
			if (!mVisitedEdges.contains(edge)) {
				mVisitedEdges.add(edge);
				// the minimized edge here has to be converted to a
				// CodeBlock-Edge
				CodeBlock cb = null;
				mCheckForMultipleFormula.clear();
				mHasConjunctionAsParent.clear();
				mSeqComposedBlocks.clear();
				mLogger.debug("New Converted Edge: " + edge + " Source: " + edge.getSource() + " / Target: "
						+ edge.getTarget());
				mLogger.debug("Size of Formula: " + edge.getElementCount());
				// Now we create a converted CodeBlock-edge
				// We add one first sequential composed list level
				mSeqComposedBlocks.push(new ArrayList<CodeBlock>());
				mLogger.info("Start Conversion of new minimized edge:");
				if (edge.getRating() instanceof DisjunctVariablesRating
						|| edge.getRating() instanceof DisjunctMultiStatementRating) {
					Integer[] ratingValues = (Integer[]) edge.getRating().getRatingValueContainer().getValue();
					mLogger.info("Disjunctions: " + ratingValues[0] + " UsedVars: " + ratingValues[1]
							+ " ComputedValue: " + ratingValues[2]);
				}
				// add statistical information
				EncodingStatistics.addToTotalRating(edge.getRating().getRatingValueAsInteger());
				EncodingStatistics.incTotalEdges();
				// Convert IMinimizedEdge to valid RCFGEdge
				cb = convertMinimizedEdge(edge);
				if (cb instanceof GotoEdge) {
					// it is possible that the found replacement, is Goto-Edge,
					// which we have to convert in a valid edge
					cb = replaceGotoEdge(cb, null);
				} else if (edge instanceof ShortcutErrEdge) {
					if (cb instanceof ShortcutCodeBlock) {
						cb = new InterproceduralSequentialComposition(null, null, mBoogie2SMT, mModGlobalVarManager,
								false, false, ((ShortcutCodeBlock) cb).getCodeBlocks(), mLogger, mServices);
					} else {
						throw new IllegalArgumentException("Converted CodeBlock for ShortcutErrEdge"
								+ " is no ShortcutCodeBlock");
					}
				}
				mLogger.debug("<-Converted Formula->: " + cb.getTransitionFormula());
				cb.connectSource(getReferencedNode(edge.getSource()));
				cb.connectTarget(getReferencedNode(edge.getTarget()));
				// now we print out all edges which we added more than two times
				for (IMinimizedEdge key : mCheckForMultipleFormula.keySet()) {
					if (mCheckForMultipleFormula.get(key) >= 2) {
						mLogger.error("Edge: " + key + " Occurence: " + mCheckForMultipleFormula.get(key));
					}
				}
				// Since we convert function by function, we do not need to
				// follow Call- and Return-Edges
				if (edge.isBasicEdge()
						&& (((IBasicEdge) edge).getOriginalEdge() instanceof Call || ((IBasicEdge) edge)
								.getOriginalEdge() instanceof Return)) {
					continue;
				}
				if (edge.getTarget() != null) {
					internalVisitNode(edge.getTarget());
				}
			}
		}
	}

	/**
	 * Here we search the the level of edges, which fulfill the rating boundary.
	 * 
	 * @param node
	 *            the minimized node
	 * @return the edges which fulfill the rating boundary
	 */
	private ArrayList<IMinimizedEdge> getEdgesAccordingToRating(MinimizedNode node) {
		// if we use LBE, we take alway the maximal minimization
		if (mLBE) {
			return new ArrayList<IMinimizedEdge>(node.getMinimalOutgoingEdgeLevel());
		}
		// we iterate over the different edge levels and check the property, we
		// start with the most minimized level (which is LBE)
		for (int i = node.getOutgoingEdgeLevels().size() - 1; i >= 0; i--) {
			SimpleEntry<IRating, List<IMinimizedEdge>> entry = node.getOutgoingEdgeLevels().get(i);
			if (entry.getKey() == null) {
				mLogger.debug("Outgoing edge level is null, should " + "only happen for ULTIMATE.start (" + node + ")");
				return new ArrayList<IMinimizedEdge>();
			}
			// we check if the rated value is okay, for a certain edge level, if
			// not we can use this level
			if (mHeuristic.isRatingBoundReached(entry.getKey(), entry.getValue())) {
				return new ArrayList<IMinimizedEdge>(entry.getValue());
			}
		}
		// We should never reach this state here, because there should exist at
		// least one edge level which is below the boundary!
		throw new IllegalStateException("No Outgoing-Edge-Level is below the boundary, should not happen!");
	}

	/**
	 * We put into our reference map to a minimized node a new ProgramPoint
	 * which is used later on during the conversion, and then we return it. the
	 * access on the map, should always be handled by this method.
	 * 
	 * @param node
	 *            the minimized Node to convert
	 * @return the created ProgramPoint
	 */
	public ProgramPoint getReferencedNode(MinimizedNode node) {
		if (mRefNodeMap.containsKey(node)) {
			return mRefNodeMap.get(node);
		} else {
			BoogieASTNode BoogieASTNode = node.getOriginalNode().getBoogieASTNode();
			if (BoogieASTNode == null && node.getOriginalNode().getPayload().hasLocation()) {
				ILocation loc = node.getOriginalNode().getPayload().getLocation();
				if (loc instanceof BoogieLocation) {
					BoogieASTNode = ((BoogieLocation) loc).getBoogieASTNode();
					if (loc.getOrigin() != null) {
						// we have to update the ast node with the original
						// location
						BoogieASTNode.getPayload().setLocation(loc.getOrigin());
					}
				}
			}
			ProgramPoint newNode = new ProgramPoint(node.getOriginalNode().getPosition(), node.getOriginalNode()
					.getProcedure(), node.getOriginalNode().isErrorLocation(), BoogieASTNode);
			mRefNodeMap.put(node, newNode);
			// to reset the rootAnnot, we need to keep a map from the original
			// program points, to the new ones. And since we only create
			// ProgramPoints here it is the right place to store it.
			mOrigToNewMap.put(node.getOriginalNode(), newNode);
			// In addition we also have to fill the map which stores every
			// ProgramPoint in relation to its name and the procedure name
			if (mLocNodesForAnnot.containsKey(newNode.getProcedure())) {
				mLocNodesForAnnot.get(newNode.getProcedure()).put(newNode.getLocationName(), newNode);
			} else {
				HashMap<String, ProgramPoint> newMap = new HashMap<String, ProgramPoint>();
				newMap.put(newNode.getLocationName(), newNode);
				mLocNodesForAnnot.put(newNode.getProcedure(), newMap);
			}
			return newNode;
		}
	}

	/**
	 * This recursive method, converts a MinimizedEdge into a valid CodeBlock.
	 * While doing this, the method uses "Sequential" and "Parallel"
	 * Composition.
	 * 
	 * @param edge
	 *            the minimized edge to convert
	 * @return a converted CodeBlock
	 */
	private CodeBlock convertMinimizedEdge(IMinimizedEdge edge) {
		if (mCheckForMultipleFormula.containsKey(edge)) {
			mCheckForMultipleFormula.put(edge, ((Integer) mCheckForMultipleFormula.get(edge)) + 1);
		} else {
			mCheckForMultipleFormula.put(edge, 1);
		}
		// We build a CodeBlock using Recursion
		// We reach one end if we have an BasicEdge
		if (edge.isBasicEdge()) {
			return convertBasicEdge(edge);
		}

		if (edge instanceof ICompositeEdge) {
			IMinimizedEdge[] edges = ((ICompositeEdge) edge).getCompositeEdges();
			if (edge instanceof ConjunctionEdge) {
				// Since we want to compose sequential edges complete we
				// remember which sub-Edges has a conjunction as parent
				mHasConjunctionAsParent.add(edges[0]);
				mHasConjunctionAsParent.add(edges[1]);
			}
			if (edge instanceof DisjunctionEdge) {
				// When we have a disjunction we have possible two conjunctions
				// at both branches of this. So we have to create two new lists
				// on the stack.
				mSeqComposedBlocks.push(new ArrayList<CodeBlock>());
				mSeqComposedBlocks.push(new ArrayList<CodeBlock>());
			}
			ArrayList<CodeBlock> recConvEdges = new ArrayList<CodeBlock>();
			for (IMinimizedEdge compEdge : edges) {
				CodeBlock convEdge = convertMinimizedEdge(compEdge);
				if (edge instanceof ConjunctionEdge && convEdge != null) {
					// add on the actual list of the stack
					mSeqComposedBlocks.peek().add(convEdge);
				}
				if (convEdge instanceof Summary) {
					// we ignore Summary-Edges
					continue;
				}
				if (convEdge != null) {
					// we simply ignore null edges
					recConvEdges.add(convEdge);
				}
			}
			// some controlling here, if there are no converted edges, there
			// should be edges to compose sequentially
			if (recConvEdges.isEmpty() && mSeqComposedBlocks.isEmpty()) {
				mLogger.error("Conversion fails, both sides are null (" + edges[0] + " -- " + edges[1] + ")");
				throw new IllegalStateException("Conversion failure, both sides are null"
						+ " / and there are no seq. edges to compose!");
			}
			if (edge instanceof ConjunctionEdge) {
				// if the parent of this conjunction is also a conjunction we do
				// not create a sequential composition here
				// seqComposedBlocks.addAll(recConvEdges);
				if (mHasConjunctionAsParent.contains(edge)) {
					return null;
				}
				// In a conjunction, we can ignore GotoEdges
				ArrayList<CodeBlock> composeEdges = new ArrayList<CodeBlock>();
				ArrayList<CodeBlock> gotoEdges = new ArrayList<CodeBlock>();
				// we take the actual list from the stack...
				for (CodeBlock cb : mSeqComposedBlocks.pop()) {
					if (cb instanceof GotoEdge) {
						gotoEdges.add(cb);
						continue;
					}
					composeEdges.add(cb);
				}
				// Special case: only Goto's we to transpose it to assume true
				if (composeEdges.isEmpty()) {
					if (gotoEdges.isEmpty()) {
						throw new IllegalArgumentException("No compose edges, there should be goto-Edges!");
					}
					// We add here a SequentialComposition with only one
					// element, because we have to remove later a list from the
					// stack whereas this is only done for not
					// SequentialCompositons
					if (edge instanceof ShortcutErrEdge) {
						return new ShortcutCodeBlock(null, null, new CodeBlock[] { replaceGotoEdge(gotoEdges.get(0),
								gotoEdges.get(1)) }, mLogger);
					}
					return new SequentialComposition(null, null, mBoogie2SMT, mModGlobalVarManager, false, false,
							mServices, replaceGotoEdge(gotoEdges.get(0), gotoEdges.get(1)));
				}
				if (edge instanceof ShortcutErrEdge) {
					return new ShortcutCodeBlock(null, null, composeEdges.toArray(new CodeBlock[0]), mLogger);
				}
				return new SequentialComposition(null, null, mBoogie2SMT, mModGlobalVarManager, false, false, mServices,
						composeEdges.toArray(new CodeBlock[0]));
			}
			if (edge instanceof DisjunctionEdge) {
				ArrayList<CodeBlock> composeEdges = new ArrayList<CodeBlock>();
				for (CodeBlock cb : recConvEdges) {
					if (!(cb instanceof SequentialComposition)) {
						// if we have no code block, we have to remove the
						// created lists on the stack
						if (!mSeqComposedBlocks.pop().isEmpty()) {
							throw new IllegalArgumentException("It is not allowed to pop "
									+ "non empty lists, from the stack");
						}
					}
					if (cb instanceof GotoEdge) {
						composeEdges.add(replaceGotoEdge(cb, null));
						continue;
					}
					composeEdges.add(cb);
				}
				// TODO: For non composite edges we have to remove one thing
				// from the stack? Is this case applicable?
				if (composeEdges.size() == 1) {
					// If we have only one composedEdge we return it, because a
					// parallel composition is not needed
					if (composeEdges.get(0) instanceof SequentialComposition) {
						// -> we only pop() if the one edge is an
						// SequentialComposition, otherwise this has already
						// done
						mSeqComposedBlocks.pop();
					}
					return composeEdges.get(0);
				}
				if (composeEdges.size() != 2) {
					throw new IllegalArgumentException("For DisjunctionEdges there should always"
							+ " be exactly two edges, to compose!");
				}
				if (composeEdges.get(0) instanceof ShortcutCodeBlock
						|| composeEdges.get(1) instanceof ShortcutCodeBlock) {
					throw new IllegalArgumentException("Shortcut is contained in ParallelComposition?");
				}
				return new ParallelComposition(null, null, mBoogie2SMT, mServices, composeEdges.get(0),
						composeEdges.get(1));
			}
		}
		// should never reach this end here?
		mLogger.error("Failure during construction of formulas... " + edge);
		return null;
	}

	/**
	 * This method converts a basic edge into one basic code block. It is
	 * copied, because we create new instances, since we do not want to change
	 * the original RCFG.
	 * 
	 * @param edge
	 *            IMinimizedEdge which is a basic edge
	 * @return corresponding CodeBlock
	 */
	private CodeBlock convertBasicEdge(IMinimizedEdge edge) {
		CodeBlock cb = ((IBasicEdge) edge).getOriginalEdge();
		CodeBlock copyOfCodeBlock = null;
		// We need to convert the basic edges, into new ones
		// -> so basically we create a new instance of the CodeBlock,
		// this is necessary to avoid mixing of the models
		if (cb instanceof StatementSequence) {
			copyOfCodeBlock = new StatementSequence(null, null, ((StatementSequence) cb).getStatements(),
					((StatementSequence) cb).getOrigin(), mLogger);
		}
		if (cb instanceof Call) {
			copyOfCodeBlock = new Call(null, null, ((Call) cb).getCallStatement(), mLogger);
		}
		if (cb instanceof Return) {
			copyOfCodeBlock = new Return(null, null, ((Return) cb).getCorrespondingCall(), mLogger);
		}
		if (cb instanceof Summary) {
			// This situation can happen, if a Call/Return/Summary-Edges are
			// involved, they are not part of the formula and are ignored
			copyOfCodeBlock = cb;
		}
		if (cb instanceof GotoEdge) {
			copyOfCodeBlock = cb;
		}
		if (copyOfCodeBlock == null) {
			throw new IllegalArgumentException("Failure while converting a" + "CodeBlock, maybe there is a new type,"
					+ "which should be added");
		} else {
			copyOfCodeBlock.setTransitionFormula(cb.getTransitionFormula());
			return copyOfCodeBlock;
		}
	}

	/**
	 * This method replaces an Goto-Edge with the statement "assume true". <br>
	 * TODO: Need to be clarified if this is correct.
	 * 
	 * @param gotoEdge
	 *            the Goto-Edge to convert
	 * @param secondGotoEdge
	 *            maybe somites we have to convert two Goto-Edges
	 * @return the converted "assume true"
	 */
	private CodeBlock replaceGotoEdge(CodeBlock gotoEdge, CodeBlock secondGotoEdge) {
		StatementSequence replacement = null;
		if (secondGotoEdge == null) {
			replacement = new StatementSequence(null, null, new AssumeStatement(gotoEdge.getPayload().getLocation(),
					new BooleanLiteral(gotoEdge.getPayload().getLocation(), true)), mLogger);
		} else {
			replacement = new StatementSequence(null, null, new AssumeStatement(gotoEdge.getPayload().getLocation(),
					new BooleanLiteral(gotoEdge.getPayload().getLocation(), true)), mLogger);
		}
		String procId = gotoEdge.getPreceedingProcedure();
		mTransFormBuilder.addTransitionFormulas(replacement, procId);
		return replacement;
	}

	/**
	 * @return the origToNewMap
	 */
	public HashMap<ProgramPoint, ProgramPoint> getOrigToNewMap() {
		return mOrigToNewMap;
	}

	/**
	 * @return the locNodesForAnnot
	 */
	public HashMap<String, HashMap<String, ProgramPoint>> getLocNodesForAnnot() {
		return mLocNodesForAnnot;
	}
}
