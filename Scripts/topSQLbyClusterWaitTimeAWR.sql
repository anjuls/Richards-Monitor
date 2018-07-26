SELECT *
FROM (SELECT round(sqt.clwait / 1000000,1) "CWT (s)"
      ,      round(DECODE(sqt.elap, 0, sqt.clwait, 100 * sqt.clwait / sqt.elap),1) "CWT % of Elapsd Time"
      ,      round(sqt.elap / 1000000,1) "Elapsed Time (s)"
      ,      round(sqt.cput / 1000000,1) "CPU Time (s)"
      ,      sqt.exec "Executions"
      ,      REPLACE(substr(st.sql_text,1,80),chr(10),' ') "SQL Text"
      ,      sqt.sql_id "Top SQL SQL_ID"
      FROM (SELECT sql_id
            ,      MAX(module) module
            ,      SUM(executions_delta) exec
            ,      SUM(cpu_time_delta) cput
            ,      SUM(elapsed_time_delta) elap
            ,      SUM(clwait_delta) clwait
            FROM dba_hist_sqlstat
            WHERE dbid = ?
              AND instance_number = ?
              AND ? < snap_id
              AND snap_id <= ?
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st
      WHERE st.sql_id (+) = sqt.sql_id
        AND st.dbid (+) = ?
      ORDER BY NVL(sqt.clwait, -1) desc
      ,        sqt.sql_id )
WHERE rownum < 41

