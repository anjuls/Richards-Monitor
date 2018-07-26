SELECT REPLACE(block_size /1024 || 'k', ? /1024 || 'k', SUBSTR(name, 1, 1)) name
,      size_for_estimate "Size for Estimate"
,      ROUND(NVL(size_factor, DECODE(REPLACE(block_size /1024 || 'k', ? /1024 || 'k', SUBSTR(name, 1, 1)), '2k', size_for_estimate *1024 / (k.k2_cache /1024), '4k', size_for_estimate *1024 / (k.k4_cache /1024), '8k', size_for_estimate *1024 / (k.k8_cache /1024), '16k', size_for_estimate *1024 / (k.k16_cache /1024), '32k', size_for_estimate *1024 / (k.k32_cache /1024), 'D', size_for_estimate *1024 / (k.def_cache /1024), 'K', size_for_estimate *1024 / (k.kee_cache /1024), 'R', size_for_estimate *1024 / (k.rec_cache /1024))), 1) "Size Factor"
,      buffers_for_estimate "Buffers for Estimate"
,      ROUND(estd_physical_read_factor, 1) "Estimated Physical Read Factor"
,      estd_physical_reads "Estimated Physical Reads" 
FROM stats$db_cache_advice
,    (SELECT NVL(SUM(CASE 
                     WHEN name = 'db_2k_cache_size'THEN VALUE
                     ELSE '0'
                     END), '0' ) k2_cache
      ,      NVL(SUM(CASE 
                     WHEN name = 'db_4k_cache_size'THEN VALUE
                     ELSE '0'
                     END), '0' ) k4_cache
      ,      NVL(SUM(CASE 
                     WHEN name = 'db_8k_cache_size'THEN VALUE
                     ELSE '0'
                     END), '0' ) k8_cache
      ,      NVL(SUM(CASE 
                     WHEN name = 'db_16k_cache_size'THEN VALUE
                     ELSE '0'
                     END), '0' ) k16_cache
      ,      NVL(SUM(CASE 
                     WHEN name = 'db_32k_cache_size'THEN VALUE
                     ELSE '0'
                     END), '0' ) k32_cache
      ,      DECODE(NVL(SUM(CASE 
                            WHEN name = 'db_keep_cache_size'THEN VALUE
                            ELSE '0'
                            END), '0'), '0', NVL(SUM(CASE 
                                                     WHEN name = 'buffer_pool_keep'THEN TO_CHAR(DECODE(0, instrb (VALUE, 'buffers'), VALUE, DECODE(1, instrb (VALUE, 'lru_latches'), SUBSTR(VALUE, instrb (VALUE, '  ,') +10, 9999), DECODE(0, instrb (VALUE, '  , lru'), substrb (VALUE, 9, 99999), substrb (substrb (VALUE, 1, instrb (VALUE, '  , lru_latches:') -1), 9, 99999)))) * ?) 
                                                     ELSE '0'
                                                     END), '0'), SUM(CASE 
                                                                     WHEN name = 'db_keep_cache_size'THEN VALUE
                                                                     ELSE '0'
                                                                     END)) kee_cache
      ,      DECODE(NVL(SUM(CASE 
                            WHEN name = 'db_recycle_cache_size'THEN VALUE
                            ELSE '0'
                            END), '0'), '0', NVL(SUM(CASE 
                                                     WHEN name = 'buffer_pool_recycle'THEN TO_CHAR(DECODE(0, instrb (VALUE, 'buffers'), VALUE, DECODE(1, instrb (VALUE, 'lru_latches'), SUBSTR(VALUE, instrb (VALUE, '  ,') +10, 9999), DECODE(0, instrb (VALUE, '  , lru'), substrb (VALUE, 9, 99999), substrb (substrb (VALUE, 1, instrb (VALUE, '  , lru_latches:') -1), 9, 99999)))) * ?) 
                                                     ELSE '0'
                                                     END), '0'), SUM(CASE 
                                                                     WHEN name = 'db_recycle_cache_size'THEN VALUE
                                                                     ELSE '0'
                                                                     END)) rec_cache
      ,      DECODE(NVL(SUM(CASE 
                            WHEN name = '__db_cache_size'THEN VALUE
                            ELSE '0'
                            END), '0'), '0', NVL(SUM(CASE 
                                                     WHEN name = 'db_block_buffers'THEN TO_CHAR(VALUE * ?) 
                                                     ELSE '0'
                                                     END), '0'), SUM(CASE 
                                                                     WHEN name = '__db_cache_size'THEN VALUE
                                                                     ELSE '0'
                                                                     END)) def_cache 
      FROM stats$parameter 
      WHERE name IN ('db_2k_cache_size', 'db_4k_cache_size', 'db_8k_cache_size', 'db_16k_cache_size', 'db_32k_cache_size', '__db_cache_size', 'db_block_buffers', 'db_keep_cache_size', 'buffer_pool_keep', 'db_recycle_cache_size', 'buffer_pool_recycle') 
        AND snap_id = ? 
        AND dbid = ? 
        AND instance_number = ? ) k 
WHERE snap_id = ? 
  AND dbid = ? 
  AND instance_number = ? 
  AND estd_physical_reads > 0 
ORDER BY name
,        block_size
,        buffers_for_estimate