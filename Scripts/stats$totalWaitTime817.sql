SELECT b.time_waited + e.time_waited 
FROM stats$system_event b
,    stats$system_event e 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.instance_number (+) = ? 
  AND e.instance_number (+) = ? 
