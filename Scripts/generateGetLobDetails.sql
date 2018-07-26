select 'select '''||l.owner||''','''||l.table_name||''','''||l.column_name||''','''||l.segment_name||''','''||l.cache||''','''||l.in_row||''', round(avg(dbms_lob.getlength('||l.column_name||')),2) from '||l.owner||'.'||l.table_name
from dba_lobs l,
     dba_tables t
where l.owner not in ('MDSYS','TSMSYS','WK_TEST','OUTLN','CTXSYS','OLAPSYS','SYSTEM','EXFSYS','ORDSYS','SYSMAN','XDB','SYS','WMSYS')
and l.owner not like 'FLOWS%'
and instr(l.column_name,'.') = 0
and l.owner = t.owner
and l.table_name = t.table_name
and t.nested = 'NO'
and l.table_name not in (select queue_table from dba_queue_tables)
and l.table_name not like '%.%'
/
