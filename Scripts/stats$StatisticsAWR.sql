SELECT b.stat_name
,      ROUND((e.value - b.value), 2) 
FROM dba_hist_sysstat b
,    dba_hist_sysstat e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.stat_name = e.stat_name 
  AND b.stat_name = ? 
ORDER BY stat_name