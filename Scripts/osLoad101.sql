SELECT LOWER(stat_name)
,      VALUE
FROM gv$osstat o 
WHERE o.inst_id = ? 
  AND o.stat_name IN ('USER_TICKS', 'SYS_TICKS', 'IDLE_TICKS', 'IO_WAIT_TICKS', 'BUSY_TICKS')
/