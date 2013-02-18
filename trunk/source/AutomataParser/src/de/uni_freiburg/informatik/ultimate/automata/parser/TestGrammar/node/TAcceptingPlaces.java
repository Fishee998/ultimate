/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.node;

import de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TAcceptingPlaces extends Token
{
    public TAcceptingPlaces()
    {
        super.setText("#acceptingPlaces");
    }

    public TAcceptingPlaces(int line, int pos)
    {
        super.setText("#acceptingPlaces");
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TAcceptingPlaces(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTAcceptingPlaces(this);
    }

    @Override
    public void setText(@SuppressWarnings("unused") String text)
    {
        throw new RuntimeException("Cannot change TAcceptingPlaces text.");
    }
}
