SELECT l.sid
,      s.sql_hash_value
,      l.username
,      l.elapsed_seconds
,      l.time_remaining
,      l.start_time
,      l.message
FROM gv$session_longops l
,    gv$session s
WHERE l.sid = s.sid (+)
  AND l.sofar != l.totalwork
  AND l.inst_id = s.inst_id (+)
ORDER BY l.time_remaining desc
 

