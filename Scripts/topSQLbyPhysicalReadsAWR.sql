SELECT * 
FROM (SELECT sqt.dskr "disk reads"
      ,      sqt.exec "executions"
      ,      TRUNC(DECODE(sqt.exec, 0, TO_NUMBER(null), (sqt.dskr / sqt.exec))) "disk reads per execution"
      ,      TRUNC((100 * sqt.dskr) / ?) "% total disk reads"
      ,      TRUNC(NVL((sqt.cput / 1000000), TO_NUMBER(null))) "cpu time (s)"
      ,      TRUNC(NVL((sqt.elap / 1000000), TO_NUMBER(null))) "elapsed time (s)"
      ,      sqt.sql_id "TOP SQL SQL_ID"
      ,      REPLACE(TRIM(TO_CHAR(SUBSTR(st.sql_text, 1, 50))),chr(10),' ') "SQL Text" 
      FROM (SELECT sql_id
            ,      SUM(disk_reads_delta) dskr
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
        AND ? > 0 
      ORDER BY NVL(sqt.dskr, -1) desc
      ,        sqt.sql_id ) 
WHERE rownum <= 40
/