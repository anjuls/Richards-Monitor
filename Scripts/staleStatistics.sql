SELECT * 
FROM dba_tab_statistics 
WHERE owner like upper(?)
AND stale_stats = 'YES'
/