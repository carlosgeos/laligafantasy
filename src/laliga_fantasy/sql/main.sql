-- :name drop-players-table :!
drop table if exists players cascade;

-- :name drop-price-history-table :!
drop table if exists price_history cascade;

-- :name drop-daily-market-table :!
drop table if exists daily_market cascade;

-- :name drop-owned-players-table :!
drop table if exists owned_players cascade;

-- :name drop-price-trends-table :!
drop table if exists price_trends cascade;

-- :name drop-activity-table :!
drop table if exists activity cascade;

-- :name create-players-table
-- :command :execute
-- :result :raw
create table if not exists players (
    id serial primary key,
    player_id integer,
    name text,
    position char(3),
    status text,
    points integer,
    market_value integer,
    points_last_season integer,
    avg_points float
);

-- :name create-price-history-table
-- :command :execute
-- :result :raw
create table if not exists price_history (
    id serial primary key,
    player_id integer,
    market_value integer,
    ts timestamptz,
    created_at timestamptz
);

-- :name create-daily-market-table
-- :command :execute
-- :result :raw
create table if not exists daily_market (
    id serial primary key,
    player_id integer,
    owner text,
    direct_offer text,
    sale_price integer,
    offers integer,
    bids integer,
    expiration timestamptz,
    created_at timestamptz
);

-- :name create-owned-players-table
-- :command :execute
-- :result :raw
create table if not exists owned_players (
    id serial primary key,
    player_id integer,
    manager text,
    manager_id text,
    league_manager_id text,
    buyout integer,
    buyout_lock_expiration timestamptz,
    created_at timestamptz
);

-- :name create-price-trends-table
-- :command :execute
-- :result :raw
create table if not exists price_trends (
    id serial primary key,
    player_id integer,
    change_3d numeric,
    change_7d numeric,
    change_14d numeric,
    change_30d numeric,
    change_all numeric,
    created_at timestamptz
);

-- :name create-activity-table
-- :command :execute
-- :result :raw
create table if not exists activity (
    id serial primary key,
    transaction_id integer,
    publication_date timestamptz,
    origin text,
    destination text,
    player_name text,
    tx_type text,
    amount integer,
    created_at timestamptz
);

-- :name create-my-team-view :!
-- :command :execute
-- :result :raw
create or replace view my_team as (
       select name, position, avg_points, points_last_season, status
       from owned_players
       join players p on owned_players.player_id = p.player_id
       where manager_id = :sql:manager-id::text
);
