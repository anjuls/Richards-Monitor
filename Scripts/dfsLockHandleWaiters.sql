SELECT gses.inst_id
,      glock.pid
,      gses.sid
,      gses.program
,      gses.username
,      gses.seconds_in_wait
,      gses.p2
,      gses.p3
,      (glock2.inst_id || ','|| glock2.pid) "BLOCKED BY (INST_ID,PID)" 
FROM gv$session gses
,    gv$dlm_locks glock
,    gv$process gproc
,    gv$dlm_locks glock2 
WHERE gproc.inst_id = gses.inst_id 
  AND gproc.addr = gses.paddr 
  AND gproc.spid = glock.pid 
  AND gproc.inst_id = glock.inst_id 
  AND gses.event = 'DFS lock handle'
  AND glock.resource_name1 like '[0x'|| gses.p2 || '][0x'|| gses.p3 || '],[CI]%'
  AND glock.blocked = 1 
  AND glock2.blocker = 1 
  AND glock.resource_name1 = glock2.resource_name1 (+) 
ORDER BY "BLOCKED BY (INST_ID,PID)"
,        inst_id 
/