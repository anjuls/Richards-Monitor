SELECT * 
FROM (SELECT sqt.exec "Executions"
      ,      sqt.rowp "Rows Processed"
      ,      TRUNC(sqt.rowp / sqt.exec) "Rows per Execution"
      ,      TRUNC(sqt.cput / sqt.exec / 1000000) "CPU per Execution (s)"
      ,      TRUNC(sqt.elap / sqt.exec / 1000000) "Elapsed per Execution (s)"
      ,      REPLACE(TRIM(TO_CHAR(SUBSTR(st.sql_text, 1, 50))),chr(10),' ') "SQL Text"
      ,      sqt.sql_id "Top SQL SQL_Id" 
      FROM (SELECT sql_id
            ,      SUM(executions_delta) exec
            ,      SUM(rows_processed_delta) rowp
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
        AND sqt.exec > 0 
      ORDER BY NVL(sqt.exec, -1) desc
      ,        sqt.sql_id ) 
WHERE rownum <= 40;