import os
import pandas as pd
from api import session as s
from core import clean_4, write_db, insert_db

league_id = os.environ["LEAGUE_ID"]
ranking_url = f"https://api.laligafantasymarca.com/api/v4/leagues/{league_id}/ranking"

ranking = s.get(ranking_url).json()
ranking = [clean_4(team) for team in ranking]


def main():
    df = pd.DataFrame(ranking)
    df = df.set_index("id")
    df = df[[
        'name',
        'position',
        'points',
        'value',
        'team_points',
        'ts'
    ]]
    write_db(df, "ranking")
    insert_db(df, "ranking_history")


if __name__ == '__main__':
    main()
