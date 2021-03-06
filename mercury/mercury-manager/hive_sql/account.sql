CREATE TABLE
IF NOT EXISTS mfc.account (
	reg_ts BIGINT,
	reg_client_ip string,
	last_modify_time BIGINT,
	available_service_time BIGINT,
	lang_web string,
	type INT,
	trial INT,
	uid BIGINT,
	PASSWORD string,
	first_login_date BIGINT,
	first_phone_login_date BIGINT,
	last_modify_user string,
	product_id INT,
	property INT,
	payment_sys_id string,
	email string,
	product string,
	create_time BIGINT,
	device_limit INT,
	LocalCreateTime BIGINT,
	reg_date BIGINT,
	activate_date BIGINT,
	service_type INT,
	expire_ts BIGINT,
	reg_client_type INT,
	preset_custom_ip string,
	LocalCreateTimestamps BIGINT,
	STATUS INT,
	login_id string,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

 LOAD DATA inpath '/mfc/activation/account/*-parameter1' overwrite INTO TABLE mfc.account;