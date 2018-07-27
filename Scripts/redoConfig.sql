SELECT distinct f.group#
,      f.member
,      l.bytes /1024 /1024 "Size in mb"
,      l.status
,      l.archived
,      l.thread#
,      f.status 
FROM v$log l
,    v$logfile f 
WHERE l.group# = f.group#  
ORDER BY f.group# 