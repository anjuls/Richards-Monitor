SELECT username "Account Name" 
,      account_status "Account Status" 
FROM dba_users 
WHERE ( username , password ) IN ( ( 'SYS' , '5638228DAF52805F' ) 
                                 , ( 'SYS' , 'D4C5016086B2DC6A' ) 
                                 , ( 'SYSTEM' , 'D4DF7931AB130E37' ) 
                                 , ( 'CTXSYS' , '24ABAB8B06281B4C' ) 
                                 , ( 'DBSNMP' , 'E066D214D5421CCC' ) 
                                 , ( 'MDSYS' , '72979A94BAD2AF80' ) 
                                 , ( 'MDSYS' , '9AAEB2214DCC9A31' ) 
                                 , ( 'OUTLN' , '4A3BA55E08595C81' ) 
                                 , ( 'SCOTT' , 'F894844C34402B67' ) 
                                 , ( 'ORDCOMMON' , '9B616F5489F90AD7' ) ) 
  AND account_status < > 'EXPIRED \& LOCKED' 
ORDER BY username 
/