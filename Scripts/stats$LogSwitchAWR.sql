SELECT 'log switches (derived)' st
,      e.sequence# - b.sequence# dif 
FROM dba_hist_thread e
,    dba_hist_thread b 
WHERE b.snap_id = ? 
  AND e.snap_id = ? 
  AND b.dbid = ? 
  AND e.dbid = ? 
  AND b.instance_number = ? 
  AND e.instance_number = ? 
  AND b.thread# = e.thread# 
  AND b.thread_instance_number = e.thread_instance_number 
  AND e.thread_instance_number = ? 
