SELECT /*+ RULE */ *
FROM (SELECT t.owner towner
      ,      t.table_name tname
      ,      s1.blocks tblocks
      ,      i.index_name iname
      ,      s2.blocks iblocks
      FROM dba_tables t
      ,    dba_segments s1
      ,    dba_segments s2
      ,    dba_indexes i
      WHERE t.owner not IN ('SYS', 'SYSTEM', 'OUTLN', 'OPS$ORACLE', 'PERFSTAT', 'CTXSYS', 'DBSNMP', 'EXFSYS','SYSMAN')
        AND t.owner = s1.owner
        AND t.table_name = s1.segment_name
        AND i.owner = s2.owner
        AND i.index_name = s2.segment_name
        AND t.table_name = i.table_name ) e
WHERE iblocks > tblocks
ORDER BY towner
,        tname
,        iname
/