SELECT name
,      SUBSTR(network, 20, ABS(20 - INSTR(network, ')'))) "Protocol"
,      status
,      owned
,      ROUND((busy / (busy + idle)) * 100, 2) "% Busy" 
FROM gv$dispatcher d
