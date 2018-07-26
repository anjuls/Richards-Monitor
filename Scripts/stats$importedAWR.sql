SELECT DISTINCT dbid
,      instance_name
,      instance_number 
FROM dba_hist_database_instance
WHERE dbid not IN (SELECT dbid 
                   FROM v$database ) 
ORDER BY 1
,        3
/
