select * 
from dba_queues
where owner = ?
and name like ?
/