/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.stalin.automata.parser.TestGrammar.node;

import local.stalin.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TInitialtoken extends Token
{
    public TInitialtoken()
    {
        super.setText("#initial");
    }

    public TInitialtoken(int line, int pos)
    {
        super.setText("#initial");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TInitialtoken(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTInitialtoken(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TInitialtoken text.");
    }
}
