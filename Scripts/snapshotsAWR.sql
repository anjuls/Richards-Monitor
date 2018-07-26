SELECT TO_CHAR(end_interval_time, 'dd/mm/yy hh24:mi:ss')
,      snap_id 
FROM dba_hist_snapshot 
WHERE dbid = ? 
  AND instance_number = ? 
ORDER BY end_interval_time 