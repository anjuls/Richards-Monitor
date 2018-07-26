select *
from dba_ind_subpartitions
where index_owner = ?
and index_name = ?
/