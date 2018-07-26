select *
from dba_ind_partitions
where index_owner = ?
and index_name = ?
/