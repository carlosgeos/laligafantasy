import os
import sqlalchemy
from logs import logger

engine = sqlalchemy.create_engine(os.environ["DATABASE_URL"])


with open("sql/player_trends.sql", "r") as sql_file:
    query = sqlalchemy.text(sql_file.read())
    logger.info("Writing player_trends to db...")
    response = engine.execute(query)
