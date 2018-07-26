SELECT l.name
,      l.gets gets
,      ROUND(l.misses *100 /decode (l.gets, 0, 1, l.gets), 2) miss
,      to_char(l.spin_gets *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep6 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) cspins
,      to_char(l.sleep1 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep7 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) csleep1
,      to_char(l.sleep2 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep8 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) csleep2
,      to_char(l.sleep3 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep9 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) csleep3
,      to_char(l.sleep4 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep10 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) csleep4
,      to_char(l.sleep5 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) || to_char(l.sleep11 *100 /decode (l.misses, 0, 1, l.misses), '990.9' ) csleep5 
FROM gv$latch l 
WHERE l.misses <> 0 
ORDER BY l.gets desc 