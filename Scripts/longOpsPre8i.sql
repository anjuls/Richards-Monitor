SELECT sid
,      elapsed_seconds
,      sofar
,      totalwork
,      start_time "Start Time"
,      msg "Message" 
FROM gv$session_longops l 
WHERE sofar != totalwork 