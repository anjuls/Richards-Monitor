select'session_cached_cursors'parameter, lpad(value, 5) value, decode(value, 0, '  n/a', to_char(least(100 * used / value, 100), '990') || '%') "maximum usage" 
FROM (SELECT MAX(s.value) used 
      FROM gv$statname n
      ,    gv$sesstat s
      ,    gv$session ss 
      WHERE n.name = 'session cursor cache count'
        AND s.statistic# = n.statistic# 
        AND s.sid = ss.sid 
        AND (   ss.module not like 'RichMon%'
             OR ss.module is null)
        AND s.inst_id = ? 
        AND s.inst_id = n.inst_id 
        AND s.inst_id = ss.inst_id ) 
,    (SELECT VALUE
      FROM gv$parameter 
      WHERE name = 'session_cached_cursors'
        AND inst_id = ? ) 
UNION ALL 
SELECT 'open_cursors' parameter
,      LPAD(VALUE, 5) VALUE
,      TO_CHAR(LEAST(100 * used / VALUE, 100), '990' ) || '%' "maximum usage" 
FROM (SELECT MAX(SUM(s.value)) used 
      FROM gv$statname n
      ,    gv$sesstat s
      ,    gv$session ss 
      WHERE n.name IN ('opened cursors current') 
        AND s.statistic# = n.statistic# 
        AND s.sid = ss.sid 
        AND (   ss.module not like 'RichMon%'
             OR ss.module is null)
        AND s.inst_id = ? 
        AND s.inst_id = n.inst_id 
        AND s.inst_id = ss.inst_id 
      GROUP BY s.sid ) 
,    (SELECT VALUE
      FROM gv$parameter 
      WHERE name = 'open_cursors'
        AND inst_id = ? ) 
