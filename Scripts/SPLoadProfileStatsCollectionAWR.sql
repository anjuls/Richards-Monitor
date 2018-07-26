SELECT b.stat_name
,      e.value - b.value 
FROM dba_hist_sysstat b
,    dba_hist_sysstat e 
WHERE b.stat_name IN ('redo size', 'session logical reads', 'db block changes', 'physical reads', 'physical writes', 'user calls', 'parse count (hard)', 'parse count (total)', 'sorts (rows)', 'sorts (memory)', 'sorts (disk)', 'logons cumulative', 'execute count', 'user commits', 'recursive calls', 'transaction rollbacks', 'user rollbacks') 
  AND b.stat_name = e.stat_name 
  AND b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.instance_number = ? 
  AND e.instance_number = ? 