SELECT VALUE
FROM dba_hist_parameter 
WHERE parameter_name = ? 
  AND snap_id = ? 
  AND dbid = ?
  AND instance_number = ? 
/