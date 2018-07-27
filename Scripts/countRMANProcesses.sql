select count(*)
from   v$session s
where s.program like 'rman%'
   or s.module like 'backup%'
/