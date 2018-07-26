SELECT vts.name
,      nvl(round((dbahts.tablespace_size * dts.block_size)/1024/1024),0)
,      nvl(round((dbahts.tablespace_usedsize * dts.block_size)/1024/1024),0)
FROM dba_hist_tbspc_space_usage dbahts
,    v$tablespace vts 
,    dba_tablespaces dts
WHERE vts.name = ?
  AND vts.ts# = dbahts.tablespace_id (+)
  AND dbahts.snap_id (+)= ?
  AND vts.name = dts.tablespace_name
/
