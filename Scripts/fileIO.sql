SELECT file_name 
,      sum( phyblkrd ) 
,      sum( phywrts ) 
FROM v$filestat vf 
,    dba_data_files ts 
WHERE vf.file# = ts.file_id 
GROUP BY file_name 
union all
SELECT name file_name
,      sum( phyblkrd ) 
,      sum( phywrts ) 
FROM v$tempstat vf 
,    v$tempfile tf
WHERE vf.file# = tf.file#
GROUP BY name
/