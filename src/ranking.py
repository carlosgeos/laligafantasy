import os
import pandas as pd
from functools import cache

from .api import session as s
from .core import clean_4, write_db, insert_db

league_id = os.environ["LEAGUE_ID"]
ranking_url = f"https://api.laligafantasymarca.com/api/v4/leagues/{league_id}/ranking"


@cache
def ranking():
    ranking = s().get(ranking_url).json()
    return [clean_4(team) for team in ranking]


def handler(event, context):
    df = pd.DataFrame(ranking())
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
    # AWS Lambda mock
    handler("some_event", "some_context")
