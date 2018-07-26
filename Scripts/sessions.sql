SELECT p.spid spid
,      s.sid sid
,      s.username username
,      s.osuser
,      s.serial#
,      s.logon_time "Logon Time"
,      s.server
,      ss.name "Shared Server"
,      d.name dispatcher
,      s.status
,      s.type
,      s.program
,      s.terminal
,      s.machine
,      SUBSTR(DECODE((i.consistent_gets +i.block_gets), 0, 'None', (100 * (i.consistent_gets +i.block_gets -i.physical_reads) / (i.consistent_gets +i.block_gets))), 0, 5) "%HIT"
,      i.block_changes
,      i.consistent_changes 
FROM gv$session s
,    gv$process p
,    gv$circuit c
,    gv$dispatcher d
,    gv$shared_server ss
,    gv$sess_io i
WHERE s.paddr = p.addr (+) 
  AND s.saddr = c.saddr (+)
  AND c.circuit = ss.circuit (+)
  AND c.dispatcher = d.paddr (+)
  AND s.sid = i.sid (+) 
  AND s.inst_id = p.inst_id (+) 
  AND s.inst_id = c.inst_id (+)
  AND c.inst_id = ss.inst_id (+)
  AND c.inst_id = d.inst_id (+)
  AND s.inst_id = i.inst_id (+) 
ORDER BY logon_time
/