SELECT status
,      COUNT(*) 
FROM gv$session s 
WHERE type = 'USER'
  AND s.inst_id = ? 
GROUP BY status 
UNION ALL 
SELECT 'RMan Processes' status
,      COUNT(*) 
FROM gv$session s 
WHERE s.inst_id = ? 
  AND (   s.program like 'rman%'
       OR s.module like 'backup%') 
