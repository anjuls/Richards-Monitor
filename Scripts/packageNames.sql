select object_name "Package"
from dba_objects
where object_type = 'PACKAGE'
and owner = ?
/