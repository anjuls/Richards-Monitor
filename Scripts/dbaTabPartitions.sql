select *
from dba_tab_partitions
where table_owner = ?
and table_name = ?
/