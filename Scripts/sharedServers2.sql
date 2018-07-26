SELECT name
,      paddr
,      requests
,      busy "Busy (cs)"
,      idle "Idle (cs)"
,      status 
FROM gv$shared_server s 
ORDER BY name 