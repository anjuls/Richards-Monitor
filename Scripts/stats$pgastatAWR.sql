SELECT name
,      ROUND(VALUE /1024 /1024, 1) 
FROM dba_hist_pgastat 
WHERE name IN ('maximum PGA allocated', 'PGA memory freed back to OS', 'total PGA allocated', 'total PGA inuse') 
  AND snap_id = ? 
  AND dbid = ? 
  AND instance_number = ? 
UNION ALL 
SELECT name
,      VALUE
FROM dba_hist_pgastat 
WHERE name IN ('over allocation count', 'cache hit percentage') 
  AND snap_id = ? 
  AND dbid = ? 
  AND instance_number = ? 
