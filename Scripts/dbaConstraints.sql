select *
from dba_constraints
where owner = ?
and table_name = ?
/