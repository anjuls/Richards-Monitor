SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             round((e.cluster_wait_time - NVL(b.cluster_wait_time, 0)) /1000000,1) "CWT (s)"
      ,      round(100 * (e.cluster_wait_time - NVL(b.cluster_wait_time, 0)) / (e.elapsed_time - NVL(b.elapsed_time, 0)),1) "CWT % of Elapsd Time"
      ,      round((e.elapsed_time - NVL(b.elapsed_time, 0)) /1000000,1) "Elapsed Time (s)"
      ,      round((e.cpu_time - NVL(b.cpu_time, 0)) /1000000,1) "CPU Time (s)"
      ,      (e.executions - NVL(b.executions, 0)) "Executions"
      ,      substr(st.sql_text,80) "SQL Text"
      ,      e.old_hash_value "Top SQL Hash Value"
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
        AND e.cluster_wait_time > NVL(b.cluster_wait_time, 0) 
      ORDER BY (e.cluster_wait_time - NVL(b.cluster_wait_time, 0)) desc
      ,        e.old_hash_value
      ,        st.piece ) 
WHERE rownum < 41 
