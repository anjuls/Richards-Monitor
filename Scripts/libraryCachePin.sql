SELECT distinct s.sid
,      pr.spid
,      kglpnmod "Mode"
,      kglpnreq "Request" 
FROM x$kglpn p
,    gv$session s
,    gv$session_wait w
,    gv$process pr 
WHERE p.kglpnuse = s.saddr 
  AND kglpnhdl = w.p1raw 
  AND w.event = 'library cache pin'
  AND pr.addr = s.paddr 
  AND p.inst_id = s.inst_id 
  AND s.inst_id = w.inst_id 
  AND s.inst_id = pr.inst_id 
