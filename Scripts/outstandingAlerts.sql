select creation_time "Creation Time"
,      time_suggested "Suggested Time"
,      reason
,      owner
,      object_name
,      subobject_name
,      suggested_action 
,      advisor_name
,      metric_value
,      message_group
,      hosting_client_id
,      host_id
from dba_outstanding_alerts
order by creation_time;