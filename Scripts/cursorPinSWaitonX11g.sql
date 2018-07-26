SELECT sid
,      sql_id
,      blocking_session
,      blocking_instance
,      blocking_session_status
FROM gv$session s
WHERE s.event = 'cursor: pin S wait on X'
/
