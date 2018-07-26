select *
from dba_tab_subpartitions
where table_owner = ?
and table_name = ?
/