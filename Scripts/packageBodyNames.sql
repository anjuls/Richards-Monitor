select object_name "Package Body"
from dba_objects
where object_type = 'PACKAGE BODY'
and owner = ?
/