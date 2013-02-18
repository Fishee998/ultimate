/* This file was generated by SableCC (http://www.sablecc.org/). */

package de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.node;

import java.util.*;
import de.uni_freiburg.informatik.ultimate.automata.parser.TestGrammar.analysis.*;

@SuppressWarnings("nls")
public final class ANetjulianAutomatonDefinition extends PAutomatonDefinition
{
    private PIdentifier _name_;
    private final LinkedList<PIdentifier> _alphabet_ = new LinkedList<PIdentifier>();
    private final LinkedList<PIdentifier> _places_ = new LinkedList<PIdentifier>();
    private final LinkedList<PNetTransition> _transitions_ = new LinkedList<PNetTransition>();
    private PMarking _initialMarking_;
    private final LinkedList<PIdentifier> _acceptingPlaces_ = new LinkedList<PIdentifier>();

    public ANetjulianAutomatonDefinition()
    {
        // Constructor
    }

    public ANetjulianAutomatonDefinition(
        @SuppressWarnings("hiding") PIdentifier _name_,
        @SuppressWarnings("hiding") List<PIdentifier> _alphabet_,
        @SuppressWarnings("hiding") List<PIdentifier> _places_,
        @SuppressWarnings("hiding") List<PNetTransition> _transitions_,
        @SuppressWarnings("hiding") PMarking _initialMarking_,
        @SuppressWarnings("hiding") List<PIdentifier> _acceptingPlaces_)
    {
        // Constructor
        setName(_name_);

        setAlphabet(_alphabet_);

        setPlaces(_places_);

        setTransitions(_transitions_);

        setInitialMarking(_initialMarking_);

        setAcceptingPlaces(_acceptingPlaces_);

    }

    @Override
    public Object clone()
    {
        return new ANetjulianAutomatonDefinition(
            cloneNode(this._name_),
            cloneList(this._alphabet_),
            cloneList(this._places_),
            cloneList(this._transitions_),
            cloneNode(this._initialMarking_),
            cloneList(this._acceptingPlaces_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseANetjulianAutomatonDefinition(this);
    }

    public PIdentifier getName()
    {
        return this._name_;
    }

    public void setName(PIdentifier node)
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

    public LinkedList<PIdentifier> getAlphabet()
    {
        return this._alphabet_;
    }

    public void setAlphabet(List<PIdentifier> list)
    {
        this._alphabet_.clear();
        this._alphabet_.addAll(list);
        for(PIdentifier e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    public LinkedList<PIdentifier> getPlaces()
    {
        return this._places_;
    }

    public void setPlaces(List<PIdentifier> list)
    {
        this._places_.clear();
        this._places_.addAll(list);
        for(PIdentifier e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    public LinkedList<PNetTransition> getTransitions()
    {
        return this._transitions_;
    }

    public void setTransitions(List<PNetTransition> list)
    {
        this._transitions_.clear();
        this._transitions_.addAll(list);
        for(PNetTransition e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    public PMarking getInitialMarking()
    {
        return this._initialMarking_;
    }

    public void setInitialMarking(PMarking node)
    {
        if(this._initialMarking_ != null)
        {
            this._initialMarking_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._initialMarking_ = node;
    }

    public LinkedList<PIdentifier> getAcceptingPlaces()
    {
        return this._acceptingPlaces_;
    }

    public void setAcceptingPlaces(List<PIdentifier> list)
    {
        this._acceptingPlaces_.clear();
        this._acceptingPlaces_.addAll(list);
        for(PIdentifier e : list)
        {
            if(e.parent() != null)
            {
                e.parent().removeChild(e);
            }

            e.parent(this);
        }
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._name_)
            + toString(this._alphabet_)
            + toString(this._places_)
            + toString(this._transitions_)
            + toString(this._initialMarking_)
            + toString(this._acceptingPlaces_);
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

        if(this._alphabet_.remove(child))
        {
            return;
        }

        if(this._places_.remove(child))
        {
            return;
        }

        if(this._transitions_.remove(child))
        {
            return;
        }

        if(this._initialMarking_ == child)
        {
            this._initialMarking_ = null;
            return;
        }

        if(this._acceptingPlaces_.remove(child))
        {
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
            setName((PIdentifier) newChild);
            return;
        }

        for(ListIterator<PIdentifier> i = this._alphabet_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PIdentifier) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        for(ListIterator<PIdentifier> i = this._places_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PIdentifier) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        for(ListIterator<PNetTransition> i = this._transitions_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PNetTransition) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        if(this._initialMarking_ == oldChild)
        {
            setInitialMarking((PMarking) newChild);
            return;
        }

        for(ListIterator<PIdentifier> i = this._acceptingPlaces_.listIterator(); i.hasNext();)
        {
            if(i.next() == oldChild)
            {
                if(newChild != null)
                {
                    i.set((PIdentifier) newChild);
                    newChild.parent(this);
                    oldChild.parent(null);
                    return;
                }

                i.remove();
                oldChild.parent(null);
                return;
            }
        }

        throw new RuntimeException("Not a child.");
    }
}
