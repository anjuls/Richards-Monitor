select b.snap_id                                          "Begin Snapshot Id"
     , e.snap_id                                          "End Snapshot Id"     
     , to_char(b.snap_time, 'dd-Mon-yy hh24:mi:ss')       "Begin Snapshot Time"     
     , to_char(e.snap_time, 'dd-Mon-yy hh24:mi:ss')       "End Snapshot Time"      
     , round(((e.snap_time - b.snap_time) * 1440 * 60)/60, 2)   "Snapshot Duration (Mins)"                                    
     , blog.value                                         "Begin Snapshot Sessions"          
     , elog.value                                         "End Snapshot Sessions"           
     , round(bocur.value/blog.value,2)                    "Begin Snapshot Curs Per Sess"              
     , round(eocur.value/elog.value,2)                    "End Snapshot Curs Per Sess"
     , b.ucomment                                         "Begin Snapshot Comment"
     , e.ucomment                                         "End Snapshot Comment"
from stats$snapshot b
,    stats$snapshot e
, (select value 
        from stats$sysstat
        where snap_id = ?
        and   name = 'logons current') blog
, (select value
        from stats$sysstat 
        where snap_id = ?
        and name = 'opened cursors current') eocur
, (select value
        from stats$sysstat 
        where snap_id = ?
        and name = 'opened cursors current') bocur
, (select value 
        from stats$sysstat
        where snap_id = ?
        and   name = 'logons current') elog
where  b.snap_id = ?
   and e.snap_id = ?
   and b.dbid            = ?
   and e.dbid            = ?
   and b.instance_number = ?
   and e.instance_number = ?
   and b.startup_time    = e.startup_time
   and b.snap_time       < e.snap_time
/
