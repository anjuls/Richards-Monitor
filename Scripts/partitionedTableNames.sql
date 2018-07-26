select table_name
from dba_part_tables
where owner = ?
and table_name like ?
/