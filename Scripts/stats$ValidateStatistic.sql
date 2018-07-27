select name
from v$statname
where lower(name) = lower(?)
/