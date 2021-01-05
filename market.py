import pandas as pd
from functools import cache
from api import session as s
from core import clean, write_db

market_url = "https://api.laligafantasymarca.com/api/v3/league/011300439/market"


def not_my_sale(player):
    """Returns true if the player listed in the market does not come from
    my squad

    """
    return "salePrice" in player


@cache
def market():
    market = s().get(market_url).json()
    return sorted([clean(player) for player in market if not_my_sale(player)], key=lambda x: x["m_per_point"])


def handler(event, context):
    df = pd.DataFrame(market())
    df = df.set_index("id")
    df = df[[
        'name',
        'status',
        'market_value',
        'sale_price',
        'price',
        'avg_points',
        'm_per_point',
        'points',
        'potential',
        'last_season_points',
        'change'
    ]]
    write_db(df, "market")


if __name__ == '__main__':
    # AWS Lambda mock
    handler("some_event", "some_context")
