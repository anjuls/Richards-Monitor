select object_name "Procedure"
from dba_objects
where object_type = 'PROCEDURE'
and owner = ?
/