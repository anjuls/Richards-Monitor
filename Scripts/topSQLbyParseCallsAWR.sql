SELECT * 
FROM (SELECT sqt.prsc "Parse Calls"
      ,      sqt.exec "Executions"
      ,      ROUND(DECODE(?, 0, 0, 100 * sqt.prsc / ?),2) "% of Total Parses"
      ,      sqt.sql_id "Top SQL SQL_Id"
      ,      REPLACE(SUBSTR(st.sql_text,0,80),chr(10),' ') "SQL Text"
      FROM (SELECT sql_id
            ,      MAX(module) module
            ,      SUM(buffer_gets_delta) bget
            ,      SUM(disk_reads_delta) dskr
            ,      SUM(executions_delta) exec
            ,      SUM(parse_calls_delta) prsc 
            FROM dba_hist_sqlstat 
            WHERE dbid = ?
              AND instance_number = ?
              AND ? < snap_id 
              AND snap_id <= ?
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st 
      WHERE st.sql_id (+) = sqt.sql_id 
        AND st.dbid (+) = ? 
      ORDER BY NVL(sqt.prsc, -1) desc
      ,        sqt.sql_id ) 
WHERE rownum < 41
