SELECT c.name
,      fu.name
,      fu.version
,      fu.detected_usages
,      fu.total_samples
,      fu.currently_used
,      fu.first_usage_date
,      fu.last_usage_date
,      fu.last_sample_date
FROM cdb_feature_usage_statistics fu
,    v$containers c
WHERE fu.con_id = c.con_id
ORDER BY c.con_id
/
