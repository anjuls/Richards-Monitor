SELECT sid
,      SUBSTR(username, 1, 20) "Username"
,      elapsed_seconds
,      time_remaining
,      start_time "Start Time"
,      message 
FROM gv$session_longops l 
WHERE sofar != totalwork 
ORDER BY time_remaining desc
