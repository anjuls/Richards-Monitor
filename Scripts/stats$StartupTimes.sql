SELECT COUNT(DISTINCT startup_time) 
FROM stats$snapshot 
WHERE snap_id BETWEEN ? AND ? 
  AND dbid = ? 
  AND instance_number = ? 