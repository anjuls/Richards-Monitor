SELECT p.name "Parameter Name"
,      p.value "Parameter Value" 
FROM gv$parameter p 
WHERE p.name like LOWER(?) 
  AND p.value is not null 
ORDER BY p.name 
/