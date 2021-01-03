import os
import api

username = os.environ["USERNAME"]
password = os.environ["PASSWORD"]


def handler(event, context):
    """Prints the fetched token to stdout. Valid for 24 hours

    $ python print_token.py

    """
    print(api.token(username, password))


if __name__ == '__main__':
    # AWS Lambda mock
    handler("some_event", "some_context")
