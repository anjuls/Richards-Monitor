SELECT s2.sid "blocking Sid"
,      s2.sql_hash_value
,      s2.sql_address
FROM v$session s2 
WHERE s2.sid IN ((SELECT TO_NUMBER(SUBSTR(TO_CHAR(RAWTOHEX(s1.p2raw)), 1, 8), 'XXXXXXXX' ) "blocking sid" 
                  FROM v$session_wait s1 
                  WHERE s1.event = 'cursor: pin S wait on X') )
/
