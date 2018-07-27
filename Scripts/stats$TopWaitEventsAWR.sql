SELECT event_name
,      time 
FROM (SELECT event_name
      ,      time 
      FROM (SELECT e.event_name
            ,      ((e.time_waited_micro - NVL(b.time_waited_micro, 0)) /1000000) time 
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
            UNION ALL 
            SELECT 'CPU time' event_name
            ,      ? /100 time 
            FROM dual 
            WHERE ? > 0 ) 
      ORDER BY time desc ) 
WHERE rownum <= 10 