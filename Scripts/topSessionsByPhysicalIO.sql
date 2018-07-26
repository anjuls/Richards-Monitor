SELECT s.sid
,      p.spid "OS Pid"
,      s.username
,      st.value "Physical Reads"
,      st2.value "Physical Writes"
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
,    gv$sesstat st2
,    gv$statname sn
,    gv$statname sn2
,    gv$session s
,    gv$process p 
WHERE sn.name = 'physical reads'
  AND sn2.name = 'physical writes'
  AND st.statistic# = sn.statistic#
  AND st2.statistic# = sn2.statistic# 
  AND st.sid = s.sid 
  AND st2.sid = s.sid 
  AND s.paddr = p.addr
  AND s.inst_id = st.inst_id
  AND s.inst_id = st2.inst_id
  AND s.inst_id = p.inst_id
  AND st.inst_id = sn.inst_id
  AND st2.inst_id = sn2.inst_id
ORDER BY st.value + st2.value desc