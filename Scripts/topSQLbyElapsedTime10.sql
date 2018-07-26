SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             NVL(TO_CHAR((e.elapsed_time - NVL(b.elapsed_time, 0)) /1000000, '999990.00'), ' ' ) "Elapsed Time (s)"
      ,      TO_CHAR((e.executions - NVL(b.executions, 0)), '999,999,999' ) "Executions"
      ,      (TO_CHAR(DECODE(e.executions - NVL(b.executions, 0), 0, TO_NUMBER(null), ((e.elapsed_time - NVL(b.elapsed_time, 0)) / (e.executions - NVL(b.executions, 0))) / 1000000), '999990.00')) "Elap per Exec (s)"
      ,      DECODE(?, 0, ' ', TO_CHAR((100 * (e.elapsed_time - NVL(b.elapsed_time, 0)) /?), '990.0')) "% Total"
      ,      NVL(TO_CHAR((e.cpu_time - NVL(b.cpu_time, 0)) /1000000, '999990.00'), ' ' ) "CPU Time (s)"
      ,      TO_CHAR((e.disk_reads - NVL(b.disk_reads, 0)), '99,999,999,999' ) "Physical Reads"
      ,      e.old_hash_value "Top SQL Hash Value"
      ,      st.sql_text "SQL Text" 
      FROM stats$sql_summary e
      ,    stats$sql_summary b
      ,    stats$sqltext st 
      WHERE b.snap_id (+) = ? 
        AND b.dbid (+) = e.dbid 
        AND b.instance_number (+) = e.instance_number 
        AND b.old_hash_value (+) = e.old_hash_value 
        AND b.address (+) = e.address 
        AND b.text_subset (+) = e.text_subset 
        AND e.snap_id = ? 
        AND e.dbid = ? 
        AND e.instance_number = ? 
        AND e.old_hash_value = st.old_hash_value 
        AND e.text_subset = st.text_subset 
        AND st.piece <= 0 
        AND e.executions > NVL(b.executions, 0) 
      ORDER BY (e.elapsed_time - NVL(b.elapsed_time, 0)) desc
      ,        e.old_hash_value
      ,        st.piece ) 
WHERE rownum < 41