SELECT sox_ind 
FROM monitor.databases 
WHERE LOWER(database_name) like LOWER(?) 
  AND LOWER(sox_ind) = 'y'
  AND live_date = (SELECT MAX(live_date) 
                   FROM monitor.databases 
                   WHERE LOWER(database_name) like LOWER(?) 
                     AND LOWER(sox_ind) = 'y') 
 /