SELECT event
,      p1
,      p2
,      p3
,      wait_time "Wait Time (CS)" 
FROM gv$session_wait_history 
WHERE sid = ?
  AND inst_id = ?
ORDER BY seq# asc 
/
