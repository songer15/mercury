CREATE TABLE
IF NOT EXISTS mfc.device_user (
	act_date BIGINT,
	appver BIGINT,
	batch_number string,
	biz_duration BIGINT,
	biz_to_account INT,
	biz_type string,
	client_region string,
	create_time BIGINT,
	did string,
	expire_ts BIGINT,
	install_type INT,
	last_modify_time BIGINT,
	login_id string,
	login_key string,
	login_type string,
	login_uid BIGINT,
	reg_ts BIGINT,
	region string,
	status INT,
	uid BIGINT,
	user_group string,
	vendor_channel_id string,
	vendor_id BIGINT,
	vendor_prop string,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc.device_user_increment (
	act_date BIGINT,
	appver BIGINT,
	batch_number string,
	biz_duration BIGINT,
	biz_to_account INT,
	biz_type string,
	client_region string,
	create_time BIGINT,
	did string,
	expire_ts BIGINT,
	install_type INT,
	last_modify_time BIGINT,
	login_id string,
	login_key string,
	login_type string,
	login_uid BIGINT,
	reg_ts BIGINT,
	region string,
	status INT,
	uid BIGINT,
	user_group string,
	vendor_channel_id string,
	vendor_id BIGINT,
	vendor_prop string,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;


 LOAD DATA inpath '/mfc/activation/device/*-parameter1' overwrite INTO TABLE mfc.device_user_increment;

INSERT overwrite table mfc.device_user
SELECT *
FROM mfc.device_user_increment
UNION all
SELECT total.*
FROM mfc.device_user total
LEFT JOIN mfc.device_user_increment inc ON total.uid = inc.uid
WHERE inc.uid IS NULL;

 LOAD DATA inpath '/mfc/activation/device_biz/*-parameter2' overwrite INTO TABLE mfc.device_user_increment;
 
INSERT overwrite table mfc.device_user
SELECT *
FROM mfc.device_user_increment
UNION all
SELECT total.*
FROM mfc.device_user total
LEFT JOIN mfc.device_user_increment inc ON total.uid = inc.uid
WHERE inc.uid IS NULL;