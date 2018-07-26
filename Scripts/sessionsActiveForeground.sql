SELECT p.spid spid
,      s.sid sid
,      s.username username
,      s.osuser
,      s.serial#
,      s.logon_time "Logon Time"
,      s.server
,      regexp_replace (LTRIM(CAST(NUMTODSINTERVAL(s.last_call_et, 'SECOND') AS interval day (2) to second (0)), ' +'), ' ', '.' ) "Active For DD.HH24:MI:SS" 
,      ss.name "Shared Server"
,      d.name dispatcher
,      s.program
,      s.terminal
,      s.machine
FROM gv$session s
,    gv$process p
,    gv$circuit c
,    gv$dispatcher d
,    gv$shared_server ss
WHERE s.status = 'ACTIVE'
  AND s.type = 'USER'
  AND s.paddr = p.addr (+) 
  AND s.saddr = c.saddr (+)
  AND c.circuit = ss.circuit (+)
  AND c.dispatcher = d.paddr (+)
  AND s.inst_id = p.inst_id (+) 
  AND s.inst_id = c.inst_id (+)
  AND c.inst_id = ss.inst_id (+)
  AND c.inst_id = d.inst_id (+)
ORDER BY logon_time
/
