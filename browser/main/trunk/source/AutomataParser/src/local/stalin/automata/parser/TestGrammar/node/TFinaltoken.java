/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.stalin.automata.parser.TestGrammar.node;

import local.stalin.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TFinaltoken extends Token
{
    public TFinaltoken()
    {
        super.setText("#final");
    }

    public TFinaltoken(int line, int pos)
    {
        super.setText("#final");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TFinaltoken(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTFinaltoken(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TFinaltoken text.");
    }
}
