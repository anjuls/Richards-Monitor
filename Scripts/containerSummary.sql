SELECT c.con_id
,      c.inst_id
,      c.name
,      c.open_mode
,      c.restricted
,      c.open_time
,      total_size
,      block_size
,      recovery_status
FROM gv$containers c
/