import os
import requests
from functools import cache
from logs import logger
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

username = os.environ["USERNAME"]
password = os.environ["PASSWORD"]


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


@cache                          # dodgy singleton
def session():
    """Returns an instance of requests.Session, with retries and
    authorization included

    """
    session = requests.Session()
    retry = Retry(
        total=5,
        backoff_factor=0.3,
        status_forcelist=(429, 500, 502, 503, 504),
        method_whitelist=["HEAD", "GET", "OPTIONS"]
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.mount('https://', adapter)
    session.headers.update({"Authorization": "Bearer {}".format(token(username, password))})
    return session
