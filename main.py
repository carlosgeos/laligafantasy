import sys
import market
import league_players
import ranking
import players
import price_history
import db
from logs import logger


def hourly_handler(event, context):
    """App entrypoint. Executed hourly

    """
    logger.info(f"Using Python version {sys.version}")
    market.handler(event, context)
    league_players.handler(event, context)
    ranking.handler(event, context)


def daily_handler(event, context):
    """App entrypoint. Executed daily

    """
    logger.info(f"Using Python version {sys.version}")
    players.handler(event, context)
    price_history.handler(event, context)
    db.build_tables()


if __name__ == '__main__':
    daily_handler("some_event", "some_context")
