package de.uni_freiburg.informatik.ultimate.blockencoding.converter;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.CodeBlock;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg.ProgramPoint;
import de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.util.IRCFGVisitor;

/**
 * This is basically a dummy code block, which we need while converting a
 * shortcut edge (to an error location). For the RCFG we have to use a so called
 * InterproceduralSeqComposition, because this can handle more Calls then
 * Returns. During the Conversion we store all involved CodeBlocks here, for
 * later use it in the InterproceduralSequentialCompositon.
 * 
 * @author Stefan Wissert
 * 
 */
public class ShortcutCodeBlock extends CodeBlock {

	private CodeBlock[] codeBlocks;

	/**
	 * @param source
	 * @param target
	 * @param codeBlocks
	 * @param logger 
	 */
	public ShortcutCodeBlock(ProgramPoint source, ProgramPoint target,
			CodeBlock[] codeBlocks, Logger logger) {
		super(source, target, logger);
		this.codeBlocks = codeBlocks;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg
	 * .CodeBlock#getFieldNames()
	 */
	@Override
	protected String[] getFieldNames() {
		return new String[] {};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.uni_freiburg.informatik.ultimate.plugins.generator.rcfgbuilder.cfg
	 * .CodeBlock#getPrettyPrintedStatements()
	 */
	@Override
	public String getPrettyPrintedStatements() {
		return null;
	}

	/**
	 * @return the codeBlocks
	 */
	public CodeBlock[] getCodeBlocks() {
		return codeBlocks;
	}

	@Override
	public String toString() {
		return "SHORTCUTCODEBLOCK";
	}

	@Override
	public void accept(IRCFGVisitor visitor)
	{
		visitor.visitCodeBlock(this);
		for(CodeBlock block : codeBlocks)
		{
			block.accept(visitor);
		}
		visitor.visitedCodeBlock(this);
	}

}
