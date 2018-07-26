select *
from dba_indexes
where owner = ?
and index_name = ?
/