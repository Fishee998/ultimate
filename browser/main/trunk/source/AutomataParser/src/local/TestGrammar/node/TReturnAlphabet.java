/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.TestGrammar.node;

import local.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TReturnAlphabet extends Token
{
    public TReturnAlphabet()
    {
        super.setText("#returnAlphabet");
    }

    public TReturnAlphabet(int line, int pos)
    {
        super.setText("#returnAlphabet");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TReturnAlphabet(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTReturnAlphabet(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TReturnAlphabet text.");
    }
}
