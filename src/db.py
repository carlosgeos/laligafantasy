import os
import sqlalchemy
from .log.core import logger

engine = sqlalchemy.create_engine(os.environ["DATABASE_URL"])


def build_tables():
    with open("sql/player_trends.sql", "r") as sql_file:
        query = sqlalchemy.text(sql_file.read())
        logger.info("Writing player_trends to db...")
        engine.execute(query)

    with open("sql/my_team.sql", "r") as sql_file:
        query = sqlalchemy.text(sql_file.read().replace("<<MANAGER_ID>>", os.environ["MANAGER_ID"]))
        logger.info("Cleaning myteam table...")
        engine.execute(query)
