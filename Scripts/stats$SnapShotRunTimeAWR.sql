SELECT ROUND(((TO_DATE(TO_CHAR(e.end_interval_time, 'dd-mon-yyyy hh24:mi:ss'), 'dd-mon-yyyy hh24:mi:ss') - TO_DATE(TO_CHAR(e.begin_interval_time, 'dd-mon-yyyy hh24:mi:ss'), 'dd-mon-yyyy hh24:mi:ss')) * 1440 * 60), 0) 
FROM dba_hist_snapshot e
WHERE e.snap_id = ? 
  AND e.dbid = ?
  AND e.instance_number = ?
/
