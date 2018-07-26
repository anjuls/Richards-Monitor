SELECT stme.VALUE - stmb.VALUE
FROM stats$sys_time_model stmb
,    stats$sys_time_model stme
,    stats$time_model_statname tms 
WHERE stmb.snap_id = ? 
  AND stmb.dbid = ? 
  AND stmb.instance_number = ?
  AND stmb.stat_id = tms.stat_id 
  AND tms.stat_name = ?
  AND stme.snap_id = ? 
  AND stme.dbid = ? 
  AND stme.instance_number = ?
  AND stme.stat_id = tms.stat_id 
/
