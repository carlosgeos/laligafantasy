# La Liga Fantasy Companion Tool

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


### Buyouts


### Market


### Ranking
