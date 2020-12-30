import os
import pandas as pd
from api import session as s
from core import clean_3, write_db

league_id = os.environ["LEAGUE_ID"]

ranking_url = f"https://api.laligafantasymarca.com/api/v4/leagues/{league_id}/ranking"
managers_url = lambda id: f"https://api.laligafantasymarca.com/api/v3/leagues/{league_id}/teams/{id}"

managers = s.get(ranking_url).json()
manager_ids = [t["team"]["id"] for t in managers]

league_players = []

for manager_id in manager_ids:
    team = s.get(managers_url(manager_id)).json()["players"]
    league_players.append([clean_3(manager_id, player) for player in team])

league_players = [player for team in league_players for player in team]  # Flatten


def main():
    df = pd.DataFrame(league_players)
    df = df.set_index("id")
    df = df[[
        'manager_id',
        'clause',
        'clause_lock_end'
    ]]
    write_db(df, "league_players")


if __name__ == '__main__':
    main()
