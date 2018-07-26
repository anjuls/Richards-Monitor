SELECT session_id "Sid"
,      TO_CHAR(sample_time, 'dd-mon-yyyy hh24:mi:ss' ) "Sample Time"
,      event
,      p1
,      p2
,      p3
,      wait_time
,      time_waited
,      sql_id
,      session_state
,      blocking_session
,      blocking_session_status
,      current_file#
,      current_block#
FROM gv$active_session_history
WHERE session_id = ?
  AND inst_id = ?
  AND session_serial# = ?
  AND sample_time > sysdate - (?/1440)
order by sample_time
/
