SELECT * 
FROM (SELECT TO_CHAR(first_time, 'dd-mon-yyyy HH24' )
      ,      COUNT(*) 
      FROM v$loghist 
      WHERE thread# = ? 
      GROUP BY TO_CHAR(first_time, 'dd-mon-yyyy HH24') 
      ORDER BY TO_DATE(TO_CHAR(first_time, 'dd-mon-yyyy HH24'), 'dd-mon-yyyy HH24') desc ) 
WHERE rownum < 101 

