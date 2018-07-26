SELECT stme.VALUE - stmb.VALUE
FROM dba_hist_sys_time_model stmb
,    dba_hist_sys_time_model stme 
WHERE stmb.snap_id = ? 
  AND stmb.dbid = ? 
  AND stmb.instance_number = ? 
  AND stmb.stat_name = ?
  AND stme.snap_id = ? 
  AND stme.dbid = ? 
  AND stme.instance_number = ?
  AND stme.stat_name = ?
/
