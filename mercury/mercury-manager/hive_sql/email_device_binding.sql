CREATE TABLE
IF NOT EXISTS mfc.email_device_binding (
	last_login_region string,
	create_time bigint,
	last_modify_time bigint,
	binding_date bigint,
	custom_id string,
	client_type INT,
	last_login_date BIGINT,
	version BIGINT,
	uid BIGINT,
	last_login_ip string,
	vendor_id BIGINT,
	model string,
	did string,
	LocalCreateTime BIGINT,
	LocalCreateTimestamps BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc.email_device_binding_increment (
	last_login_region string,
	create_time bigint,
	last_modify_time bigint,
	binding_date bigint,
	custom_id string,
	client_type INT,
	last_login_date BIGINT,
	version BIGINT,
	uid BIGINT,
	last_login_ip string,
	vendor_id BIGINT,
	model string,
	did string,
	LocalCreateTime BIGINT,
	LocalCreateTimestamps BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

 LOAD DATA inpath '/mfc/activation/email_device_binding/*-parameter1' overwrite INTO TABLE mfc.email_device_binding_increment;

INSERT overwrite table mfc.email_device_binding
SELECT *
FROM mfc.email_device_binding_increment
UNION all
SELECT total.*
FROM mfc.email_device_binding total
LEFT JOIN mfc.email_device_binding_increment inc ON total.custom_id = inc.custom_id
WHERE inc.custom_id IS NULL;
