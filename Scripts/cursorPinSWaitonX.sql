SELECT s2.sid "blocking Sid"
,      s2.sql_id "blocking sql_id" 
FROM v$session s2 
WHERE s2.sid IN ((SELECT TO_NUMBER(SUBSTR(TO_CHAR(RAWTOHEX(s1.p2raw)), 1, 8), 'XXXXXXXX' ) "blocking sid" 
                  FROM v$session s1 
                  WHERE s1.event = 'cursor: pin S wait on X') )
/
