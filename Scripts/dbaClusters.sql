select *
from dba_clusters
where owner = ?
and cluster_name = ?
/