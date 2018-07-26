SELECT e.class
,      e.wait_count - NVL(b.wait_count, 0) "Waits"
,      (e.time - NVL(b.time, 0)) /100 "Total Wait Time (s)"
,      TO_CHAR(ROUND(10 * (e.time - NVL(b.time, 0)) / (e.wait_count - NVL(b.wait_count, 0)), 2), '999,999,990.99' ) "Avg Time (ms)" 
FROM stats$waitstat b
,    stats$waitstat e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.dbid = e.dbid 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.instance_number = e.instance_number 
  AND b.class = e.class 
  AND b.wait_count < e.wait_count 
ORDER BY 3 desc
,        2 desc 
