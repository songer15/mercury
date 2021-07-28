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
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;