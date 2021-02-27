import os
import sqlalchemy
from .log.core import logger

BASE_PATH = os.path.dirname(os.path.abspath(__file__))
engine = sqlalchemy.create_engine(os.environ["DATABASE_URL"])


def build_tables():
    with open(f"{BASE_PATH}/sql/player_trends.sql", "r") as sql_file:
        query = sqlalchemy.text(sql_file.read())
        logger.info("Writing player_trends to db...")
        engine.execute(query)

    with open(f"{BASE_PATH}/sql/my_team.sql", "r") as sql_file:
        query = sqlalchemy.text(sql_file.read().replace("<<MANAGER_ID>>", os.environ["MANAGER_ID"]))
        logger.info("Cleaning myteam table...")
        engine.execute(query)
