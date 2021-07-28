CREATE TABLE
IF NOT EXISTS mfc.bill_renewal_increment (
	account_id BIGINT,
	account_type BIGINT,
	create_ip string,
	create_status BIGINT,
	create_time BIGINT,
	current_iid BIGINT,
	data_ver BIGINT,
	biz_type string,
	client_region string,
	id BIGINT,
	invoice_expire_date BIGINT,
	last_modify_ip string,
	last_modify_time BIGINT,
	last_modify_user string,
	license_expire_date BIGINT,
	limit_date string,
	next_payment_date BIGINT,
	order_id string,
	origin_iid BIGINT,
	payment_source string,
	period string,
	pid BIGINT,
	purchase_status bigint,
	renewal_date bigint,
	request_id bigint,
	round bigint,
	status int,
	times int,
	uid bigint,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc.bill_renewal (
	account_id BIGINT,
	account_type BIGINT,
	create_ip string,
	create_status BIGINT,
	create_time BIGINT,
	current_iid BIGINT,
	data_ver BIGINT,
	biz_type string,
	client_region string,
	id BIGINT,
	invoice_expire_date BIGINT,
	last_modify_ip string,
	last_modify_time BIGINT,
	last_modify_user string,
	license_expire_date BIGINT,
	limit_date string,
	next_payment_date BIGINT,
	order_id string,
	origin_iid BIGINT,
	payment_source string,
	period string,
	pid BIGINT,
	purchase_status bigint,
	renewal_date bigint,
	request_id bigint,
	round bigint,
	status int,
	times int,
	uid bigint,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT,
	CountryCode string,
	CityName string,
	AutonomousSystemOrganization string,
	AutonomousSystemNumber BIGINT,
	GeoHash string,
	StateCode string,
	Location map<string,float>
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

 LOAD DATA inpath '/mfc/bill/bill_renewal/*-parameter1' overwrite INTO TABLE mfc.bill_renewal_increment;

 INSERT overwrite table mfc.bill_renewal
SELECT *
FROM mfc.bill_renewal_increment
UNION all
SELECT total.*
FROM mfc.bill_renewal total
LEFT JOIN mfc.bill_renewal_increment inc ON total.id = inc.id
WHERE inc.id IS NULL;