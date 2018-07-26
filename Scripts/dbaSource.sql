select text
from dba_source
where owner = ?
and name = ?
and type = ?
order by line
/