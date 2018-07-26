select name,value
from v$sysstat
where name = 'parse count (total)'
union all
select name,value
from v$sysstat 
where name = 'parse count (hard)'
union all
select s.name ,round((s.value * p.value)/1024/1024,1)
from v$sysstat s
,    v$parameter p
where s.name = 'physical reads'
and p.name = 'db_block_size'
union all
select s.name ,round((s.value * p.value)/1024/1024,1)
from v$sysstat s
,    v$parameter p
where s.name = 'physical writes'
and p.name = 'db_block_size'
union all
select 'sessions',count(*)
from v$session
union all
select s.name ,round((s.value * p.value)/1024/1024,1)
from v$sysstat s
,    v$parameter p
where s.name = 'redo blocks written'
and p.name = 'db_block_size'
union all
select name,value
from v$sysstat
where name = 'execute count'
order by 1
/
