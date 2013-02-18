/* This file was generated by SableCC (http://www.sablecc.org/). */

package local.TestGrammar.node;

import local.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class AAutomatonNameExpression extends PExpression
{
    private TId _name_;

    public AAutomatonNameExpression()
    {
        // Constructor
    }

    public AAutomatonNameExpression(
        @SuppressWarnings("hiding") TId _name_)
    {
        // Constructor
        setName(_name_);

    }

    @Override
    public Object clone()
    {
        return new AAutomatonNameExpression(
            cloneNode(this._name_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAAutomatonNameExpression(this);
    }

    public TId getName()
    {
        return this._name_;
    }

    public void setName(TId node)
    {
        if(this._name_ != null)
        {
            this._name_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._name_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._name_ == child)
        {
            this._name_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._name_ == oldChild)
        {
            setName((TId) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}
