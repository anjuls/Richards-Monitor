SELECT distinct f.group#
,      f.member
,      l.bytes /1024 /1024 "Size in mb"
,      l.status
,      l.archived
,      l.thread#
,      f.status 
FROM gv$log l
,    gv$logfile f 
WHERE l.group# = f.group#  
  and l.inst_id = f.inst_id
ORDER BY f.group# 
