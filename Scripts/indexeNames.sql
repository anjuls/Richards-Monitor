select index_name
from dba_indexes
where owner = ?
order by index_name
/