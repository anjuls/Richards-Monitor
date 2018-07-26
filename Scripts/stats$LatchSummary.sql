SELECT b.name name
,      e.gets - b.gets "Get|Requests"
,      ROUND(TO_NUMBER(DECODE(e.gets, b.gets, null, (e.misses - b.misses) * 100 / (e.gets - b.gets))), 2) "Pct|Get|Miss"
,      ROUND(TO_NUMBER(DECODE(e.misses, b.misses, null, (e.sleeps - b.sleeps) / (e.misses - b.misses))), 2) "Avg|Slps|/Miss"
,      ROUND((e.wait_time - b.wait_time) /1000000, 2) "Wait|Time|(s)"
,      e.immediate_gets - b.immediate_gets "NoWait|Requests"
,      ROUND(TO_NUMBER(DECODE(e.immediate_gets, b.immediate_gets, null, (e.immediate_misses - b.immediate_misses) * 100 / (e.immediate_gets - b.immediate_gets))), 2) "Pct|NoWait|Miss" 
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
  AND (e.gets - b.gets + e.immediate_gets - b.immediate_gets) > 0 
ORDER BY b.name 
