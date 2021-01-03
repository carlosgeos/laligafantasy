drop table if exists player_trends;
create table player_trends as
with last_dates as (
    select id,
           date,
           row_number() over (partition by id order by date desc) rn_desc,
           row_number() over (partition by id order by date)      rn_asc
    from price_history
),
     today_price as (
         select id,
                price as today_price
         from price_history
                  join last_dates using (id, date)
         where rn_desc = 1
         group by id, price
     ),
     yesterday_price as (
         select id,
                price as yesterday_price
         from price_history
                  join last_dates using (id, date)
         where rn_desc = 2
         group by id, price
     ),
     yesterday as (
         select id,
                regr_slope(price, rn_asc) as yesterday_slope
         from price_history
                  join last_dates using (id, date)
         where last_dates.rn_desc <= 2
         group by id
     ),
     four_day_price as (
         select id,
                price as four_day_price
         from price_history
                  join last_dates using (id, date)
         where rn_desc = 4
         group by id, price
     ),
     four_days as (
         select id,
                regr_slope(price, rn_asc) as four_day_slope
         from price_history
                  join last_dates using (id, date)
         where last_dates.rn_desc <= 4
         group by id
     ),
     ten_day_price as (
         select id,
                price as ten_day_price
         from price_history
                  join last_dates using (id, date)
         where rn_desc = 10
         group by id, price
     ),
     ten_days as (
         select id,
                regr_slope(price, rn_asc) as ten_day_slope
         from price_history
                  join last_dates using (id, date)
         where last_dates.rn_desc <= 10
         group by id
     )
select id,
       yesterday_slope,
       four_day_slope,
       ten_day_slope,
       ((today_price::numeric / yesterday_price::numeric) - 1) as yesterday_change,
       ((today_price::numeric / four_day_price::numeric) - 1)  / 3 as four_day_change,
       ((today_price::numeric / ten_day_price::numeric) - 1)  / 9 as ten_day_change,
       current_timestamp as last_refreshed
from yesterday
         join four_days using (id)
         join ten_days using (id)
         join today_price using (id)
         join yesterday_price using (id)
         join four_day_price using (id)
         join ten_day_price using (id);
