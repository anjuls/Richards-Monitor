SELECT owner 
,      object_type 
,      object_name
FROM dba_objects 
WHERE status = 'INVALID' 
ORDER BY 1 , 2 , 3 
/