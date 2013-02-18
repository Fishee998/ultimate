package de.uni_freiburg.informatik.ultimate.cookiefy.ltl.model;

public class Until extends BinaryOperator {

	public Until(Formula first, Formula last) {
		super(BinOp.UNTIL, first, last);
	}
	
	@Override
	public void acceptPreOrder(Visitor visitor) {
		visitor.visit(this);
		mFirstOperand.acceptPreOrder(visitor);
		mLastOperand.acceptPreOrder(visitor);
	}
	
	@Override
	public void acceptInOrder(Visitor visitor) {
		mFirstOperand.acceptInOrder(visitor);
		visitor.visit(this);
		mLastOperand.acceptInOrder(visitor);
	}

	@Override
	public void acceptPostOrder(Visitor visitor) {
		mFirstOperand.acceptPostOrder(visitor);
		mLastOperand.acceptPostOrder(visitor);
		visitor.visit(this);
	}

}
