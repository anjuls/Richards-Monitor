SELECT b.name
,      ROUND((e.value - b.value), 2) 
FROM stats$sysstat b
,    stats$sysstat e 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.name = e.name 
  AND b.name = ? 
ORDER BY name 