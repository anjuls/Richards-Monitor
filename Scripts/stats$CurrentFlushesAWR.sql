SELECT (e.flush1 +e.flush10 +e.flush100 +e.flush1000 +e.flush10000) - (b.flush1 +b.flush10 +b.flush100 +b.flush1000 +b.flush10000) 
FROM dba_hist_current_block_server b
,    dba_hist_current_block_server e 
WHERE b.snap_id = ?
  AND b.dbid = ? 
  AND b.instance_number = ?
  AND e.snap_id = ?
  AND e.dbid = ?
  AND e.instance_number = ?
/
