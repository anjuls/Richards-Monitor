SELECT tablespace_name 
,      sum( phyblkrd ) 
,      sum( phywrts ) 
FROM v$filestat vf 
,    dba_data_files ts 
WHERE vf.file# = ts.file_id 
GROUP BY tablespace_name 
union all
SELECT ts.name tablespace_name
,      sum( phyblkrd ) 
,      sum( phywrts ) 
FROM v$tempstat vf 
,    v$tempfile tf
,    v$tablespace ts
WHERE vf.file# = tf.file#
and   tf.ts# = ts.ts#
GROUP BY ts.name 
/