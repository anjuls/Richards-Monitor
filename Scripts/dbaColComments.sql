select *
from dba_col_comments
where owner = ?
and table_name = ?
/