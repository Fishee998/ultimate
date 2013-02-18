/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.stalin.automata.parser.TestGrammar.node;

import local.stalin.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TDifferencetoken extends Token
{
    public TDifferencetoken()
    {
        super.setText("DIFFERENCE");
    }

    public TDifferencetoken(int line, int pos)
    {
        super.setText("DIFFERENCE");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TDifferencetoken(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTDifferencetoken(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TDifferencetoken text.");
    }
}
