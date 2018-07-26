select *
from dba_tab_comments
where owner = ?
and table_name = ?
/