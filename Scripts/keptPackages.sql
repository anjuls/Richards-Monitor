select owner
,      name
,      namespace
,      executions
from v$db_object_cache
where kept = 'YES'
/