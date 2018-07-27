select s.sql_hash_value
,      s.sql_address
from   v$session s
where  s.sid = ?
/