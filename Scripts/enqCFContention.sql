SELECT 'Blockers' session_role
,      l.inst_id
,      l.sid
,      l.lmode
,      l.request
,      s.program
,      p.pid
,      p.spid
,      s.username
,      s.terminal
,      s.module
,      s.action
,      s.event
,      s.wait_time
,      s.seconds_in_wait
,      s.state
FROM gv$lock l
,    gv$session s
,    gv$process p
WHERE l.sid = s.sid
  AND s.paddr = p.addr
  AND l.type = 'CF'
  AND l.lmode >= 5
  AND l.inst_id = s.inst_id
  AND s.inst_id = p.inst_id
UNION ALL
SELECT 'Waiter' session_role
,      l.inst_id
,      l.sid
,      l.lmode
,      l.request
,      s.program
,      p.pid
,      p.spid
,      s.username
,      s.terminal
,      s.module
,      s.action
,      s.event
,      s.wait_time
,      s.seconds_in_wait
,      s.state
FROM gv$lock l
,    gv$session s
,    gv$process p
WHERE l.sid = s.sid
  AND s.paddr = p.addr
  AND l.type = 'CF'
  AND l.request >= 5
  AND l.inst_id = s.inst_id
  AND s.inst_id = p.inst_id 
/
