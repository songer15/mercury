CREATE external TABLE
IF NOT EXISTS mfc_behavior.user_play (
	actionDetail string,
	actionTime bigint,
	actionType string,
	preset_custom_ip string,
	localForwardIp string,
	appVersion string,
	device string,
	did string,
	email string,
	episode int,
	language string,
	loginType string,
	playDuration bigint,
	playResName string,
	season int,
	sourcePage string,
	sourcePageId string,
	vendorId bigint,
	videoId string,
	videoType string,
	LocalCreateTimestamps BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) PARTITIONED BY (dt int) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile Location '/mfc/user_log/user_play_log';

CREATE TABLE
IF NOT EXISTS report_forms.mfc_behabior_play_analyse (
	play_count bigint,
	user_count bigint,
	device_count bigint,
	play_time float,
	action_time bigint,
	CountryCode string,
	device string,
	video_type string,
	language string,
	action_detail string,
	vendorId bigint,
	loginType string,
	metric_date string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS report_forms.mfc_behabior_play_top_analyse (
	videoId string,
	play_count bigint,
	CountryCode string,
	device string,
	video_type string,
	language string,
	action_detail string,
	vendorId bigint,
	loginType string,
	metric_date string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

LOAD DATA INPATH '/mfc/user_log/user_play_log/mfc_app_play_action-2020-4-server.log' INTO TABLE mfc_behavior.user_play PARTITION(dt=20201200);

INSERT overwrite TABLE report_forms.mfc_behabior_play_analyse
SELECT
	count(1) as play_count,
	case loginType
	WHEN 'ACCT' THEN
			count(distinct email)
		ELSE
			count(distinct did)
		END 
	AS user_count,
	count(distinct did) as device_count,
	avg(playDuration) / 60 as play_time,
	min(actionTime) as action_time,
	CountryCode,
	device,
	videoType as video_type,
	language,
	actionDetail as action_detail,
	vendorId,
	loginType,
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
	videoType,
	language,
	actionDetail,
	vendorId,
	loginType
union all 
select * from report_forms.mfc_behabior_play_analyse
where metric_date != from_unixtime(
		floor(#metricTime / 1000),
		'yyyy-MM-dd'
	);

insert overwrite TABLE report_forms.mfc_behabior_play_top_analyse
select b.* from (
select a.* from(
select 
	videoId,
	count(1) as play_count,
	CountryCode,
	device,
	videoType as video_type,
	language,
	actionDetail as action_detail,
	vendorId,
	loginType,
	from_unixtime(
		floor(1614823200000 / 1000),
		'yyyy-MM-dd'
	) AS metric_date
	from mfc_behavior.user_play
	where playDuration < 43200 and playDuration >0 and dt='20210304' and videoId is NOT null 
	group by 
	CountryCode,
	device,
	videoType,
	language,
	actionDetail,
	vendorId,
	loginType,
	videoId
)a order by a.play_count desc limit 100)b
union all 
select * from report_forms.mfc_behabior_play_top_analyse
where metric_date != from_unixtime(
		floor(1614823200000 / 1000),
		'yyyy-MM-dd'
	);
