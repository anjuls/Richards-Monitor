SELECT value
FROM stats$sysstat
WHERE name = ?
AND   snap_id in (?,?)
AND   dbid = ?
AND   instance_number = ?
ORDER BY snap_id
/