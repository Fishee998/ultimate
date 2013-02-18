/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.node;

import de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TNet extends Token
{
    public TNet()
    {
        super.setText("#net");
    }

    public TNet(int line, int pos)
    {
        super.setText("#net");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TNet(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTNet(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TNet text.");
    }
}
