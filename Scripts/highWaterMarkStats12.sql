SELECT hwm.highwater
,      hwm.last_value
,      hwm.description 
FROM cdb_high_water_mark_statistics hwm
,    v$containers c
WHERE hwm.con_id = c.con_id
ORDER BY c.con_id
/