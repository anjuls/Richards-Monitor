SELECT name "Parameter Name"
,      VALUE"Parameter Value" 
FROM gv$parameter p
WHERE isdefault = 'FALSE'
ORDER BY name 
/