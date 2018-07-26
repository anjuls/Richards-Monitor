SELECT n.stat_name
,      (e.value - b.value) 
FROM dba_hist_osstat b
,    dba_hist_osstat e
,    dba_hist_osstat_name n 
WHERE b.stat_id = n.stat_id 
  AND b.stat_id = e.stat_id 
  AND n.stat_name IN ('USER_TIME', 'SYS_TIME', 'IDLE_TIME', 'BUSY_TIME','IOWAIT_TIME') 
  AND b.snap_id = ? 
  AND b.instance_number = ? 
  AND b.dbid = ? 
  AND e.snap_id = ? 
  AND e.instance_number = ? 
  AND e.dbid = ? 
