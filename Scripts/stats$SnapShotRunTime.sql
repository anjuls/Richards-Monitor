SELECT ROUND(((e.snap_time - b.snap_time) * 1440 * 60), 0) 
FROM stats$snapshot b
,    stats$snapshot e 
WHERE b.snap_id = ? 
  AND b.dbid = ?
  AND b.instance_number = ?
  AND e.snap_id = ? 
  AND e.dbid = ?
  AND e.instance_number = ?
