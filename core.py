import pandas as pd
from logs import logger
from db import engine


def m_per_point(player):
    """Calculates the effective price (in millions) per point one would
    pay for the player

    AVG Points are max'ed with 0.01 to avoid division by zero

    """
    price = max(player["playerMaster"]["marketValue"], player["salePrice"])
    avg_points = player["playerMaster"]["averagePoints"]
    return (price / max(avg_points, 0.01)) / 1000000


def m_per_point_2(player):
    """Lite version of the same function. Accounts for API endpoints
    discrepancies

    """
    price = int(player["marketValue"])
    avg_points = player["averagePoints"]
    return (price / max(avg_points, 0.01)) / 1000000


def change(player):
    """Performance change from last season to this season

    """
    this_season_forecast = player["playerMaster"]["averagePoints"] * 38
    last_season_points = max(player["playerMaster"]["lastSeasonPoints"], 0.001) if "lastSeasonPoints" in player["playerMaster"] else None
    return ((this_season_forecast / last_season_points) - 1) * 100 if last_season_points is not None else 0


def change_2(player):
    """Performance change from last season to this season

    """
    this_season_forecast = player["averagePoints"] * 38
    last_season_points = max(int(player["lastSeasonPoints"]), 0.001) if "lastSeasonPoints" in player else None
    return ((this_season_forecast / last_season_points) - 1) * 100 if last_season_points is not None else 0


def clean(player):
    """JSON filtering and basic transformations

    """
    new_player = {
        "id": int(player["playerMaster"]["id"]),
        "name": player["playerMaster"]["nickname"],
        "status": player["playerMaster"]["playerStatus"],
        "team": player["playerMaster"]["team"]["name"],
        "market_value": player["playerMaster"]["marketValue"],
        "sale_price": player["salePrice"],
        "price": max(player["playerMaster"]["marketValue"], player["salePrice"]),
        "points": player["playerMaster"]["points"],
        "last_season_points": player["playerMaster"]["lastSeasonPoints"] if "lastSeasonPoints" in player["playerMaster"] else None,
        "avg_points": player["playerMaster"]["averagePoints"],
        "potential": player["playerMaster"]["averagePoints"] * 38,
        "m_per_point": m_per_point(player),
        "change": change(player)
    }
    return new_player


def clean_2(player):
    """JSON filtering and fewer transformations. Used on a different
    endpoint that does serialisation differently

    """
    new_player = {
        "id": int(player["id"]),
        "name": player["nickname"],
        "position": int(player["positionId"]),
        "status": player["playerStatus"],
        "team": player["team"]["name"],
        "market_value": int(player["marketValue"]),
        "points": player["points"],
        "last_season_points": int(player["lastSeasonPoints"]) if "lastSeasonPoints" in player else None,
        "avg_points": player["averagePoints"],
        "potential": player["averagePoints"] * 38,
        "m_per_point": m_per_point_2(player),
        "change": change_2(player)
    }
    return new_player


def clean_3(manager_id, player):
    """Same kind of transform, for the 'team' (manager) endpoint

    """
    new_player = {
        "id": int(player["playerMaster"]["id"]),
        "manager_id": int(manager_id),
        "clause": int(player["buyoutClause"]),
        "clause_lock_end": pd.to_datetime(player["buyoutClauseLockedEndTime"], utc=True)
    }
    return new_player


def clean_4(team):
    """Same kind of transform, for the 'ranking' endpoint

    """
    new_team = {
        "id": int(team["team"]["id"]),
        "position": int(team["position"]),
        "points": int(team["points"]),
        "name": team["team"]["manager"]["managerName"],
        "value": int(team["team"]["teamValue"]),
        "team_points": int(team["team"]["teamPoints"]),
        "ts": pd.to_datetime("today")
    }
    return new_team


def write_db(df, table_name):
    """Writes dataframe df to table table_name using the imported
    SQLAlchemy engine.

    """
    logger.info(f"Rebuilding and loading {table_name}...")
    df.to_sql(table_name, con=engine, if_exists='replace', method='multi')
    logger.info("done")


def insert_db(df, table_name):
    """Same as write_db, only inserting instead of truncating and
    reloading.

    """
    logger.info(f"Appending to {table_name}...")
    df.to_sql(table_name, con=engine, if_exists='append', method='multi')
    logger.info("done")
