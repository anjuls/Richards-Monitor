select count(*)
from v$session
where type = 'USER'
/