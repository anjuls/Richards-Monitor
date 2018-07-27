SELECT comp_id
,      comp_name
,      version
,      status
,      modified
,      namespace
,      control
,      schema
,      procedure
,      startup
,      parent_id
,      other_schemas 
FROM cdb_registry r
,    v$containers c 
WHERE r.con_id = c.con_id 
/
