SELECT da.apply_name
,      da.status
,      NVL(SUM(dae.message_count), 0) errors 
FROM dba_apply da
,    dba_apply_error dae 
WHERE da.apply_name = dae.apply_name (+) 
GROUP BY da.apply_name
,        da.status 
ORDER BY da.apply_name 
/