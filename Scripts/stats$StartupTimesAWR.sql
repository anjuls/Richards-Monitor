SELECT COUNT(DISTINCT startup_time) 
FROM dba_hist_snapshot 
WHERE snap_id BETWEEN ? AND ? 
  AND dbid = ? 
  AND instance_number = ? 