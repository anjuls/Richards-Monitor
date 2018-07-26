SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             (e.parse_calls - NVL(b.parse_calls, 0)) "Parse Calls"
      ,      (e.executions - NVL(b.executions, 0)) "Executions"
      ,      ROUND(100 * (e.parse_calls - NVL(b.parse_calls, 0)) /?,2) "% of Total Parses"
      ,      e.old_hash_value "Top SQL Hash Value"
      ,      substr(st.sql_text,1,80) "SQL Text"
      FROM stats$sql_summary e
      ,    stats$sql_summary b
      ,    stats$sqltext st 
      WHERE b.snap_id (+) = ? 
        AND b.dbid (+) = e.dbid 
        AND b.instance_number (+) = e.instance_number 
        AND b.old_hash_value (+) = e.old_hash_value 
        AND b.address (+) = e.address 
        AND b.text_subset (+) = e.text_subset 
        AND e.snap_id =?
        AND e.dbid = ?
        AND e.instance_number = ?
        AND e.old_hash_value = st.old_hash_value 
        AND e.text_subset = st.text_subset 
        AND st.piece = 0 
      ORDER BY (e.parse_calls - NVL(b.parse_calls, 0)) desc
      ,        e.old_hash_value
      ,        st.piece ) 
WHERE rownum < 41 
