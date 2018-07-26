SELECT event
,      waits
,      time 
FROM (SELECT event
      ,      waits
      ,      time 
      FROM (SELECT e.event event
            ,      e.total_waits - NVL(b.total_waits, 0) waits
            ,      ((e.time_waited_micro - NVL(b.time_waited_micro, 0)) /1000000) time 
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
                                  FROM stats$idle_event ) 
            UNION ALL 
            SELECT 'CPU time' event
            ,      0
            ,      ? /100 time 
            FROM dual 
            WHERE ? > 0 ) 
      ORDER BY time desc ) 
WHERE rownum <= 10