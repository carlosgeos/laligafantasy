import pandas as pd
from api import session as s
from core import clean_2, write_db


all_players_url = "https://api.laligafantasymarca.com/api/v3/players"

all_players = s.get(all_players_url).json()

all_players = [clean_2(player) for player in all_players]


def main():
    df = pd.DataFrame(all_players)
    df = df.astype({
        "id": "int64",
        "market_value": "int64"
    })
    df = df.set_index("id")
    df = df[[
        'name',
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
    main()
