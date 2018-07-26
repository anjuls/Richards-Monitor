select owner
,      object_name
,      subobject_name
,      object_type
,      to_char(created,'dd-mon-yyyy hh24:mi:ss') "date created"
,      status
,      temporary temp
,      generated gen
from dba_objects
where object_name like upper ( ? )
/