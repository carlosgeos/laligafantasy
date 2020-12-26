import pandas as pd
import players
from core import write_db
from api import session as s
from logs import logger
from json.decoder import JSONDecodeError

player_prices_url = lambda p: f"https://api.laligafantasymarca.com/api/v3/player/{p['id']}/market-value"


def price_history(player):
    """Fetches and parses the price history for player id 'player'

    """
    logger.info(f"Processing {player['name']}")
    url = player_prices_url(player)
    try:
        player_prices = s.get(url, timeout=4).json()
        player_prices = [{
            "id": player["id"],
            "price": p["marketValue"],
            "date": p["date"]
        } for p in player_prices]

        return player_prices
    except JSONDecodeError:
        logger.warn(f"JSON Decoding failed for player id {player['id']}")
        return []


all_prices = [price_history(player) for player in players.all_players]
all_prices = [price for prices in all_prices for price in prices]

df = pd.DataFrame(all_prices)
df["date"] = pd.to_datetime(df["date"], utc=True)
df["id"] = df["id"].astype("int64")
df = df.set_index("id")

players_df = pd.DataFrame(players.all_players)
players_df = players_df.astype({
    "id": "int64",
    "market_value": "int64"
})
players_df = players_df.set_index("id")


df = df.join(players_df)
df = df[[
    'name',
    'price',
    'date'
]]
write_db(df, "price_history")
