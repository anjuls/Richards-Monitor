SELECT b.begin_interval_time
,      a.capture_name
,      a.lag "Lag(s)"
,      a.startup_time
,      a.total_messages_captured
,      a.total_messages_enqueued
,      a.elapsed_redo_wait_time
,      a.elapsed_rule_time
,      a.elapsed_enqueue_time
,      a.elapsed_pause_time
FROM dba_hist_streams_capture a
,    dba_hist_snapshot b 
WHERE a.snap_id = b.snap_id 
  AND a.instance_number = b.instance_number 
  AND a.snap_id = ?
  AND a.instance_number = ?
  AND a.dbid = ?
ORDER BY 1, 2 
/
