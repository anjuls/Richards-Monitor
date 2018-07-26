SELECT sw.event
,      COUNT(*) 
FROM gv$session_wait sw 
,    gv$session s
WHERE sw.inst_id = s.inst_id
and   sw.sid = s.sid
GROUP BY sw.event 
