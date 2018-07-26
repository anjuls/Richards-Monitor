SELECT *
FROM (SELECT round(sqt.cput /1000000,1)  "CPU Time (s)"
      ,      round(sqt.elap /1000000,1)  "Elapsed Time (s)"
      ,      sqt.exec "Executions"
      ,      DECODE(sqt.exec, 0, TO_NUMBER(null), round((sqt.cput / sqt.exec / 1000000),1))  "CPU per Execution (s)"
      ,      round((100 * (sqt.elap / ?)),1) "% Total DB Time"
      ,      REPLACE(substr(st.sql_text,1,80),chr(10),' ') "SQL Text"
      ,      sqt.sql_id "Top SQL SQL_ID"
      FROM (SELECT sql_id
            ,      MAX(module) module
            ,      SUM(cpu_time_delta) cput
            ,      SUM(elapsed_time_delta) elap
            ,      SUM(executions_delta) exec
            FROM dba_hist_sqlstat
            WHERE dbid = ?
              AND instance_number = ?
              AND ? < snap_id
              AND snap_id <= ?
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st
      WHERE st.sql_id (+) = sqt.sql_id
        AND st.dbid (+) = ?
      ORDER BY NVL(sqt.cput, -1) desc
      ,        sqt.sql_id )
WHERE rownum < 41
/