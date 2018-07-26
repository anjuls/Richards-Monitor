SELECT n.stat_name
,      (e.value - b.value) 
FROM dba_hist_osstat b
,    dba_hist_osstat e
,    dba_hist_osstat_name n 
WHERE b.stat_id = n.stat_id 
  AND b.stat_id = e.stat_id 
  AND n.stat_name IN ('AVG_USER_TICKS', 'AVG_SYS_TICKS', 'AVG_IDLE_TICKS', 'AVG_BUSY_TICKS') 
  AND b.snap_id = ? 
  AND b.instance_number = ? 
  AND b.dbid = ? 
  AND e.snap_id = ? 
  AND e.instance_number = ? 
  AND e.dbid = ? 
