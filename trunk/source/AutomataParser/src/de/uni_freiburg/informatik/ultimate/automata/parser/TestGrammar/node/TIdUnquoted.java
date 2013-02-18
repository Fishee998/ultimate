/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.node;

import de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class TIdUnquoted extends Token
{
    public TIdUnquoted(String text)
    {
        setText(text);
    }

    public TIdUnquoted(String text, int line, int pos)
    {
        setText(text);
        setLine(line);
        setPos(pos);
    }

    @Override
    public Object clone()
    {
      return new TIdUnquoted(getText(), getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTIdUnquoted(this);
    }
}
