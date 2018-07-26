SELECT * 
FROM (SELECT /*+ ORDERED USE_NL(b st) */ 
             e.version_count "Version Count"
      ,      sqt.exec "Executions"
      ,      e.sql_id "Top SQL SQL_ID"
      ,      REPLACE(SUBSTR(st.sql_text, 1, 80),chr(10),' ') "SQL Text" 
      FROM dba_hist_sqlstat e
      ,    (SELECT sql_id
            ,      SUM(executions_delta) exec 
            FROM dba_hist_sqlstat 
            WHERE dbid = ? 
              AND instance_number = ? 
              AND ? < snap_id 
              AND snap_id <= ? 
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st 
      WHERE sqt.sql_id (+) = e.sql_id 
        AND e.snap_id = ? 
        AND e.dbid = ? 
        AND e.instance_number = ? 
        AND st.sql_id (+) = e.sql_id 
        AND st.dbid (+) = ? 
      ORDER BY NVL(e.version_count, -1) desc
      ,        e.sql_id ) 
WHERE rownum < 41 
