SELECT * 
FROM (SELECT sqt.dskr "disk reads"
      ,      sqt.exec "executions"
      ,      DECODE(interconnectbytes, 0, 0, DECODE(eligbytes, 0, 0, ROUND(100 - 100 * interconnectbytes /eligbytes, 2))) "IO SAVED % (SMART SCAN)"
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
            ,      SUM(io_offload_elig_bytes_delta) eligbytes
            ,      SUM(io_interconnect_bytes_delta) interconnectbytes
            FROM dba_hist_sqlstat 
            WHERE dbid = ? 
              AND instance_number = ? 
              AND ? < snap_id 
              AND snap_id <= ? 
            GROUP BY sql_id ) sqt
      ,    dba_hist_sqltext st 
      WHERE st.sql_id (+) = sqt.sql_id 
        AND st.dbid (+) = ? 
        AND eligbytes > 0 
      ORDER BY 3 desc
      ,        sqt.sql_id ) 
WHERE rownum <= 40
/
