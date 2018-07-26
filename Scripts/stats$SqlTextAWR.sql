SELECT sql_text
FROM dba_hist_sqltext 
WHERE sql_id = ? 
  AND dbid = ? 
