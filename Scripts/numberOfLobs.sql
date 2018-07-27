select count(*)
from dba_lobs
where owner not in ('SYS','SYSTEM','DBSNMP','MGMT_VIEW','SYSMAN','TSMSYS','OLAPSYS')
and owner not like 'FLOWS%'
/