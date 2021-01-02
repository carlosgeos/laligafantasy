# Main entrypoint
import sys
import market
import league_players
import ranking
import players
import price_history
from logs import logger


def handler(event, context):
    logger.info(f"Using Python version {sys.version}")
    market.main()
    league_players.main()
    ranking.main()
    players.main()
    price_history.main()


handler("some event", "some_context")
