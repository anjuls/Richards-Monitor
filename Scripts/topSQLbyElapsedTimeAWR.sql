SELECT * 
FROM (SELECT ROUND(NVL((sqt.elap /1000000), TO_NUMBER(null))) "Elapsed Time (s)"
      ,      ROUND(NVL((sqt.cput /1000000), TO_NUMBER(null))) "CPU Time (s)"
      ,      sqt.exec "Executions"
      ,      ROUND(DECODE(sqt.exec, 0, TO_NUMBER(null), (sqt.elap / sqt.exec / 1000000)),1) "Elapsed per Exec (s)"
      ,      ROUND((100 * (sqt.elap / ?)),1) "% Total DB Time"
      ,      REPLACE(TRIM(SUBSTR(NVL(st.sql_text, TO_CLOB('** SQL Text Not Available **')),1,100)),chr(10),' ') "SQL Text"
      ,      sqt.sql_id
      FROM (SELECT sql_id
            ,      MAX(module) module
            ,      SUM(elapsed_time_delta) elap
            ,      SUM(cpu_time_delta) cput
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
      ORDER BY NVL(sqt.elap, -1) desc
      ,        sqt.sql_id ) 
WHERE rownum < 41
