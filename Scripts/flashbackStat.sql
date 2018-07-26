SELECT begin_time
,      end_time
,      flashback_data
,      db_data
,      redo_data
,      estimated_flashback_size 
FROM gv$flashback_database_stat fl 
/