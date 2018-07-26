SELECT e.name name
,      e.gets - NVL(b.gets, 0) gets
,      e.waits - NVL(b.waits, 0) waits 
FROM stats$enqueuestat b
,    stats$enqueuestat e 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ? 
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.dbid (+) = e.dbid 
  AND b.instance_number (+) = ? 
  AND e.instance_number = ? 
  AND b.instance_number (+) = e.instance_number 
  AND b.name (+) = e.name 
  AND e.waits - NVL(b.waits, 0) > 0 
ORDER BY waits desc
,        gets desc 

