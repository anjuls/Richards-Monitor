SELECT tablespace_name tablespace
,      file_name
,      TO_CHAR(bytes /1024 /1024, '999,999,999,999' ) "size mb"
,      status
,      autoextensible autoextend
,      increment_by 
FROM dba_data_files 
ORDER BY tablespace_name 
