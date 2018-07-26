SELECT 'Blockers' "Role"
,      gses.inst_id
,      glock.pid
,      gses.sid
,      gses.program
,      gses.username
,      gsw.seconds_in_wait
,      gsw.event
,      gsw.p1
,      gsw.p2
,      gsw.p3
,      'unknown in this release' "BLOCKED BY (INST_ID,PID)"
,      glock.resource_name1 
FROM gv$session gses
,    gv$dlm_locks glock
,    gv$process gproc 
,    gv$session_wait gsw
WHERE gproc.inst_id = gses.inst_id 
  AND gproc.addr = gses.paddr 
  AND gproc.spid = glock.pid 
  AND gproc.inst_id = glock.inst_id 
  AND gses.sid = gsw.sid
  AND gses.inst_id = gsw.inst_id
  AND glock.blocker = 1 
  AND glock.resource_name1 like '%[CI]%'
union all
SELECT 'Waiter' "Role"
,      gses.inst_id
,      glock.pid
,      gses.sid
,      gses.program
,      gses.username
,      gsw.seconds_in_wait
,      gsw.event
,      gsw.p1
,      gsw.p2
,      gsw.p3
,      (glock2.inst_id || ','|| glock2.pid) "BLOCKED BY (INST_ID,PID)" 
,      ' '
FROM gv$session gses
,    gv$dlm_locks glock
,    gv$process gproc
,    gv$dlm_locks glock2 
,    gv$session_wait gsw
WHERE gproc.inst_id = gses.inst_id 
  AND gproc.addr = gses.paddr 
  AND gproc.spid = glock.pid 
  AND gproc.inst_id = glock.inst_id 
  AND gses.sid = gsw.sid
  AND gses.inst_id = gsw.inst_id
  AND gsw.event = 'DFS lock handle'
  AND glock.resource_name1 like '[0x'|| gsw.p2 || '][0x'|| gsw.p3 || '],[CI]%'
  AND glock.blocked = 1 
  AND glock2.blocker = 1 
  AND glock.resource_name1 = glock2.resource_name1 (+) 
ORDER BY "BLOCKED BY (INST_ID,PID)"
,        inst_id 
