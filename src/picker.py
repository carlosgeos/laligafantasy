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

print(dataset[["name", "status", "avg_points"]].to_dict("index"))

model = pyo.ConcreteModel()

model.players = pyo.Set(initialize=dataset["name"])
model.avg_points = pyo.Param(model.players, domain=pyo.Reals, initialize=dataset["avg_points"])
model.picked = pyo.Var(model.players, domain=pyo.Boolean)

#model.players[] = dataset["name"]

print(dataset)
print(model)
print(model.avg_points)


def obj(m):
    """Objective Function
    """
    return pyo.summation(model.avg_points, model.picked)


def eleven(m):
    return pyo.summation(model.picked) <= 11


model.performance = pyo.Objective(
    rule=obj,
    sense=pyo.maximize
)
model.eleven = pyo.Constraint(rule=eleven)

#model.constraints = pyo.ConstraintList()

opt = pyo.SolverFactory('glpk')
results = opt.solve(model)
model.display()
