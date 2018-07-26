SELECT l.sid
,      DECODE(l.type,'MR','Media Recovery'
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
,      DECODE(l.request,0,'None'
                       ,1,'Null'
                       ,2,'Row-S(SS)'
                       ,3,'Row-X(SX)'
                       ,4,'Share'
                       ,5,'S/Row-X(SSX)'
                       ,6,'Exclusive'
                       ,l.lmode) "Lock Request"
FROM gv$lock l 
WHERE l.id1 IN (SELECT l2.id1 
                FROM gv$lock l2 
                WHERE l2.sid = ? 
                  AND l2.inst_id = ? 
                  AND l2.lmode > 0 )  
  AND l.request > 0 
/
