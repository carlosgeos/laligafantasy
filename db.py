import os
from sqlalchemy import create_engine

engine = create_engine(os.environ["DATABASE_URL"])

# Create
# db.execute("CREATE TABLE IF NOT EXISTS films (title text, director text, year text)")
# db.execute("INSERT INTO films (title, director, year) VALUES ('Doctor Strange', 'Scott Derrickson', '2016')")

# Read
# result_set = db.execute("SELECT * FROM films")
# for r in result_set:
#     print(r)

# Update
#db.execute("UPDATE films SET title='Some2016Film' WHERE year='2016'")

# Delete
#db.execute("DELETE FROM films WHERE year='2016'")
