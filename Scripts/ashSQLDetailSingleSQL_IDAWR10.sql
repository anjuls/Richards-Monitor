SELECT sample_time
,      users.username
,      sql_id 
,      sql_plan_hash_value
,      command_name
,      session_type
,      en.name
,      p1
,      p2
,      p3
,      wait_time
,      time_waited
,      services.name "Service"
,      program
,      module
,      action 
FROM dba_hist_active_sess_history ash
,    dba_users users
,    dba_services services
,    dba_hist_sqlcommand_name command
,    v$event_name en
WHERE ash.user_id = users.user_id 
  AND ash.service_hash = services.name_hash 
  AND ash.sql_opcode = command.command_type
  AND ash.event_id = en.event_id
  AND ash.snap_id > ? 
  AND ash.snap_id <= ?
  AND ash.dbid = ?
  AND ash.instance_number = ?
  AND ash.sql_id = ?
ORDER BY sample_time
/

