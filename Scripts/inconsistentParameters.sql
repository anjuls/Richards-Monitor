WITH problempars AS (SELECT name
                     ,      VALUE
                     ,      COUNT(*) 
                     FROM gv$system_parameter 
                     GROUP BY name
                     ,        VALUE
                     minus 
                     SELECT name
                     ,             VALUE
                     ,             COUNT(*) 
                     FROM gv$system_parameter 
                     GROUP BY name
                     ,        VALUE
                     HAVING COUNT(*) > 1 ) 
SELECT DISTINCT sp.inst_id
,      problempars.name 
,      problempars.value
FROM gv$system_parameter sp
,    problempars 
WHERE sp.name = problempars.name 
order by 2,1
/
