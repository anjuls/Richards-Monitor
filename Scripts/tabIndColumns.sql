select index_name,column_name
from dba_ind_columns
where index_owner = ?
and   table_name = ?
order by index_name,column_position
/