select *
from dba_tab_columns
where owner = ?
and table_name = ?
/