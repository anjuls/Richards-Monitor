select s.sid
from gv$session s
,    gv$process p
where p.spid = ?
and s.paddr = p.addr
and s.inst_id = p.inst_id
and p.inst_id = ?
/