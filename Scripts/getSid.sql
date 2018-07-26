select sid, serial#, username
from gv$session
where sid = ?
  and inst_id = ?
/