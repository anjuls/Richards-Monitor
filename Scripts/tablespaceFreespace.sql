SELECT t.tablespace_name "Tablespace Name"
,      t.status
,      TO_CHAR(ROUND(SUM(fs.bytes) /1024 /1024, 2), '999,999,990.99' ) "Free mb"
,      TO_CHAR(SUM(fs.bytes), '999,999,999,999,999' ) "Free bytes"
,      TO_CHAR(ROUND(MAX(fs.bytes) /1024 /1024, 2), '999,999,990.99' ) "Max ext Size mb" 
FROM dba_data_files f
,    dba_tablespaces t
,    dba_free_space fs 
WHERE fs.file_id = f.file_id 
  AND t.tablespace_name = f.tablespace_name 
GROUP BY t.tablespace_name
,        t.status 
ORDER BY 1