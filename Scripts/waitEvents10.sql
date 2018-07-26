SELECT s.sid
,      s.username
,      s.event
,      s.p1
,      s.p2
,      s.p3
,      s.wait_time
,      s.seconds_in_wait
,      s.state 
FROM gv$session s 
WHERE wait_class != 'Idle'
  ORDER BY s.seconds_in_wait desc