select *
from dba_ind_expressions
where index_owner = ?
and index_name = ?
/