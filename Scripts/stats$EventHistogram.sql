with event_histogram as (
  select /*+ inline ordered index(h) index(se) */
         h.snap_id
       , se.event
       , sum(h.wait_count) total_waits
       , sum(case when (h.wait_time_milli = 1)
                  then (nvl(h.wait_count,0)) else 0 end) to1
       , sum(case when (h.wait_time_milli = 2)
                  then (nvl(h.wait_count,0)) else 0 end) to2
       , sum(case when (h.wait_time_milli = 4)
                  then (nvl(h.wait_count,0)) else 0 end) to4
       , sum(case when (h.wait_time_milli = 8)
                  then (nvl(h.wait_count,0)) else 0 end) to8
       , sum(case when (h.wait_time_milli = 16)
                  then (nvl(h.wait_count,0)) else 0 end) to16
       , sum(case when (h.wait_time_milli = 32)
                  then (nvl(h.wait_count,0)) else 0 end) to32
       , sum(case when (h.wait_time_milli between 64 and 1024)
                  then (nvl(h.wait_count,0)) else 0 end) to1024
       , sum(case when (1024 < h.wait_time_milli)
                  then (nvl(h.wait_count,0)) else 0 end) over
       , decode(i.event, null, 0, 99)                    idle
    from stats$event_histogram h
       , stats$system_event    se
       , stats$idle_event      i
   where se.event_id           = h.event_id
     and se.snap_id            = h.snap_id
     and i.event(+)            = se.event
     and se.instance_number    = ?
     and se.dbid               = ?
     and h.instance_number     = ?
     and h.dbid                = ?
   group by h.snap_id
       , se.event
       , decode(i.event, null, 0, 99)
  )
select e.event
     , lpad(case
              when e.total_waits - nvl(b.total_waits,0) <= 9999
                   then to_char(e.total_waits - nvl(b.total_waits,0))||' '
              when trunc((e.total_waits - nvl(b.total_waits,0))/1000) <= 9999
                   then to_char(trunc((e.total_waits - nvl(b.total_waits,0))/1000))||'K'
              when trunc((e.total_waits - nvl(b.total_waits,0))/1000000) <= 9999
                   then to_char(trunc((e.total_waits - nvl(b.total_waits,0))/1000000))||'M'
              when trunc((e.total_waits - nvl(b.total_waits,0))/1000000000) <= 9999
                   then to_char(trunc((e.total_waits - nvl(b.total_waits,0))/1000000000))||'G'
              when trunc((e.total_waits - nvl(b.total_waits,0))/1000000000000) <= 9999
                   then to_char(trunc((e.total_waits - nvl(b.total_waits,0))/1000000000000))||'T'
              else substr(to_char(trunc((e.total_waits - nvl(b.total_waits,0))/1000000000000000))||'P', 1, 5) end
            , 5, ' ')                                                              total_waits
     , substr(to_char(decode(e.to1-nvl(b.to1,0),0,to_number(NULL),(e.to1-nvl(b.to1,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<1ms"
     , substr(to_char(decode(e.to2-nvl(b.to2,0),0,to_number(NULL),(e.to2-nvl(b.to2,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<2ms"
     , substr(to_char(decode(e.to4-nvl(b.to4,0),0,to_number(NULL),(e.to4-nvl(b.to4,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<4ms"
     , substr(to_char(decode(e.to8-nvl(b.to8,0),0,to_number(NULL),(e.to8-nvl(b.to8,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<8ms"
     , substr(to_char(decode(e.to16-nvl(b.to16,0),0,to_number(NULL),(e.to16-nvl(b.to16,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<16ms"
     , substr(to_char(decode(e.to32-nvl(b.to32,0),0,to_number(NULL),(e.to32-nvl(b.to32,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<32ms"
     , substr(to_char(decode(e.to1024-nvl(b.to1024,0),0,to_number(NULL),(e.to1024-nvl(b.to1024,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) "<1s"
     , substr(to_char(decode(e.over-nvl(b.over,0),0,to_number(NULL),(e.over-nvl(b.over,0))*100/(e.total_waits-nvl(b.total_waits,0))),'999.9MI'),1,5) ">1s"
  from ( select *
           from event_histogram
          where snap_id          = ?) b
     , ( select *
           from event_histogram
          where snap_id          = ?) e
 where b.event(+) = e.event
   and (e.total_waits - nvl(b.total_waits,0)) > 0
 order by e.idle, e.event;