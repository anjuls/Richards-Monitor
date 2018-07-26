SELECT owner 
,      object_type 
,      count ( * ) 
FROM dba_objects 
WHERE status = 'INVALID' 
GROUP BY owner , object_type 
ORDER BY 1 , 2 , 3 
/