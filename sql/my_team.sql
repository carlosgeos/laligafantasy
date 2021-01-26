-- Put bought players in
insert into public.myteam
select id,
       name,
       null as bought_at
from league_players
join players using(id)
where id in (select id from league_players where manager_id = <<MANAGER_ID>>)
and id not in (select id from public.myteam);

-- Take players out (sale, etc)
delete from public.myteam
where id not in (select id from public.league_players where manager_id = <<MANAGER_ID>>);
