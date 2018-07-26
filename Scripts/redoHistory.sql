SELECT thread#
,      sequence#
,      first_time "First Time" 
FROM gv$loghist l 
ORDER BY first_time desc 
