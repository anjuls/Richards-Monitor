select *
from dba_tab_col_statistics
where owner = ?
and table_name = ?
/