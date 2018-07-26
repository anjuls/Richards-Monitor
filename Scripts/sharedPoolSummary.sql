SELECT ksmchcom contents
,      COUNT(*) chunks
,      SUM(DECODE(ksmchcls, 'recr', ksmchsiz)) recreatable
,      SUM(DECODE(ksmchcls, 'freeabl', ksmchsiz)) freeable
,      SUM(ksmchsiz) total 
FROM sys.x$ksmsp k 
WHERE ksmchcls not like 'R%'
GROUP BY ksmchcom 
/