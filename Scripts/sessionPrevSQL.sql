select sql_text "Session Last SQL"
from   v$session s
,      v$sqltext sq
where  s.sid = ?
and    s.prev_hash_value = sq.hash_value
and    s.prev_sql_addr = sq.address
order by piece
/