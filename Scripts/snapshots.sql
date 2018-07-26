SELECT TO_CHAR(snap_time, 'dd/mm/yy hh24:mi:ss' )
,      snap_id 
FROM stats$snapshot 
WHERE dbid = ? 
  AND instance_number = ? 
ORDER BY snap_time 