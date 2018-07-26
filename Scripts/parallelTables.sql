SELECT owner
,      table_name
,      degree
,      instances 
FROM dba_tables 
WHERE (    degree is not null 
       AND TRIM(degree) != 'DEFAULT'
       AND TRIM(degree) NOT IN ('0', '1')) 
   OR (    instances is not null 
       AND TRIM(instances) != 'DEFAULT'
       AND TRIM(instances) NOT IN ('0', '1'))
/