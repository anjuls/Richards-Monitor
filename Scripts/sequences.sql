SELECT sequence_owner
,      sequence_name
,      min_value
,      max_value
,      increment_by
,      cycle_flag
,      order_flag
,      cache_size
,      last_number 
FROM dba_sequences 
where sequence_owner not in ('SYS','SYSTEM')
ORDER BY 1
,        2
/