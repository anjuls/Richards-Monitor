SELECT i.instance_name
,      i.version
,      i.startup_time "Startup Time"
,      i.status
,      d.log_mode
,      i.host_name
,      u.users "#Users"
,      ap.aps "#Active Users"
,      bp.bps "#Background Procs" 
FROM gv$instance i
,    gv$database d
,    (SELECT inst_id
      ,      COUNT(*) users 
      FROM gv$session 
      GROUP BY inst_id ) u
,    (SELECT inst_id
      ,      COUNT(*) bps 
      FROM gv$session s 
      WHERE type = 'BACKGROUND'
      GROUP BY inst_id ) bp
,    (SELECT inst_id
      ,      COUNT(*) aps 
      FROM gv$session s 
      WHERE status = 'ACTIVE'
      GROUP BY inst_id ) ap 
WHERE i.inst_id = d.inst_id 
  AND i.inst_id = u.inst_id 
  AND i.inst_id = bp.inst_id 
  AND i.inst_id = ap.inst_id