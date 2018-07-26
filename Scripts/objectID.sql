SELECT object_id
FROM dba_objects
WHERE owner = ?
  AND object_name = ?
/
