select object_name "Java Source"
from dba_objects
where object_type = 'JAVA SOURCE'
and owner = ?
/