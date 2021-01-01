# Main entrypoint
import sys


def handler(event, context):
    print(f"Using PYthon version {sys.version}")
    print("event:")
    print(event)
    print("context:")
    print(context)
