/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.stalin.automata.parser.TestGrammar.node;

import local.stalin.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TAutomatatoken extends Token
{
    public TAutomatatoken()
    {
        super.setText("#automata");
    }

    public TAutomatatoken(int line, int pos)
    {
        super.setText("#automata");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TAutomatatoken(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTAutomatatoken(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TAutomatatoken text.");
    }
}
