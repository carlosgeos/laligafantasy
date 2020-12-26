import requests
import pandas as pd
import auth
from core import clean, write_db

market_url = "https://api.laligafantasymarca.com/api/v3/league/011300439/market"
headers = {"Authorization": "Bearer {}".format(auth.token)}
market = requests.get(market_url, headers=headers).json()

clean_market = sorted([clean(player) for player in market], key=lambda x: x["m_per_point"])

df = pd.DataFrame(clean_market)
df = df[[
    'id',
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
print(df)
#write_db(df, "market")


def get_status(player):
    return "❌" if player["status"] == "injured" else "✅"


def get_change_percent(player):
    percent = player["change"]
    if percent > 0:
        return "{:.2f}%".format(percent)
    elif percent < 0:
        return "{:.2f}%".format(percent)
    else:
        return str(percent)


def report():
    print("{:<6}{:<20}{:>12}{:>12}{:>12}{:>12}".format("Forma", "Jugador", "€M/Pts", "Potencial", "LL19/20", "%"))
    print("{:<6}{:<20}{:>12}{:>12}{:>12}{:>12}".format("-----", "-------", "------", "---------", "-------", "-"))
    for p in clean_market:
        print("{:<6}{:<20}{:>12.3f}{:>12.2f}{:>12.2f}{:>12}".format(get_status(p), p["name"], p["m_per_point"], p["potential"], p["last_season_points"] if p["last_season_points"] else 0, get_change_percent(p) if p["last_season_points"] else ""))
