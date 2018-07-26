SELECT distinct starttime 
FROM (SELECT TO_CHAR(startup_time, 'dd-mon-yyyy hh24:mi:ss' ) starttime 
      FROM dba_hist_snapshot 
      WHERE snap_id BETWEEN ? AND ? 
        AND dbid = ? 
        AND instance_number = ? ) 
WHERE rownum < 11 
ORDER BY TO_DATE(starttime, 'dd-mon-yyyy hh24:mi:ss') desc