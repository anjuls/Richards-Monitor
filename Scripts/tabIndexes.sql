select *
from dba_indexes
where owner = ?
and   table_name = ?
/