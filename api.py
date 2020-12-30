import os
import requests
from logs import logger
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

username = os.environ["USERNAME"]
password = os.environ["PASSWORD"]

auth_url = "https://api.laligafantasymarca.com/login/v3/email/auth"
auth_payload = {"policy": "B2C_1A_ResourceOwnerv2",
                "username": username,
                "password": password}
code = requests.post(auth_url, data=auth_payload).json()["code"]
logger.info(f"Received auth code {code[:10]}...")

token_url = "https://api.laligafantasymarca.com/login/v3/email/token"
token_payload = {"code": code,
                 "policy": "B2C_1A_ResourceOwnerv2"}
token = requests.post(token_url, data=token_payload).json()["access_token"]
logger.info(f"Received token {token[:10]}...")

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
session.headers.update({"Authorization": "Bearer {}".format(token)})
