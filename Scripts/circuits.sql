select 'v$circuit#',count(*)
from v$circuit
union all
select status,count(*)
from v$circuit
group by status
/