select substr(l.lmode,1,1)||' '||
       decode(l.lmode,0,'None'
                     ,1,'Null'
                     ,2,'Row-S(SS)'
                     ,3,'Row-X(SX)'
                     ,4,'Share'
                     ,5,'S/Row-X(SSX)'
                     ,6,'Exclusive'
                     ,l.lmode) "Lock Desc"
,      substr(l.request,1,1)||' '|| 
       decode(l.request,0,'None'
                       ,1,'Null'
                       ,2,'Row-S(SS)'
                       ,3,'Row-X(SX)'
                       ,4,'Share'
                       ,5,'S/Row-X(SSX)'
                       ,6,'Exclusive'
                       ,l.lmode) "Request Desc"
,      substr(o.object_type,1,30) "Object Type"
,      substr(o.object_name,1,30) "Object Name"
from   gv$lock l
,      dba_objects o
where  l.sid = ?
and    l.inst_id = ?
and    l.type = 'TM'
and    l.id1 = o.object_id (+)
/