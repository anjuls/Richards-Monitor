select *
from dba_tables
where owner = ?
and table_name = ?
/