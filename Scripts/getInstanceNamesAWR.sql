SELECT d.dbid
,      d.instance_number
,      d.instance_name
,      to_char(s.begin_interval_time,'dd-mon-yyyy hh24:mi:ss')
FROM dba_hist_database_instance d
,    dba_hist_snapshot s 
WHERE d.dbid = s.dbid 
  AND d.instance_number = s.instance_number 
  AND s.begin_interval_time = (SELECT MAX(begin_interval_time) 
                               FROM dba_hist_snapshot h 
                               WHERE h.dbid = d.dbid 
                                 AND h.instance_number = d.instance_number ) 
  AND d.startup_time = (SELECT MAX(startup_time) 
                        FROM dba_hist_database_instance 
                        WHERE dbid = d.dbid 
                          AND instance_number = d.instance_number ) 
ORDER BY d.instance_number