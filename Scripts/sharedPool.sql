SELECT name
,      ROUND(bytes /1024 /1024, 1) 
FROM gv$sgastat 
WHERE pool = 'shared pool'
  AND inst_id = ? 
  AND name IN ('library cache', 'miscellaneous', 'sql area', 'free memory') 
UNION ALL 
SELECT 'open cursors'
 ,      COUNT(*) 
FROM gv$open_cursor 
WHERE inst_id = ? 
UNION ALL 
SELECT 'free memory (reserved pool)'
 ,      free_space /1024 /1024 
FROM gv$shared_pool_reserved 
WHERE inst_id = ? 
UNION ALL 
SELECT 'sessions'
 ,      COUNT(*) 
FROM gv$session 
WHERE inst_id = ? 
UNION ALL 
SELECT 'reloads'
 ,      SUM(reloads) 
FROM gv$librarycache 
WHERE inst_id = ? 
UNION ALL 
SELECT 'invalidations'
 ,      SUM(invalidations) 
FROM gv$librarycache 
WHERE inst_id = ?
ORDER BY 1