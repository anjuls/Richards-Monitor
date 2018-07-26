SELECT name
,      paddr
,      requests
,      ROUND((busy / (busy + idle)) * 100, 2) "% Busy"
,      status 
FROM gv$shared_server s
