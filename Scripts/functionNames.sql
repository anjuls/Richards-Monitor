select object_name "Function"
from dba_objects
where object_type = 'FUNCTION'
and owner = ?
/