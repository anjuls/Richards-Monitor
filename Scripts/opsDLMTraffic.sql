select inst_id
,      local_nid
,      remote_nid
,      tckt_avail
,      tckt_limit
,      tckt_wait
from gv$dlm_traffic_controller
order by tckt_avail
/
