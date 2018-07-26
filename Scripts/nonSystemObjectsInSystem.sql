SELECT owner 
,      segment_name 
,      segment_type 
FROM dba_segments 
WHERE owner not IN ( 'SYS' , 'SYSTEM' , 'OUTLN' ) 
  AND tablespace_name = 'SYSTEM'
/