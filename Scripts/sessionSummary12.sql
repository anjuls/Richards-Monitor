SELECT type
,      status
,      COUNT(*) 
FROM gv$session s 
,    v$containers c
WHERE c.con_id = s.con_id
GROUP BY type
,        status 
/
