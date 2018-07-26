SELECT name
,      bytes /1024 /1024 "size mb"
,      status 
FROM v$tempfile t
ORDER BY name
