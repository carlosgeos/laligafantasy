import pandas as pd
from functools import cache
from api import session as s
from core import clean_2, write_db


all_players_url = "https://api.laligafantasymarca.com/api/v3/players"


@cache
def all_players():
    all_players = s().get(all_players_url).json()
    return [clean_2(player) for player in all_players]


def handler(event, context):
    df = pd.DataFrame(all_players())
    df = df.set_index("id")
    df = df[[
        'name',
        'position',
        'status',
        'team',
        'market_value',
        'avg_points',
        'm_per_point',
        'points',
        'potential',
        'last_season_points',
        'change'
    ]]
    write_db(df, "players")


if __name__ == '__main__':
    # AWS Lambda mock
    handler("some_event", "some_context")
