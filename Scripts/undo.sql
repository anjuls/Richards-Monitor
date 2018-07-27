SELECT s.sid
,      start_time "Start Time"
,      s.username
,      n.name "Segment name"
,      t.used_urec
,      t.used_ublk
,      ROUND((p.value * t.used_ublk) /1024, 2) "Used kb"
,      ROUND((p.value * t.used_ublk) /1024 /1024, 2) "Used mb" 
FROM v$transaction t
,    v$session s
,    v$rollname n
,    v$parameter p 
WHERE s.sid like ? 
  AND t.xidusn = n.usn 
  AND t.ses_addr = s.saddr (+) 
  AND p.name = 'db_block_size'
ORDER BY start_time
/