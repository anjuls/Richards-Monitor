SELECT s.sid
,      start_time "Start Time"
,      s.username
,      t.xidusn
,      t.used_urec
,      t.used_ublk
,      ROUND((p.value * t.used_ublk) /1024, 2) "Used kb"
,      ROUND((p.value * t.used_ublk) /1024 /1024, 2) "Used mb" 
FROM gv$transaction t
,    gv$session s
,    gv$parameter p 
WHERE t.ses_addr = s.saddr (+) 
  AND p.name = 'db_block_size'
  AND t.inst_id = s.inst_id (+)  
  AND t.inst_id = p.inst_id 
ORDER BY start_time