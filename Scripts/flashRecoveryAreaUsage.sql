SELECT file_type
,      percent_space_used
,      percent_space_reclaimable
,      number_of_files 
FROM v$flashback_recovery_area_usage fl 
/