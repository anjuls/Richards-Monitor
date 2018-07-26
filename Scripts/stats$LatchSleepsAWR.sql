SELECT b.latch_name
,      e.gets - b.gets "Gets|Requests"
,      e.misses - b.misses misses
,      e.sleeps - b.sleeps sleeps
,      e.spin_gets - b.spin_gets "spin|gets" 
FROM dba_hist_latch b
,    dba_hist_latch e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.latch_name = e.latch_name 
  AND e.sleeps - b.sleeps > 0 
ORDER BY misses desc 
