SELECT * 
FROM dba_sqlset_statements 
WHERE sqlset_owner = ?
  AND sqlset_name = ? 
/