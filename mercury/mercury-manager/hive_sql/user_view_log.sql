CREATE external TABLE
IF NOT EXISTS mfc_behavior.user_view (
	model string,
	brand string,
	androidId string,
	apkSource string,
	clientIp string,
	actionDetail string,
	actionTime bigint,
	actionType string,
	preset_custom_ip string,
	localForwardIp string,
	appVersion string,
	device string,
	did string,
	email string,
	language string,
	loginType string,
	vendorId bigint,
	videoId string,
	LocalCreateTimestamps BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) PARTITIONED BY (dt int) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile Location '/mfc/user_log/user_view_log';

CREATE TABLE
IF NOT EXISTS report_forms.mfc_behabior_view_analyse (
	view_count bigint,
	device_count bigint,
	action_time bigint,
	country_code string,
	device string,
	language string,
	metric_date string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

LOAD DATA INPATH '/mfc/user_log/user_view_log/*-parameter1' INTO TABLE mfc_behavior.user_view PARTITION(dt=parameter2);

INSERT overwrite TABLE report_forms.mfc_behabior_view_analyse
SELECT
	count(1) as view_count,
	count(distinct did) as device_count,
	min(actionTime) as action_time,
	CountryCode as country_code,
	device,
	language,
	from_unixtime(
		floor(#metricTime / 1000),
		'yyyy-MM-dd'
	) AS metric_date
FROM
	mfc_behavior.user_play
where playDuration < 43200 and playDuration >0 and dt='#metricTime2'
GROUP BY
	CountryCode,
	device,
	language
union all 
select * from report_forms.mfc_behabior_view_analyse
where metric_date != from_unixtime(
		floor(#metricTime / 1000),
		'yyyy-MM-dd'
	);