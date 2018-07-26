SELECT * 
FROM (SELECT sql_id
      ,      SUM(time_waited) /1000000 "Time Waited" 
      FROM dba_hist_active_sess_history ash 
      WHERE ash.snap_id > ? 
        AND ash.snap_id <= ? 
        AND ash.dbid = ? 
        AND ash.instance_number = ? 
        AND ash.event = ? 
      GROUP BY sql_id 
      ORDER BY 2 desc ) 
WHERE rownum < 21 
/
