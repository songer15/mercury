CREATE TABLE
IF NOT EXISTS mfc.feedback_info (
	id BIGINT,
	createTime BIGINT,
	lastModifyTime BIGINT,
	lastModifyUser string,
	aid string,
	appVersion BIGINT,
	channelId string,
	clientIp string,
	deviceId string,
	hostId string,
	issueId string,
	issueState INT,
	lang string,
	logFilePath string,
	did string,
	region string,
	reportInfo string,
	reportTime string,
	SYSTEM string,
	uid BIGINT,
	appId string,
	releaseId string,
	TYPE INT,
	uploadPath string,
	uploadStatus INT,
	accAlias string,
	acct string,
	cid string,
	episode string,
	progress string,
	resId string,
	resName string,
	season string,
	ttid string,
	subFileName string,
	subLanguage string,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) COMMENT "feedback_info" ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;


CREATE TABLE
IF NOT EXISTS mfc.feedback_info_increment (
	id BIGINT,
	createTime BIGINT,
	lastModifyTime BIGINT,
	lastModifyUser string,
	aid string,
	appVersion BIGINT,
	channelId string,
	clientIp string,
	deviceId string,
	hostId string,
	issueId string,
	issueState INT,
	lang string,
	logFilePath string,
	did string,
	region string,
	reportInfo string,
	reportTime string,
	SYSTEM string,
	uid BIGINT,
	appId string,
	releaseId string,
	TYPE INT,
	uploadPath string,
	uploadStatus INT,
	accAlias string,
	acct string,
	cid string,
	episode string,
	progress string,
	resId string,
	resName string,
	season string,
	ttid string,
	subFileName string,
	subLanguage string,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) COMMENT "feedback_info_increment" ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

 LOAD DATA inpath '/mfc/feedback/feedback_info/*-parameter1' overwrite INTO TABLE mfc.feedback_info_increment;


INSERT overwrite table mfc.feedback_info
SELECT *
FROM mfc.feedback_info_increment
UNION
SELECT total.*
FROM mfc.feedback_info total
LEFT JOIN mfc.feedback_info_increment inc ON total.id = inc.id
WHERE inc.id IS NULL;


-- CREATE TABLE IF NOT EXISTS report_forms.mfc_feedback (actionType string, hiveTime BIGINT, totalCount INT, issueId string,actionTime string, cid string);


-- INSERT INTO report_forms.mfc_feedback
-- SELECT 'feedback' AS actionType,
--        unix_timestamp()*1000 AS hiveTime,
--        COUNT(1) AS totalCount,
--        a.issueId AS issueId,
--        a.times AS actionTime,
--        concat(a.times,a.issueId) AS cid
-- FROM
--   (SELECT issueId,
--           from_unixtime(floor(createTime/1000),'yyyy-MM-dd') AS times
--    FROM mfc.feedback_info
--    WHERE createTime BETWEEN parameter2 AND parameter3) a
-- GROUP BY a.times,
--          a.issueId;


-- SELECT *
-- FROM report_forms.mfc_feedback
-- WHERE hiveTime > parameter4;

