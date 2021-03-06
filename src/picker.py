import os
import pyomo.environ as pyo
import pandas as pd
from .players import all_players
from .league_players import players_managed_by


my_players = players_managed_by(os.environ["MANAGER_ID"])
my_players_df = pd.DataFrame(my_players)
my_players_df = my_players_df.set_index("id")

player_details = all_players()
player_details_df = pd.DataFrame(player_details)
player_details_df = player_details_df.set_index("id")

dataset = my_players_df.join(player_details_df)

dataset_dict = dataset[["name", "status", "avg_points", "position"]].to_dict("index")  # DataFrame to dict
player_statuses = {v["name"]: 0 if v["status"] in ["injured", "suspended"] else 1 for (k, v) in dataset_dict.items()}  # player -> status map
player_points = {v["name"]: v["avg_points"] for (k, v) in dataset_dict.items()} # player -> points map
player_positions = {v["name"]: v["position"] for (k, v) in dataset_dict.items()} # player -> positions

model = pyo.ConcreteModel()

model.players = pyo.Set(initialize=dataset["name"])
model.avg_points = pyo.Param(model.players, domain=pyo.Reals, initialize=player_points)
model.status = pyo.Param(model.players, domain=pyo.NonNegativeIntegers, initialize=player_statuses)
model.positions = pyo.Param(model.players, initialize=player_positions)
model.picked = pyo.Var(model.players, domain=pyo.Boolean, initialize=0)

model.performance = pyo.Objective(
    rule=lambda m: pyo.summation(m.avg_points, m.picked),
    sense=pyo.maximize
)

goalies     = [p for p in model.players if model.positions[p] == 1]
defenders   = [p for p in model.players if model.positions[p] == 2]
midfielders = [p for p in model.players if model.positions[p] == 3]
attackers   = [p for p in model.players if model.positions[p] == 4]

model.eleven = pyo.Constraint(rule=lambda m: pyo.summation(m.picked) == 11)
model.all_ok = pyo.Constraint(rule=lambda m: pyo.summation(m.status, m.picked) == 11)
model.one_goalkeeper = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in goalies) == 1)
model.defenders_lb = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in defenders) >= 3)
model.defenders_ub = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in defenders) <= 5)
model.midfielders_lb = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in midfielders) >= 3)
model.midfielders_ub = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in midfielders) <= 5)
model.attackers_lb = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in attackers) >= 1)
model.attackers_ub = pyo.Constraint(rule=lambda m: sum(m.picked[j] for j in attackers) <= 3)

opt = pyo.SolverFactory('glpk')


def print_lineup(model, results):
    picked = [{"name": player, "pos": model.positions[player]} for player in model.players if pyo.value(model.picked[player]) == 1]

    defenders = sum(1 if player["pos"] == 2 else 0 for player in picked)
    midfielders = sum(1 if player["pos"] == 3 else 0 for player in picked)
    attackers = sum(1 if player["pos"] == 4 else 0 for player in picked)

    print("LINEUP")
    print("------")
    print(f"Formation: {defenders}-{midfielders}-{attackers}")

    print("\nGoalkeeper:")
    print("-----------")
    print(" --- ".join([player["name"] for player in picked if player["pos"] == 1]))

    print("\nDefenders:")
    print("----------")
    print(" --- ".join([player["name"] for player in picked if player["pos"] == 2]))

    print("\nMidfielders:")
    print("------------")
    print(" --- ".join([player["name"] for player in picked if player["pos"] == 3]))

    print("\nAttackers:")
    print("---------")
    print(" --- ".join([player["name"] for player in picked if player["pos"] == 4]))


if __name__ == '__main__':
    results = opt.solve(model)

    if (results.solver.status == pyo.SolverStatus.ok) and (results.solver.termination_condition == pyo.TerminationCondition.optimal):
        print("--SOLVER OK--")
        print(f"MAX: {pyo.value(model.performance):.2f} average points\n")
        print_lineup(model, results)
    elif results.solver.termination_condition == pyo.TerminationCondition.infeasible:
        print("infeasible")
    else:
        print(str(results.solver))
