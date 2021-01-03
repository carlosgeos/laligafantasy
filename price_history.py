import pandas as pd
import players
from functools import cache
from core import write_db
from api import session as s
from logs import logger
from json.decoder import JSONDecodeError

player_prices_url = lambda p: f"https://api.laligafantasymarca.com/api/v3/player/{p['id']}/market-value"


def price_history(i, player, total):
    """Fetches and parses the price history for player id 'player'

    """
    logger.info(f"Processing {player['name']}. {i + 1} out of {total}")
    url = player_prices_url(player)
    try:
        player_prices = s().get(url, timeout=4).json()
        player_prices = [{
            "id": player["id"],
            "price": p["marketValue"],
            "date": pd.Timestamp(p["date"])
        } for p in player_prices]

        return player_prices
    except JSONDecodeError:
        logger.warn(f"JSON Decoding failed for player id {player['id']}")
        return []


@cache
def all_prices():
    """Returns a list of dicts containing historical prices for all
    players in LaLiga (501 before winter market 20/21)

    """
    all_players = players.all_players()
    total = len(all_players)
    all_prices = [price_history(i, player, total) for (i, player) in enumerate(all_players)]
    return [price for prices in all_prices for price in prices]  # Flatten


def handler(event, context):
    df = pd.DataFrame(all_prices())
    df = df.set_index("id")
    write_db(df, "price_history")


if __name__ == '__main__':
    # AWS Lambda mock
    handler("some_event", "some_context")
