SELECT type
,      status
,      COUNT(*) 
FROM gv$session s 
GROUP BY type
,        status 
/
