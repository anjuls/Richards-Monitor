SELECT LOWER(stat_name)
,      VALUE
FROM gv$osstat o 
WHERE o.inst_id = ? 
  AND o.stat_name IN ('USER_TIME', 'SYS_TIME', 'IDLE_TIME', 'IO_WAIT_TIME', 'BUSY_TIME', 'LOAD')
/