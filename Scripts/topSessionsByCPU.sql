SELECT s.sid
,      p.spid "OS Pid"
,      s.username
,      st.value /100 "CPU Sec" 
,      s.osuser
,      s.logon_time "Logon Time"
,      s.server
,      s.status
,      s.type
,      s.program
,      s.terminal
,      s.machine
,      s.module
FROM gv$sesstat st
,    gv$statname sn
,    gv$session s
,    gv$process p 
WHERE sn.name = 'CPU used by this session'
  AND st.statistic# = sn.statistic# 
  AND s.sid = st.sid
  AND s.paddr = p.addr 
  AND s.inst_id = st.inst_id
  AND s.inst_id = p.inst_id
  AND st.inst_id = sn.inst_id
ORDER BY st.value desc