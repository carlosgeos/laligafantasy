import pandas as pd
import players
from core import write_db
from api import session as s
from logs import logger
from json.decoder import JSONDecodeError

player_prices_url = lambda p: f"https://api.laligafantasymarca.com/api/v3/player/{p['id']}/market-value"
num_players = len(players.all_players)


def price_history(i, player):
    """Fetches and parses the price history for player id 'player'

    """
    logger.info(f"Processing {player['name']}. {i + 1} out of {num_players}")
    url = player_prices_url(player)
    try:
        player_prices = s.get(url, timeout=4).json()
        player_prices = [{
            "id": player["id"],
            "price": p["marketValue"],
            "date": pd.to_datetime(p["date"], utc=True)
        } for p in player_prices]

        return player_prices
    except JSONDecodeError:
        logger.warn(f"JSON Decoding failed for player id {player['id']}")
        return []


def main():
    all_prices = [price_history(i, player) for (i, player) in enumerate(players.all_players)]
    all_prices = [price for prices in all_prices for price in prices]  # Flatten

    df = pd.DataFrame(all_prices)
    df = df.set_index("id")
    write_db(df, "price_history")


if __name__ == '__main__':
    main()
