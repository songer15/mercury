CREATE external TABLE
IF NOT EXISTS mfc_dw.current_device_user_info_tmp (
	actionTime bigint,
	appVersion bigint, 
	loginType string,
	create_time bigint,
	vendorId bigint,
	userId string,
	createTime bigint,
	countryCode string,
	device string,
	did string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile Location '/mfc/tmp/current_device_user_info';

CREATE TABLE
IF NOT EXISTS mfc_dw.current_device_user_info(
	actionTime bigint,
	appVersion bigint, 
	loginType string,
	create_time bigint,
	vendorId bigint,
	userId string,
	createTime bigint,
	countryCode string,
	device string,
	did string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_dw.current_device_user_info_inc(
	actionTime bigint,
	appVersion bigint,
	loginType string,
	vendorId bigint,
	userId string,
	countryCode string,
	device string,
	did string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

INSERT overwrite table mfc_dw.current_device_user_info
SELECT *
FROM mfc_dw.current_device_user_info_tmp
UNION all
SELECT total.*
FROM mfc_dw.current_device_user_info total
LEFT JOIN mfc_dw.current_device_user_info_tmp inc ON total.did = inc.did
WHERE inc.did IS NULL;

insert overwrite table mfc_dw.current_device_user_info_inc
	select max(actionTime),
	LAG(appVer,1,0) over() as appVersion,
	LAG(loginType,1,0) over() as loginType,
	LAG(vendorId,1,0) over() as vendorId,
	LAG(email,1,0) over() as userId,
	LAG(countryCode,1,0) over() as countryCode,
	LAG(device,1,0) over() as device,
	did
    from mfc_behavior.user_login where dt=#parameter group by did

INSERT overwrite table mfc_dw.current_device_user_info
SELECT *
FROM mfc_dw.current_device_user_info_inc
UNION all
SELECT total.*
FROM mfc_dw.current_device_user_info total
LEFT JOIN mfc_dw.current_device_user_info_inc inc ON total.did = inc.did
WHERE inc.did IS NULL;