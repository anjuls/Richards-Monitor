SELECT value
FROM dba_hist_sysstat
WHERE stat_name = ?
AND   snap_id in (?,?)
AND   dbid = ?
AND   instance_number = ?
ORDER BY snap_id
/