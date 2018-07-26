select *
from dba_ind_columns
where index_owner = ?
and index_name = ?
/