//#terminating
/*
 * Date: 10.11.2013
 * Author: heizmann@informatik.uni-freiburg.de
 *
 */
var x,y: int;

procedure main()
modifies x, y;
{
  assume true;
  while (x>0) {
    call x := decrease(x);
  }
}

procedure decrease(a: int) returns (res: int)
{
  res := a - 1;
}