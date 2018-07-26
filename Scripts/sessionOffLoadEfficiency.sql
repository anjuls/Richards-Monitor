SELECT DECODE(s1.value, 0, 0, DECODE(s2.value, 0, 0, ROUND(100 - 100 *s1.value /s2.value, 2))) "Value"
,      'IO SAVED % (SMART SCAN)' "Session Statistic"  
FROM gv$sesstat s1
,    gv$sesstat s2
,    v$statname n1
,    v$statname n2
WHERE s1.statistic# = n1.statistic# 
  AND s2.statistic# = n2.statistic#
  AND n1.name = 'cell physical IO interconnect bytes'
  AND n2.name = 'cell physical IO bytes eligible for predicate offload'
  AND s1.inst_id = ?
  AND s2.inst_id = s1.inst_id
  AND s1.sid = ?
  AND s2.sid = s1.sid
  AND n1.name = 'cell physical IO interconnect bytes'
  AND n2.name = 'cell physical IO bytes eligible for predicate offload'
union all
SELECT s.value
,      n.name 
FROM gv$sesstat s
,    v$statname n 
WHERE s.statistic# = n.statistic# (+) 
  AND s.inst_id = ?
  AND s.sid = ?
  AND n.name in ('physical read total bytes','physical write total bytes','cell IO uncompressed bytes'
                ,'cell physical IO interconnect bytes','cell physical IO bytes eligible for predicate offload'
                ,'cell physical IO bytes saved by storage index','cell physical IO interconnect bytes returned by smart scan'
                ,'cell IO uncompressed bytes')
union all
SELECT s.value
,      n.name 
FROM gv$sesstat s
,    v$statname n 
WHERE s.statistic# = n.statistic# (+) 
  AND s.inst_id = ?
  AND s.sid = ?
  AND n.name not in ('physical read total bytes','physical write total bytes','cell IO uncompressed bytes'
                    ,'cell physical IO interconnect bytes','cell physical IO bytes eligible for predicate offload'
                    ,'cell physical IO bytes saved by storage index','cell physical IO interconnect bytes returned by smart scan'
                    ,'cell IO uncompressed bytes')
  AND n.name like ('cell%')
/
