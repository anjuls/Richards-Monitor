SELECT * 
FROM (SELECT /*+ ordered use_nl (b st) */ LPAD(TO_CHAR((e.buffer_gets - NVL(b.buffer_gets, 0)), '99,999,999,999'), 15) "buffer gets"
      ,      LPAD(TO_CHAR((e.executions - NVL(b.executions, 0)), '999,999,999'), 12) "executions"
      ,      LPAD((TO_CHAR(DECODE(e.executions - NVL(b.executions, 0), 0, TO_NUMBER(null), (e.buffer_gets - NVL(b.buffer_gets, 0)) / (e.executions - NVL(b.executions, 0))), '999,999,990.0')), 14) "buffer gets per execution"
      ,      LPAD((TO_CHAR(100* (e.buffer_gets - NVL(b.buffer_gets, 0)) /?, '990.0')), 6) "% of total gets"
      ,      LPAD(NVL(TO_CHAR((e.cpu_time - NVL(b.cpu_time, 0)) /1000000, '999990.00'), ' '), 8) "cpu time (s)"
      ,      LPAD(NVL(TO_CHAR((e.elapsed_time - NVL(b.elapsed_time, 0)) /1000000, '999990.00'), ' '), 9) "elapsed time (s)"
      ,      e.hash_value "top sql hash value"
      ,      st.sql_text 
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
