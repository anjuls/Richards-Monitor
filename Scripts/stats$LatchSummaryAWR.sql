SELECT b.latch_name name
,      e.gets - b.gets "Get|Requests"
,      ROUND(TO_NUMBER(DECODE(e.gets, b.gets, null, (e.misses - b.misses) * 100 / (e.gets - b.gets))), 2) "Pct|Get|Miss"
,      ROUND(TO_NUMBER(DECODE(e.misses, b.misses, null, (e.sleeps - b.sleeps) / (e.misses - b.misses))), 2) "Avg|Slps|/Miss"
,      ROUND((e.wait_time - b.wait_time) /1000000, 2) "Wait|Time|(s)"
,      e.immediate_gets - b.immediate_gets "NoWait|Requests"
,      ROUND(TO_NUMBER(DECODE(e.immediate_gets, b.immediate_gets, null, (e.immediate_misses - b.immediate_misses) * 100 / (e.immediate_gets - b.immediate_gets))), 2) "Pct|NoWait|Miss" 
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
  AND (e.gets - b.gets + e.immediate_gets - b.immediate_gets) > 0 
ORDER BY b.latch_name 
