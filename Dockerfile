FROM python:3-slim
RUN apt-get update && apt-get install -y libpq-dev build-essential

WORKDIR /src/laliga

# One has to remember to
# $ pipenv lock -r > requirements.txt
# if packages have changed for the Dockerfile to pick it up
COPY requirements.txt /tmp
RUN pip install -r /tmp/requirements.txt

COPY . /src/laliga

#RUN pip install /tmp/laliga
CMD ["python", "market.py"]
