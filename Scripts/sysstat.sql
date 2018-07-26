select name,value
from gv$sysstat
where name in (?,?,?)
and inst_id = ?
/