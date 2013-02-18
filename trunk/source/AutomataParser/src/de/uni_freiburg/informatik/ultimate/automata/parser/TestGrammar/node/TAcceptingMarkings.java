/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.node;

import de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TAcceptingMarkings extends Token
{
    public TAcceptingMarkings()
    {
        super.setText("#acceptingMarkings");
    }

    public TAcceptingMarkings(int line, int pos)
    {
        super.setText("#acceptingMarkings");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TAcceptingMarkings(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTAcceptingMarkings(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TAcceptingMarkings text.");
    }
}
