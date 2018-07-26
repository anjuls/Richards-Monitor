select table_name
from dba_tables
where owner = ?
order by table_name
/