/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.TestGrammar.node;

import local.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TCallTransitions extends Token
{
    public TCallTransitions()
    {
        super.setText("#callTransitions");
    }

    public TCallTransitions(int line, int pos)
    {
        super.setText("#callTransitions");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TCallTransitions(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTCallTransitions(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TCallTransitions text.");
    }
}
