SELECT x.ksppinm
,      y.ksppstvl 
FROM x$ksppi x
,    x$ksppcv y 
WHERE x.indx = y.indx 
  AND x.ksppinm like '\_%'escape '\'
ORDER BY x.ksppinm 
/
