SELECT distinct plan_hash_value 
FROM dba_hist_sqlstat 
WHERE sql_id = ?
/
