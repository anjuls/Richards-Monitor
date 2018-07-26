select s.sid
,      sum(round((p.value * t.used_ublk)/1024,2)) "Used kb"
from   v$transaction t
,      v$session s
,      v$rollname n
,      v$parameter p
where   xidusn = n.usn
and     t.ses_addr = s.saddr (+)
and     p.name = 'db_block_size'
group by s.sid
order by 1
/