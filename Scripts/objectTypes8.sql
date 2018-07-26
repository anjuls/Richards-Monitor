select distinct object_type
from dba_objects
where owner = ?
and object_type in ('TABLE'
                   ,'INDEX'
                   ,'TABLE PARTITION'
                   ,'INDEX PARTITION'
                   ,'VIEW','PACKAGE'
                   ,'PACKAGE BODY'
                   ,'PROCEDURE'
                   ,'FUNCTION'
                   ,'JAVA SOURCE'
                   ,'JAVA CLASS'
                   ,'TRIGGER'
                   ,'SYNONYM'
                   ,'CLUSTER'
                   ,'DATABASE LINK'
                   ,'SEQUENCE'
                   ,'QUEUE'
                   ,'LIBRARY'
                   ,'TYPE'
                   ,'TYPE BODY')
union all
select 'SNAPSHOT'
from dba_snapshots
where owner = ?
and   rownum = 1
order by 1
/