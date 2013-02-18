/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.stalin.automata.parser.TestGrammar.node;

import local.stalin.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class AAbstractcall extends PAbstractcall
{
    private TId _from_;
    private TId _to_;
    private TId _symbol_;

    public AAbstractcall()
    {
        // Constructor
    }

    public AAbstractcall(
        @SuppressWarnings("hiding") TId _from_,
        @SuppressWarnings("hiding") TId _to_,
        @SuppressWarnings("hiding") TId _symbol_)
    {
        // Constructor
        setFrom(_from_);

        setTo(_to_);

        setSymbol(_symbol_);

    }

    @Override
    public Object clone()
    {
        return new AAbstractcall(
            cloneNode(this._from_),
            cloneNode(this._to_),
            cloneNode(this._symbol_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAAbstractcall(this);
    }

    public TId getFrom()
    {
        return this._from_;
    }

    public void setFrom(TId node)
    {
        if(this._from_ != null)
        {
            this._from_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._from_ = node;
    }

    public TId getTo()
    {
        return this._to_;
    }

    public void setTo(TId node)
    {
        if(this._to_ != null)
        {
            this._to_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._to_ = node;
    }

    public TId getSymbol()
    {
        return this._symbol_;
    }

    public void setSymbol(TId node)
    {
        if(this._symbol_ != null)
        {
            this._symbol_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._symbol_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._from_)
            + toString(this._to_)
            + toString(this._symbol_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._from_ == child)
        {
            this._from_ = null;
            return;
        }

        if(this._to_ == child)
        {
            this._to_ = null;
            return;
        }

        if(this._symbol_ == child)
        {
            this._symbol_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._from_ == oldChild)
        {
            setFrom((TId) newChild);
            return;
        }

        if(this._to_ == oldChild)
        {
            setTo((TId) newChild);
            return;
        }

        if(this._symbol_ == oldChild)
        {
            setSymbol((TId) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
