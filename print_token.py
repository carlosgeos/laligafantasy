from api import token


def print_token():
    """Prints the fetched token to stdout. Valid for 24 hours

    $ python print_token.py

    """
    print(token)


if __name__ == '__main__':
    print_token()
