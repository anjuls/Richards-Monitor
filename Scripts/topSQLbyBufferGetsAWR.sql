SELECT * 
FROM (SELECT sqt.bget "buffer gets"
      ,      sqt.exec "executions"
      ,      TRUNC(DECODE(sqt.exec, 0, TO_NUMBER(null), (sqt.bget / sqt.exec))) "gets per execution"
      ,      TRUNC((100 * sqt.bget) / ?) "% of total gets"
      ,      TRUNC(NVL((sqt.cput/1000000), TO_NUMBER(null))) "CPU Times (s)"
      ,      TRUNC(NVL((sqt.elap/1000000), TO_NUMBER(null))) "elapsed time (s)"
      ,      sqt.sql_id "Top SQL SQL_Id"
      ,      REPLACE(TRIM(TO_CHAR(SUBSTR(st.sql_text, 1, 50))),chr(10),' ') "SQL Text" 
      FROM (SELECT sql_id
            ,      SUM(buffer_gets_delta) bget
            ,      SUM(executions_delta) exec
            ,      SUM(cpu_time_delta) cput
            ,      SUM(elapsed_time_delta) elap 
            FROM dba_hist_sqlstat 
            WHERE dbid = ? 
              AND instance_number = ? 
              AND ? < snap_id 
              AND snap_id <= ? 
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st 
      WHERE st.sql_id (+) = sqt.sql_id 
        AND st.dbid (+) = ? 
      ORDER BY NVL(sqt.bget, -1) desc
      ,        sqt.sql_id ) 
WHERE rownum <= 40;