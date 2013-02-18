(set-option :produce-proofs true)
(set-info :source "{
Desired Interpolant: (and (= ab3 ab2) (= ab (g ab1)))
}")
(set-info :category "{ crafted }")
(set-info :difficulty "{ 0 }")
(set-logic QF_UF)
(declare-sort U 0)
(declare-fun a () U)
(declare-fun a2 () U)
(declare-fun a3 () U)
(declare-fun b () U)
(declare-fun b2 () U)
(declare-fun b3 () U)
(declare-fun ab () U)
(declare-fun ab1 () U)
(declare-fun ab2 () U)
(declare-fun ab3 () U)
(declare-fun fa ( U) U)
(declare-fun fb ( U) U)
(declare-fun g ( U) U)
(assert (! (and (= a ab) (= a (g ab1)) (= ab2 a2) (= a2 ab3)) :named IP_0))
(assert (! (and (= ab1 b2) (= b2 (fb ab2)) (= (fb ab3) (g ab1)) (= ab1 b3) (not (= b3 ab))) :named IP_1))
(check-sat)
(get-interpolants IP_0 IP_1)
(exit)
