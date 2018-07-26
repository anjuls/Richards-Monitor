SELECT sid
,      name
,      VALUE
,      isspecified
,      ordinal
,      update_comment 
FROM gv$spparameter p
WHERE name like LOWER(?)
/