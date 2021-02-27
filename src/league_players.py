import os
import pandas as pd
from functools import cache
from .api import session as s
from .core import clean_3, write_db

league_id = os.environ["LEAGUE_ID"]

ranking_url = f"https://api.laligafantasymarca.com/api/v4/leagues/{league_id}/ranking"
managers_url = lambda id: f"https://api.laligafantasymarca.com/api/v3/leagues/{league_id}/teams/{id}"


@cache
def manager_ids():
    """Returns manager ids from the ranking view

    """
    managers = s().get(ranking_url).json()
    return [t["team"]["id"] for t in managers]


@cache
def players_managed_by(manager_id):
    """Returns player buyout details managed by manager_id

    """
    team = s().get(managers_url(manager_id)).json()["players"]
    return [clean_3(manager_id, player) for player in team]


@cache
def league_players():
    """This function returns player buyout details for all those players
    which are managed by at least 1 manager

    """
    league_players = []
    for manager_id in manager_ids():
        team = s().get(managers_url(manager_id)).json()["players"]
        league_players.append([clean_3(manager_id, player) for player in team])

    return [player for team in league_players for player in team]  # Flatten


def handler(event, context):
    df = pd.DataFrame(league_players())
    df = df.set_index("id")
    df = df[[
        'manager_id',
        'clause',
        'clause_lock_end'
    ]]
    write_db(df, "league_players")


if __name__ == '__main__':
    # AWS Lambda mock
    handler("some_event", "some_context")
