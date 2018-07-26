SELECT e.flushes - b.flushes  
FROM dba_hist_cr_block_server b
,    dba_hist_cr_block_server e 
WHERE b.snap_id = ?
  AND b.dbid = ?
  AND b.instance_number = ?
  AND e.snap_id = ?
  AND e.dbid = ?
  AND e.instance_number = ?
/