SELECT * 
FROM (SELECT /*+ ordered use_nl (b st) */ 
             e.buffer_gets - NVL(b.buffer_gets, 0) "buffer gets"
      ,      e.executions - NVL(b.executions, 0)) "executions"
      ,      ROUND(DECODE(e.executions - NVL(b.executions, 0), 0, TO_NUMBER(null), (e.buffer_gets - NVL(b.buffer_gets, 0)) / (e.executions - NVL(b.executions, 0))), 2) "buffer gets per execution"
      ,      ROUND(100* (e.buffer_gets - NVL(b.buffer_gets, 0)) /?, '990.0')), 2) "% of total gets"
      ,      TRIM(st.sql_text)
      ,      e.hash_value "top sql hash value" 
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
      ORDER BY (e.buffer_gets - NVL(b.buffer_gets, 0)) desc
      ,        e.hash_value
      ,        st.piece ) 
WHERE rownum <= 40;