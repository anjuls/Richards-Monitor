SELECT name,value/1024/1024
FROM gv$pgastat
WHERE name in ('aggregate PGA target parameter','aggregate PGA auto target','total PGA inuse','total PGA allocated','over allocation count')
  and inst_id = ?
ORDER by 1
/

