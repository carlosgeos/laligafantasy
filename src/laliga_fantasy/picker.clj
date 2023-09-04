(ns laliga-fantasy.picker
  (:require
   [next.jdbc.sql :as sql]
   [laliga-fantasy.db :as db]
   [next.jdbc.result-set :as rs])
  (:import org.chocosolver.solver.Model
           org.chocosolver.solver.Solver
           org.chocosolver.solver.exception.ContradictionException
           org.chocosolver.solver.variables.IntVar))


(def team
  (delay
    (sql/query
     db/ds
     ["select * from my_team"]
     {:builder-fn rs/as-unqualified-maps})))

(def m (Model. "knapsack"))
