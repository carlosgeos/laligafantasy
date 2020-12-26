import os
import requests
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

token = os.environ["OAUTH_TOKEN"]

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
