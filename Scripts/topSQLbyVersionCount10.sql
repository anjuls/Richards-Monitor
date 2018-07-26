SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             e.version_count "Version Count"
      ,      e.executions - NVL(b.executions, 0) "Executions"
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
        AND st.piece = 0 
        AND e.executions > NVL(b.executions, 0) 
      ORDER BY e.version_count desc
      ,        e.old_hash_value
      ,        st.piece ) 
WHERE rownum < 41 
