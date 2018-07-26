SELECT event_name
,      (CASE 
        WHEN total_waits <= 9999 THEN TO_CHAR(total_waits) || ' '
        WHEN TRUNC(total_waits /1000) <= 9999 THEN TO_CHAR(TRUNC(total_waits /1000)) || 'K'
        WHEN TRUNC(total_waits /1000000) <= 9999 THEN TO_CHAR(TRUNC(total_waits /1000000)) || 'M'
        WHEN TRUNC(total_waits /1000000000) <= 9999 THEN TO_CHAR(TRUNC(total_waits /1000000000)) || 'G'
        WHEN TRUNC(total_waits /1000000000000) <= 9999 THEN TO_CHAR(TRUNC(total_waits /1000000000000)) || 'T'
        ELSE TO_CHAR(TRUNC(total_waits /1000000000000000)) || 'P'
        END) total_waits_str
,      SUBSTR(TO_CHAR(DECODE(to1, 0, TO_NUMBER(null), 100 * to1 /total_waits), '999.9MI'), 1, 5) "<1ms"
,      SUBSTR(TO_CHAR(DECODE(to2, 0, TO_NUMBER(null), 100 * to2 /total_waits), '999.9MI'), 1, 5) "<2ms"
,      SUBSTR(TO_CHAR(DECODE(to4, 0, TO_NUMBER(null), 100 * to4 /total_waits), '999.9MI'), 1, 5) "<4ms"
,      SUBSTR(TO_CHAR(DECODE(to8, 0, TO_NUMBER(null), 100 * to8 /total_waits), '999.9MI'), 1, 5) "<8ms"
,      SUBSTR(TO_CHAR(DECODE(to16, 0, TO_NUMBER(null), 100 * to16 /total_waits), '999.9MI'), 1, 5) "<16ms"
,      SUBSTR(TO_CHAR(DECODE(to32, 0, TO_NUMBER(null), 100 * to32 /total_waits), '999.9MI'), 1, 5) "<32ms"
,      SUBSTR(TO_CHAR(DECODE(to1024, 0, TO_NUMBER(null), 100 * to1024 /total_waits), '999.9MI'), 1, 5) "<1s"
,      SUBSTR(TO_CHAR(DECODE(over, 0, TO_NUMBER(null), 100 * over /total_waits), '999.9MI'), 1, 5) ">1s"
FROM (SELECT h.event_name event_name
      ,      SUM(h.wait_count) total_waits
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 1) THEN h.wait_count 
                 ELSE 0 
                 END) to1
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 2) THEN h.wait_count 
                 ELSE 0 
                 END) to2
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 4) THEN h.wait_count 
                 ELSE 0 
                 END) to4
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 8) THEN h.wait_count 
                 ELSE 0 
                 END) to8
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 16) THEN h.wait_count 
                 ELSE 0 
                 END) to16
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli = 32) THEN h.wait_count 
                 ELSE 0 
                 END) to32
      ,      SUM(CASE 
                 WHEN (h.wait_time_milli BETWEEN 64 AND 1024) THEN h.wait_count 
                 ELSE 0 
                 END) to1024
      ,      SUM(CASE 
                 WHEN (1024 < h.wait_time_milli) THEN h.wait_count 
                 ELSE 0 
                 END) over
      ,      DECODE(h.wait_class, 'Idle', 99, 0) idle 
      FROM (SELECT e.event_name
            ,      e.wait_class
            ,      e.wait_time_milli
            ,      e.wait_count - NVL(b.wait_count, 0) AS wait_count 
            FROM dba_hist_event_histogram b
            ,    dba_hist_event_histogram e 
            WHERE b.snap_id (+) = ? 
              AND e.snap_id = ? 
              AND b.dbid (+) = ? 
              AND e.dbid = ? 
              AND b.instance_number (+) = ? 
              AND e.instance_number = ? 
              AND b.event_id (+) = e.event_id 
              AND b.wait_time_milli (+) = e.wait_time_milli 
              AND e.wait_count > NVL(b.wait_count, 0)) h 
      GROUP BY h.event_name
      ,        DECODE(h.wait_class, 'Idle', 99, 0)) 
ORDER BY idle
,        event_name 