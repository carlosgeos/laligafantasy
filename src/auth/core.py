import requests
from functools import cache
from ..log.core import logger


@cache
def code(username, password):
    """Returns the authorization code required to request an auth token

    """
    auth_url = "https://api.laligafantasymarca.com/login/v3/email/auth"
    auth_payload = {"policy": "B2C_1A_ResourceOwnerv2",
                    "username": username,
                    "password": password}
    code = requests.post(auth_url, data=auth_payload).json()["code"]
    logger.info(f"Received auth code {code[:10]}...")
    return code


@cache
def token(username, password):
    """Returns an authorization token

    """
    token_url = "https://api.laligafantasymarca.com/login/v3/email/token"
    token_payload = {"code": code(username, password),
                     "policy": "B2C_1A_ResourceOwnerv2"}
    token = requests.post(token_url, data=token_payload).json()["access_token"]
    logger.info(f"Received token {token[:10]}...")
    return token
