SELECT n.stat_name
,      (e.value - b.value) 
FROM stats$osstat b
,    stats$osstat e
,    stats$osstatname n 
WHERE b.osstat_id = n.osstat_id 
  AND b.osstat_id = e.osstat_id 
  AND n.stat_name IN ('AVG_USER_TIME', 'AVG_SYS_TIME', 'AVG_IDLE_TIME', 'AVG_BUSY_TIME') 
  AND b.snap_id = ? 
  AND b.instance_number = ? 
  AND b.dbid = ? 
  AND e.snap_id = ? 
  AND e.instance_number = ? 
  AND e.dbid = ? 
