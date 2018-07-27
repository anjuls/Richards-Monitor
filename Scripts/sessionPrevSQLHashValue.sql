select s.prev_hash_value
,      s.prev_sql_addr
from   v$session s
where  s.sid = ?
/