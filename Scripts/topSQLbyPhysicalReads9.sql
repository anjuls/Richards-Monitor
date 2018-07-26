SELECT * 
FROM (SELECT /*+ ordered use_nl (b st) */ LPAD(TO_CHAR((e.disk_reads - NVL(b.disk_reads, 0)), '99,999,999,999'), 15) "disk reads"
      ,      LPAD(TO_CHAR((e.executions - NVL(b.executions, 0)), '999,999,999'), 12) "executions"
      ,      LPAD((TO_CHAR(DECODE(e.executions - NVL(b.executions, 0), 0, TO_NUMBER(null), (e.disk_reads - NVL(b.disk_reads, 0)) / (e.executions - NVL(b.executions, 0))), '999,999,990.0')), 14) "disk reads per execution"
      ,      LPAD((TO_CHAR(100* (e.disk_reads - NVL(b.disk_reads, 0)) /?, '990.0')), 6) "% of total disk reads"
      ,      LPAD(NVL(TO_CHAR((e.cpu_time - NVL(b.cpu_time, 0)) /1000000, '999990.00'), ' '), 8) "cpu time (s)"
      ,      LPAD(NVL(TO_CHAR((e.elapsed_time - NVL(b.elapsed_time, 0)) /1000000, '999990.00'), ' '), 9) "elapsed time (s)"
      ,      e.hash_value "top sql hash value"
      ,      TRIM(SUBSTR(st.sql_text, 1, 50)) "SQL Text" 
      FROM stats$sql_summary e
      ,    stats$sql_summary b
      ,    stats$sqltext st 
      WHERE b.snap_id (+) = ? 
        AND b.dbid (+) = e.dbid 
        AND b.instance_number (+) = e.instance_number 
        AND b.hash_value (+) = e.hash_value 
        AND b.address (+) = e.address 
        AND b.text_subset (+) = e.text_subset 
        AND e.snap_id = ? 
        AND e.dbid = ? 
        AND e.instance_number = ? 
        AND e.hash_value = st.hash_value 
        AND e.text_subset = st.text_subset 
        AND st.piece < 1 
        AND e.executions > NVL(b.executions, 0) 
        AND ? > 0 
      ORDER BY (e.disk_reads - NVL(b.disk_reads, 0)) desc
      ,        e.hash_value
      ,        st.piece ) 
WHERE rownum <= 40;