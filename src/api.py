import os
import requests
from functools import cache
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

from .auth import core as auth

username = os.environ["USERNAME"]
password = os.environ["PASSWORD"]


@cache                          # dodgy singleton for Session
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
    session.headers.update({"Authorization": "Bearer {}".format(auth.token(username, password))})
    return session
