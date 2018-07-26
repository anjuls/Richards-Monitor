SELECT sw.wait_class
,      COUNT(*) 
FROM gv$session_wait sw 
WHERE sw.wait_time = 0 
  AND sw.inst_id = ? 
  AND sw.wait_class != 'Idle'
GROUP BY sw.wait_class
