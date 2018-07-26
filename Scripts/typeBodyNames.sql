select object_name
from dba_objects
where owner = ?
and object_type = 'TYPE BODY'
/