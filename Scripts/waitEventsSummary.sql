SELECT sw.event
,      COUNT(*) 
FROM gv$session_wait sw 
GROUP BY sw.event 
