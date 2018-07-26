SELECT owner 
,      segment_name 
,      partition_name 
,      segment_type 
,      tablespace_name 
,      extents 
,      bytes/1024/1024 "Mb" 
FROM dba_segments 
WHERE owner not IN ( 'SYS' , 'SYSTEM' ) 
  AND extents > 10000
/