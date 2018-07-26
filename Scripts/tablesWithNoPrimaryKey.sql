SELECT owner "Owner" 
,      table_name "Table_Name" 
FROM sys.all_tables 
WHERE owner = user 
  AND temporary = 'N' 
  AND substr ( table_name , 1 , 4 ) != 'BIN$' 
  AND substr ( table_name , 1 , 3 ) != 'DR$' 
  AND ( owner , table_name ) not IN ( SELECT owner 
,      table_name 
                                      FROM sys.all_constraints 
                                      WHERE constraint_type = 'P' ) 
ORDER BY owner , table_name 
/