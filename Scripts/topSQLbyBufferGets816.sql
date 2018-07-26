SELECT gets "buffer gets"
,      execs "executions"
,      ROUND(gpx, 2) "buffer gets per execution"
,      ROUND(rel_pct, 2) "% of total gets"
,      hashval "top sql hash value"
,      TRIM(sql_text) 
FROM (SELECT e.buffer_gets - NVL(b.buffer_gets, 0) gets
      ,      e.executions - NVL(b.executions, 0) execs
      ,      DECODE((e.executions - NVL(b.executions, 0)), 0, TO_NUMBER(null), (e.buffer_gets - NVL(b.buffer_gets, 0)) / (e.executions - NVL(b.executions, 0))) gpx
      ,      100* (e.buffer_gets - NVL(b.buffer_gets, 0)) /? rel_pct
      ,      e.hash_value hashval
      ,      e.sql_text sql_text 
      FROM stats$sql_summary b
      ,    stats$sql_summary e 
      WHERE b.snap_id (+) = ? 
        AND e.snap_id = ? 
        AND b.dbid (+) = ? 
        AND e.dbid = ? 
        AND b.instance_number (+) = ? 
        AND e.instance_number = ? 
        AND b.hash_value (+) = e.hash_value 
        AND b.address (+) = e.address 
        AND e.executions > NVL(b.executions, 0) 
      ORDER BY gets desc ) 
WHERE rownum <= 40;