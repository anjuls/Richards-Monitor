SELECT b.snap_id "Begin Snapshot Id"
,      e.snap_id "End Snapshot Id"
,      TO_CHAR(b.end_interval_time, 'dd-Mon-yy hh24:mi:ss' ) "Begin Snapshot Time"
,      TO_CHAR(e.end_interval_time, 'dd-Mon-yy hh24:mi:ss' ) "End Snapshot Time"
,      ROUND(((EXTRACT(day FROM (es.end_interval_time - bs.end_interval_time) day to second) * 86400) + (EXTRACT(hour FROM (es.end_interval_time - bs.end_interval_time)) * 3600) + (EXTRACT(minute FROM (es.end_interval_time - bs.end_interval_time)) * 60) + (EXTRACT(second FROM (es.end_interval_time - bs.end_interval_time)))) / 60, 2) "mins"
,      blog.value "Begin Snapshot Sessions"
,      elog.value "End Snapshot Sessions"
,      ROUND(bocur.value /blog.value, 2) "Begin Snapshot Curs Per Sess"
,      ROUND(eocur.value /elog.value, 2) "End Snapshot Curs Per Sess" 
FROM dba_hist_snapshot b
,    dba_hist_snapshot e
,    (SELECT VALUE
      FROM dba_hist_sysstat 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? 
        AND stat_name = 'logons current') blog
,    (SELECT VALUE
      FROM dba_hist_sysstat 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? 
        AND stat_name = 'opened cursors current') eocur
,    (SELECT VALUE
      FROM dba_hist_sysstat 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? 
        AND stat_name = 'opened cursors current') bocur
,    (SELECT VALUE
      FROM dba_hist_sysstat 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? 
        AND stat_name = 'logons current') elog
,    (SELECT end_interval_time 
      FROM dba_hist_snapshot 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? ) bs
,    (SELECT end_interval_time 
      FROM dba_hist_snapshot 
      WHERE snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? ) es 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.startup_time = e.startup_time 
  AND b.end_interval_time < e.end_interval_time 