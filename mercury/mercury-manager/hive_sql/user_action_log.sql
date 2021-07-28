CREATE external TABLE
IF NOT EXISTS mfc_behavior.user_login (
	action string,
	actionDetail string,
	actionStatus string,
	actionTime string,
	actionType string,
	apkSource string,
	appVer BIGINT,
	bizDate bigint,
	clientIp string,
	clientRegion string,
	device string,
	did string,
	email string,
	loginType string,
	plan string,
	releaseId string,
	ts BIGINT,
	uid string,
	vendorId string,
	vendorName string,
	LocalCreateTimestamps BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) PARTITIONED BY (dt int) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile Location '/mfc/user_log/user_login_log';

LOAD DATA INPATH '/mfc/user_log/user_action_log/*-2021-1-16' INTO TABLE mfc_behavior.user_login PARTITION(dt=20210116);