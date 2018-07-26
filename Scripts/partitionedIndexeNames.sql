select index_name
from dba_part_indexes
where owner = ?
and index_name like ?
/