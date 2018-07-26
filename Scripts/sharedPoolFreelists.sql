SELECT DECODE(SIGN(ksmchsiz - 812), -1, (ksmchsiz - 16) / 4, DECODE(SIGN(ksmchsiz - 4012), -1, TRUNC((ksmchsiz + 11924) / 64), DECODE(SIGN(ksmchsiz - 65548), -1, TRUNC(1 /log (ksmchsiz - 11, 2)) + 238, 254))) bucket
,      SUM(ksmchsiz) free_space
,      COUNT(*) free_chunks
,      TRUNC(AVG(ksmchsiz)) average_size
,      MAX(ksmchsiz) biggest 
FROM sys.x$ksmsp k 
WHERE ksmchcls = 'free'
GROUP BY DECODE(SIGN(ksmchsiz - 812), -1, (ksmchsiz - 16) / 4, DECODE(SIGN(ksmchsiz - 4012), -1, TRUNC((ksmchsiz + 11924) / 64), DECODE(SIGN(ksmchsiz - 65548), -1, TRUNC(1 /log (ksmchsiz - 11, 2)) + 238, 254)))
/