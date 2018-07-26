SELECT event
,      time 
FROM (SELECT e.event event
      ,      (e.time_waited - NVL(b.time_waited, 0)) /100 time 
      FROM stats$system_event b
      ,    stats$system_event e 
      WHERE b.snap_id (+) = ? 
        AND e.snap_id = ? 
        AND b.dbid (+) = ? 
        AND e.dbid = ? 
        AND b.instance_number (+) = ? 
        AND e.instance_number = ? 
        AND b.event (+) = e.event 
        AND e.total_waits > NVL(b.total_waits, 0) 
        AND e.event not IN (SELECT event 
                            FROM stats$idle_event 
                            UNION ALL 
                            SELECT 'slave wait'
                            FROM dual ) 
      ORDER BY time desc ) 
WHERE rownum <= 10 
