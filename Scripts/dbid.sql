SELECT dbid
,      instance_name
,      to_char(sysdate,'dd-mon-yyyy_hh24miss')
FROM dba_hist_database_instance o 
WHERE instance_number = ? 
  AND startup_time = (SELECT MAX(startup_time) 
                      FROM dba_hist_database_instance i 
                      WHERE i.instance_number = o.instance_number ) 
/
