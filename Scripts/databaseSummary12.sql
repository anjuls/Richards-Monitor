SELECT c.name
,      i.instance_name
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
,    (SELECT con_id
      ,      inst_id
      ,      COUNT(*) users 
      FROM gv$session 
      GROUP BY con_id,inst_id ) u
,    (SELECT con_id
      ,      inst_id
      ,      COUNT(*) bps 
      FROM gv$session s 
      WHERE type = 'BACKGROUND'
      GROUP BY con_id,inst_id ) bp
,    (SELECT con_id
      ,      inst_id
      ,      COUNT(*) aps 
      FROM gv$session s 
      WHERE status = 'ACTIVE'
      GROUP BY con_id,inst_id ) ap 
,     v$containers c
WHERE i.inst_id = d.inst_id (+)
  AND i.inst_id = u.inst_id
  AND i.inst_id = bp.inst_id (+)
  AND i.inst_id = ap.inst_id
  AND c.con_id = u.con_id
  and c.con_id = ap.con_id
  and c.con_id = bp.con_id (+)
  and c.con_id = d.con_id (+)
ORDER by c.con_id
/