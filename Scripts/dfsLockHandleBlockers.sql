SELECT gses.inst_id
,      glock.pid
,      gses.sid
,      gses.program
,      gses.username
,      gses.event
,      gses.p1
,      gses.p2
,      gses.p3
,      gses.seconds_in_wait
,      glock.resource_name1 
FROM gv$session gses
,    gv$dlm_locks glock
,    gv$process gproc 
WHERE gproc.inst_id = gses.inst_id 
  AND gproc.addr = gses.paddr 
  AND gproc.spid = glock.pid 
  AND gproc.inst_id = glock.inst_id 
  AND glock.blocker = 1 
  AND glock.resource_name1 like '%[CI]%'
/