SELECT b.name
,      e.gets - b.gets "Gets|Requests"
,      e.misses - b.misses misses
,      e.sleeps - b.sleeps sleeps
,      e.spin_gets - b.spin_gets "spin|gets" 
FROM stats$latch b
,    stats$latch e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.name = e.name 
  AND e.sleeps - b.sleeps > 0 
ORDER BY misses desc 
