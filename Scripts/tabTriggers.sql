SELECT * 
FROM DBA_TRIGGERS
WHERE table_owner = ?
  AND table_name = ?
/