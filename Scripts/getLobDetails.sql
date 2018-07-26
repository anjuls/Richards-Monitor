SELECT l.owner
,      l.table_name
,      l.column_name
,      l.segment_name
,      l.cache
,      l.in_row
FROM dba_lobs l
,    dba_tables t
WHERE l.owner not IN ('MDSYS', 'TSMSYS', 'WK_TEST', 'OUTLN', 'CTXSYS', 'OLAPSYS', 'SYSTEM', 'EXFSYS', 'ORDSYS', 'SYSMAN', 'XDB', 'SYS', 'WMSYS')
  AND l.owner not like 'FLOWS%'
  AND INSTR(l.column_name, '.') = 0
  AND l.owner = t.owner
  AND l.table_name = t.table_name
  AND t.nested = 'NO'
  AND l.table_name not IN (SELECT queue_table
                           FROM dba_queue_tables )
ORDER BY 1
,        2
,        3