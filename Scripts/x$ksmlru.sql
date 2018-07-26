SELECT addr
,      ksmlrcom "Alloc Type"
,      ksmlrsiz "Size of Alloc"
,      ksmlrnum "Num Objects Flushed"
,      ksmlrhon "Object being loaded"
,      sid
,      ksmlrohv "hash value of object loading" 
FROM sys.x$ksmlru
,    gv$session s 
WHERE ksmlrses = s.saddr (+)
/