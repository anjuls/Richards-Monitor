SELECT sql_id 
FROM gv$session 
WHERE sid = ? 
  AND inst_id = ?
/