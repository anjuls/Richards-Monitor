SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             ROUND((e.cpu_time - NVL(b.cpu_time, 0)) /1000000, 2) "CPU Time (s)"
      ,      ROUND(e.executions - NVL(b.executions, 0), 1) "Executions (s)"
      ,      ROUND(((e.cpu_time - NVL(b.cpu_time, 0)) / (e.executions - NVL(b.executions, 0))) / 1000000,2) "CPU per Execution (s)"
      ,      ROUND(100 * (e.cpu_time - NVL(b.cpu_time, 0)) /?, 2) "% Total DB Time"
      ,      ROUND(((e.elapsed_time - NVL(b.elapsed_time, 0)) /1000000), 2) "Elapsed Time (s)"
      ,      e.buffer_gets - b.buffer_gets "Buffer Gets"
      ,      e.old_hash_value "Top SQL Hash Value"
      ,      SUBSTR(st.sql_text, 1, 80) "SQL Text"
      ,      e.old_hash_value 
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
        AND st.piece = 0 
        AND e.executions > NVL(b.executions, 0) 
      ORDER BY (e.cpu_time - NVL(b.cpu_time, 0)) desc
      ,        e.old_hash_value
      ,        st.piece ) 
WHERE rownum < 41 
/
