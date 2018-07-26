select 'Redo size',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Logical reads',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Block changes',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Physical reads',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Physical writes',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'User calls',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Parses',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Hard Parses',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Sorts',round((?+?)/?,2) "per second", round((?+?)/?,2) "per transaction"
from dual
union all
select 'Logons',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Executes',round(?/?,2) "per second", round(?/?,2) "per transaction"
from dual
union all
select 'Transactions',round(?/?,2) "per second", 1 "per transaction"
from dual
/