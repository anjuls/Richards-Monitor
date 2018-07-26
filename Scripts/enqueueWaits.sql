SELECT sw.sid
,      s.username
,      CHR(BITAND(sw.p1, -16777216) /16777215) || CHR(BITAND(sw.p1, 16711680 /65535)) "Lock"
,      TO_CHAR(BITAND(sw.p1, 65535)) "Mode"
,      sw.wait_time
,      sw.seconds_in_wait
,      sw.state 
FROM gv$session_wait sw
,    gv$session s 
WHERE sw.event = 'enqueue'
  AND sw.sid = s.sid 
  AND sw.inst_id = s.inst_id 