SELECT ksmchcom contents
,      COUNT(*) chunks
,      SUM(DECODE(ksmchcls, 'R-recr', ksmchsiz)) recreatable
,      SUM(DECODE(ksmchcls, 'R-freea', ksmchsiz)) freeable
,      SUM(ksmchsiz) total 
FROM sys.x$ksmspr k
GROUP BY ksmchcom 
/