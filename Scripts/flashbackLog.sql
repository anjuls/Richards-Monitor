SELECT oldest_flashback_scn
,      oldest_flashback_time
,      retention_target
,      flashback_size
,      estimated_flashback_size 
FROM gv$flashback_database_log fl
/