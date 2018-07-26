SELECT sample_time
,      users.username
,      sql_id 
,      sql_plan_hash_value
,      top_level_sql_id
,      command_name
,      sql_plan_line_id
,      sql_plan_operation
,      sql_exec_id
,      sql_exec_start
,      session_type
,      session_state
,      blocking_session
,      blocking_session_status
,      blocking_inst_id
,      event
,      p1
,      p2
,      p3
,      wait_time
,      time_waited
,      services.name "Service"
,      program
,      module
,      action 
,      in_connection_mgmt
,      in_parse
,      in_hard_parse
,      in_sql_execution
,      in_plsql_execution
,      in_plsql_rpc
,      in_plsql_compilation
,      in_java_execution
,      in_bind
,      in_cursor_close
,      in_sequence_load
,      pga_allocated
,      temp_space_allocated
FROM dba_hist_active_sess_history ash
,    dba_users users
,    dba_services services
,    dba_hist_sqlcommand_name command
WHERE ash.user_id = users.user_id 
  AND ash.service_hash = services.name_hash 
  AND ash.sql_opcode = command.command_type
  AND ash.snap_id > ? 
  AND ash.snap_id <= ?
  AND ash.dbid = ?
  AND ash.instance_number = ?
  AND ash.sql_id = ?
ORDER BY sample_time
/

