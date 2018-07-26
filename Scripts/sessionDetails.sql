select p.spid spid
,      s.sid
,      s.username
,      to_char(s.serial#) "Serial#"
,      to_char(logon_time,'dd-mon-yyyy hh24:mi:ss') "Logon Time"
,      s.server
,      ss.name "Shared Server"
,      d.name Dispatcher
,      s.status
,      s.program
,      s.terminal
,      s.machine
,      s.sql_hash_value
,      s.sql_address
,      s.prev_hash_value
,      s.prev_sql_addr
from   gv$session s
,      gv$process p
,      gv$circuit c
,      gv$dispatcher d
,      gv$shared_server ss
where  s.sid = ?
and    s.inst_id = ?
and    p.addr = s.paddr
and    p.inst_id = s.inst_id
and    s.saddr = c.saddr (+)
and    s.inst_id = c.inst_id (+)
and    c.circuit = ss.circuit (+)
and    c.inst_id = ss.inst_id (+)
and    c.dispatcher = d.paddr (+)
and    c.inst_id = d.inst_id (+)
/