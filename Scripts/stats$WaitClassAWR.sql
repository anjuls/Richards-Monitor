SELECT 'CPU time' wait_class
,      ? /100 time 
FROM dual 
WHERE ? > 0 
UNION ALL 
SELECT e.wait_class
,      SUM(((e.time_waited_micro - NVL(b.time_waited_micro, 0)) /1000000)) time 
FROM dba_hist_system_event b
,    dba_hist_system_event e 
WHERE b.snap_id (+) = ? 
  AND e.snap_id = ?
  AND b.dbid (+) = ? 
  AND e.dbid = ? 
  AND b.instance_number (+) = ?
  AND e.instance_number = ?
  AND b.event_name (+) = e.event_name 
  AND e.total_waits > NVL(b.total_waits, 0) 
  AND e.wait_class != 'Idle'
GROUP BY e.wait_class 
ORDER BY time desc
/

