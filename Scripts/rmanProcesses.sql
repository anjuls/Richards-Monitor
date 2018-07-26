SELECT s.sid
,      p.spid spid
,      s.username
,      s.serial#
,      TO_CHAR(s.logon_time, 'dd-mon-yyyy hh24:mi:ss' ) "Logon Time"
,      s.status
,      s.module 
FROM gv$session s
,    gv$process p 
WHERE p.addr = s.paddr 
  AND (   s.program like 'rman%'
       OR s.module like 'backup%') 
  AND s.inst_id = p.inst_id 
/