# La Liga Fantasy Companion Tool

![LaLiga](img/laliga.png)

## About

This tool queries LaLiga Fantasy API for historical data on players, prices, points and other metadata to build dashboards and insights around the game.

The flow is:

- Extract JSON data
- Parse and insert to DB
- Write transforms (SQL)
- Build dashboards on Metabase OR run Pyomo

## Config

Required ENV vars are: `DATABASE_URL`, `USERNAME`, `PASSWORD`,
`LEAGUE_ID` and `MANAGER_ID`

## Usage

```
$ pipenv install
$ pipenv shell
$ python -m src.main
```

## Features

### Overview

The main idea is to fetch and analyse metrics on all of the league
players to determine their value (price vs performance)

### Lineup picker (using [pyomo](https://github.com/Pyomo/pyomo))

![Picker](img/picker.png)

The tool can choose the best possible lineup with the current players
in the squad. The objective function is maximise is the sum of average
points of all players, with the following conditions:

- 11 players
- No injured or suspended players
- One goalkeeper
- One of the following formations:
  - 3-5-2
  - 3-4-3
  - 4-5-1
  - 4-4-2
  - 4-3-3
  - 5-4-1
  - 5-3-2

Obviously, sometimes players can simply sit one out and rest and there
is no way of knowing in advance, but this lineup picker would always
choose the best players in the long run.

### Profits

![Profits](img/profit.png)

### Buyouts

![Buyouts](img/buyouts.png)

### Market

![Market](img/market.png)

### Ranking

Metabase

### Sabotage

Metabase
