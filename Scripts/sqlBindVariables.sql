SELECT name
,      last_captured
,      value_string VALUE
,      position
,      dup_position
,      datatype_string "data type"
,      was_captured
FROM v$sql_bind_capture sbc
WHERE sql_id = ?
AND child_number = ?
ORDER BY position
/