SELECT value
FROM dba_hist_dlm_misc
WHERE name = ?
AND   snap_id in (?,?)
AND   dbid = ?
AND   instance_number = ?
ORDER BY snap_id
/