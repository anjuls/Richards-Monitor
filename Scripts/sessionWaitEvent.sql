SELECT sw.event
,      sw.p1text
,      sw.p1
,      sw.p2text
,      sw.p2
,      sw.p3text
,      sw.p3
,      sw.wait_time
,      sw.seconds_in_wait
,      sw.state 
FROM gv$session_wait sw
,    gv$session s
,    gv$process p 
WHERE s.sid = ? 
  AND s.inst_id = ? 
  AND sw.sid = s.sid 
  AND sw.inst_id = s.inst_id  
  AND s.paddr = p.addr 
  AND s.inst_id = p.inst_id 
/