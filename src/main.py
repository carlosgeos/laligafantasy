import sys
from . import market
from . import league_players
from . import ranking
from . import players
from . import price_history
from . import db
from .log.core import logger


def hourly_handler(event, context):
    """App entrypoint. Executed hourly or several times per day ideally

    """
    market.handler(event, context)
    league_players.handler(event, context)


def daily_handler(event, context):
    """App entrypoint. Executed daily

    """
    players.handler(event, context)
    ranking.handler(event, context)
    price_history.handler(event, context)
    db.build_tables()


if __name__ == '__main__':
    logger.info(f"Using Python version {sys.version}")
    hourly_handler("some_event", "some_context")
    daily_handler("some_event", "some_context")
