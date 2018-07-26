select substr(l.lmode,1,1)||' '||
       decode(l.lmode,0,'None'
                     ,1,'Null'
                     ,2,'Row-S(SS)'
                     ,3,'Row-X(SX)'
                     ,4,'Share'
                     ,5,'S/Row-X(SSX)'
                     ,6,'Exclusive'
                     ,l.lmode) "Lock"
,      substr(l.request,1,1)||' '|| 
       decode(l.request,0,'None'
                       ,1,'Null'
                       ,2,'Row-S(SS)'
                       ,3,'Row-X(SX)'
                       ,4,'Share'
                       ,5,'S/Row-X(SSX)'
                       ,6,'Exclusive'
                       ,l.lmode) "Request"
,      l.id1 id1
,      l.id2 id2
,      decode(l.type,'MR','Media Recovery'
                    ,'RT','Redo Thread'
                    ,'UN','User Name'
                    ,'TX','Transaction'
                    ,'TM','DML' 
                    ,'UL','PL/SQL User Lock'
                    ,'DX','Distributed Xaction'
                    ,'CF','Control File'
                    ,'IS','Instance State'
                    ,'FS','File Set'
                    ,'IR','Instance Recovery'
                    ,'ST','Disk Space Transaction'
                    ,'TS','Temp Segment'
                    ,'IV','Library Cache Invalidation'
                    ,'LS','Log Start or Switch'
                    ,'RW','Row Wait'
                    ,'SQ','Sequence Number'
                    ,'ET','Extend Table'
                    ,'TT','Temp Table'
                    ,l.type) "Lock Type"
from   gv$session s
,      gv$lock l
where  s.sid = ?
and    s.inst_id = ?
and    l.sid (+) = s.sid
and    l.inst_id (+) = s.inst_id
/