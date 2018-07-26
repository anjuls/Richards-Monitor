select *
from dba_libraries
where owner = ?
and library_name = ?
/