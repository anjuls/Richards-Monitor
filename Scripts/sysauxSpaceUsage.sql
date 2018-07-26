SELECT occupant_name
,      occupant_desc
,      schema_name
,      space_usage_kbytes 
FROM v$sysaux_occupants 
ORDER BY 4 desc 
/