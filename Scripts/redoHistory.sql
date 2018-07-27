SELECT thread#
,      sequence#
,      first_time "First Time" 
FROM v$loghist l 
ORDER BY first_time desc 
