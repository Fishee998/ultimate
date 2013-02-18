(set-option :produce-proofs true)
(set-info :source "{ Simple test for interpolation with quantified formulae but quantifier-free interpolants }")
(set-logic AUFLIA)
(declare-fun P ( Int) Bool)
(declare-fun a () Int)
(assert (! (and (forall ((x Int)) (! (P x) :pattern ((P x)))) (= a 3)) :named IP_0))
(assert (! (not (P a)) :named IP_1))
(check-sat)
(get-interpolants IP_0 IP_1)
(exit)
