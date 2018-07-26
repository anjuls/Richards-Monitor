select s.sid
,      substr(to_char(to_date(start_time,'MM/DD/YY HH24:MI:SS'),'DD-MON-YYYY HH24:MI'),1, 18) "Start Time"
,      s.username
,      n.name "Segment name"
,      sum(t.used_urec)
,      sum(t.used_ublk)
,      sum(round((p.value * t.used_ublk)/1024,2)) "Used kb"
,      sum(round((p.value * t.used_ublk)/1024/1024,2)) "Used mb"
from   gv$transaction t
,      gv$session s
,      v$rollname n
,      gv$parameter p
where   s.sid like ?
and     s.inst_id = ?
and     t.xidusn = n.usn
and     t.ses_addr = s.saddr (+)
and     t.inst_id = s.inst_id (+)
and     p.name = 'db_block_size'
and     p.inst_id = ?
group by s.sid, start_time, s.username, n.name
/