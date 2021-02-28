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

#model.players[] = dataset["name"]

# print(dataset)
# print(model)
# print(model.avg_points)


def obj(model):
    """Objective Function
    """
    return pyo.summation(model.avg_points, model.picked)


def eleven(model):
    """There should be 11 players in the line up

    """
    return pyo.summation(model.picked) == 11


def all_ok(model):
    """Players are not injured

    """
    return pyo.summation(model.status, model.picked) == 11


def possible_lineups(model):
    keepers = [p for p in model.players if model.positions[p] == 1]
    defenders = [p for p in model.players if model.positions[p] == 2]
    midfielders = [p for p in model.players if model.positions[p] == 3]
    attackers = [p for p in model.players if model.positions[p] == 4]

    one_goalkeeper = sum(model.picked[j] for j in keepers) == 1

    three_five_two = (sum(model.picked[j] for j in defenders) == 3 and
                      sum(model.picked[j] for j in midfielders) == 5 and
                      sum(model.picker[j] for j in attackers == 2))

    three_four_three = (sum(model.picked[j] for j in defenders) == 3 and
                        sum(model.picked[j] for j in midfielders) == 4 and
                        sum(model.picker[j] for j in attackers == 3))

    four_five_one = (sum(model.picked[j] for j in defenders) == 4 and
                     sum(model.picked[j] for j in midfielders) == 5 and
                     sum(model.picker[j] for j in attackers == 1))

    four_four_two = (sum(model.picked[j] for j in defenders) == 4 and
                     sum(model.picked[j] for j in midfielders) == 4 and
                     sum(model.picker[j] for j in attackers == 2))

    five_four_one = (sum(model.picked[j] for j in defenders) == 5 and
                     sum(model.picked[j] for j in midfielders) == 4 and
                     sum(model.picker[j] for j in attackers == 1))

    five_three_two = (sum(model.picked[j] for j in defenders) == 5 and
                      sum(model.picked[j] for j in midfielders) == 3 and
                      sum(model.picker[j] for j in attackers == 2))


    return (one_goalkeeper and (three_five_two or
                                three_four_three or
                                four_five_one or
                                four_four_two or
                                five_four_one or
                                five_three_two))


model.performance = pyo.Objective(
    rule=obj,
    sense=pyo.maximize
)
model.eleven = pyo.Constraint(rule=eleven)
model.all_ok = pyo.Constraint(rule=all_ok)
model.possible_lineups = pyo.Constraint(rule=possible_lineups)

opt = pyo.SolverFactory('glpk')
results = opt.solve(model)
model.display()
