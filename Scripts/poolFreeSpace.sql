SELECT pool
,      mb "Free mb"
FROM (SELECT inst_id
      ,      pool
      ,      ROUND(bytes /1024 /1024, 2) as mb
      FROM gv$sgastat 
      WHERE name = 'free memory'
      UNION ALL 
      SELECT inst_id
      ,      'reserved pool' as pool
      ,      ROUND(free_space /1024 /1024, 2) as mb
      FROM gv$shared_pool_reserved ) s 
ORDER BY pool 