select d.instance_name
,      b.sid
,      a.PID
,      a.RESOURCE_NAME
,      decode(a.BLOCKED,1, 'BLOCKED', decode(a.BLOCKER, 1, 'BLOCKER','UNKONWN')) "Status"
,      b.program
from   gv$dlm_locks a
,      gv$session b
,      gv$process c
,      gv$instance d
where c.addr = b.paddr
and c.spid = a.pid
and a.inst_id = b.inst_id
and b.inst_id = c.inst_id
and c.inst_id = d.inst_id
/
