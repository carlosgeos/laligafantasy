import logging


def aws_lambda_patch():
    """Required to get logging working on AWS Lambda

    https://stackoverflow.com/questions/37703609/using-python-logging-with-aws-lambda

    """
    root = logging.getLogger()
    if root.handlers:
        for handler in root.handlers:
            root.removeHandler(handler)


aws_lambda_patch()
logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s')
logger = logging.getLogger()
