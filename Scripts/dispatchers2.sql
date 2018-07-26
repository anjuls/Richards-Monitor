SELECT name
,      SUBSTR(network, 20, ABS(20 - INSTR(network, ')'))) "Protocol"
,      status
,      owned
,      idle "Idle (cs)"
,      busy "Busy (cs)" 
FROM gv$dispatcher d 
ORDER BY name 