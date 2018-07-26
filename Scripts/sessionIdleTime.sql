select * 
from (select sid
      ,      username
      ,      osuser
      ,      status
      ,      to_char(logon_time,'dd-mm-yy hh:mi:ss') "LOGON"
      ,      floor(last_call_et/3600)||':'||floor(mod(last_call_et,3600)/60)||':'||mod(mod(last_call_et,3600),60) "IDLE"
      ,      substr(program,1,35) "program"
      ,      substr(machine,1,35) "machine"
      from v$session
      where type='USER'
      order by last_call_et desc)
where rownum < 51
