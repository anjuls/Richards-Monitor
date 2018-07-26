SELECT b.capture_name
,      b.status
,      b.capture_type
,      a.state || '  At ' || TO_CHAR(capture_message_create_time, 'HH24 :MI:SS DD/MM/YYYY' ) state 
,      b.error_message
FROM v$streams_capture a
,    dba_capture b 
WHERE a.capture_name (+) = b.capture_name 
ORDER BY capture_name 
/