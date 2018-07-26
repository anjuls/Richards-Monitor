SELECT SUBSTR(f.tablespace_name, 1, 25) "Tablespace Name"
,      SUBSTR(f.file_name, 1, 50) "File Name"
,      f.autoextensible "Auto Ext"
,      f.increment_by incr
,      TO_CHAR(ROUND(f.bytes /1024 /1024, 2), '999,999,990.99' ) "File Size mb"
,      TO_CHAR(ROUND(SUM(fs.bytes) /1024 /1024, 2), '999,999,990.99' ) "Free mb"
,      TO_CHAR(SUM(fs.bytes), '999,999,999,999,999' ) "Free bytes"
,      TO_CHAR(ROUND(MAX(fs.bytes) /1024 /1024, 2), '999,999,990.99' ) "Max ext Size mb" 
FROM dba_data_files f
,    dba_free_space fs 
WHERE fs.file_id = f.file_id 
GROUP BY f.tablespace_name
,        f.autoextensible
,        f.increment_by
,        f.file_name
,        f.bytes 
ORDER BY 1 