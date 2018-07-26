SELECT servers_highwater
,      servers_started
,      servers_terminated
,      maximum_sessions
,      maximum_connections 
FROM gv$shared_server_monitor s 
