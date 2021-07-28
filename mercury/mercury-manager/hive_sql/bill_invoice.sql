CREATE TABLE
IF NOT EXISTS mfc.bill_invoice (
	account_type int,
	action int,
	amount float,
	cid bigint,
	code_id bigint,
	compare int,
	create_time bigint,
	currency string,
	dc_type int,
	device_limit int,
	discount float,
	discount_by_code float,
	discount_by_rest_ts float,
	discount_code string,
	email string,
	final_price float,
	generate_type string,
	iid bigint,
	installments string,
	invoice_date bigint,
	last_modify_ip string,
	last_modify_time bigint,
	last_payment_amount float,
	last_payment_code string,
	last_payment_currency string,
	last_payment_date bigint,
	last_payment_gatway string,
	last_payment_id bigint,
	last_payment_message string,
	last_payment_status int,
	order_id string,
	origin_order_id string,
	payment_client string,
	payment_currency string,
	payment_final_price float,
	payment_source string,
	pid bigint,
	pre_product int,
	product_name string,
	promotion_discount float,
	promotion_id bigint,
	renew_times bigint,
	rid bigint,
	service_days int,
	sid bigint,
	status int,
	uid bigint,
	unbind bigint,
	upgrade int,
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
IF NOT EXISTS mfc.bill_invoice_increment (
	account_type int,
	action int,
	amount float,
	cid bigint,
	code_id bigint,
	compare int,
	create_time bigint,
	currency string,
	dc_type int,
	device_limit int,
	discount float,
	discount_by_code float,
	discount_by_rest_ts float,
	discount_code string,
	email string,
	final_price float,
	generate_type string,
	iid bigint,
	installments string,
	invoice_date bigint,
	last_modify_ip string,
	last_modify_time bigint,
	last_payment_amount float,
	last_payment_code string,
	last_payment_currency string,
	last_payment_date bigint,
	last_payment_gatway string,
	last_payment_id bigint,
	last_payment_message string,
	last_payment_status int,
	order_id string,
	origin_order_id string,
	payment_client string,
	payment_currency string,
	payment_final_price float,
	payment_source string,
	pid bigint,
	pre_product int,
	product_name string,
	promotion_discount float,
	promotion_id bigint,
	renew_times bigint,
	rid bigint,
	service_days int,
	sid bigint,
	status int,
	uid bigint,
	unbind bigint,
	upgrade int,
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

LOAD DATA inpath '/mfc/bill/bill_invoice/*-parameter1' overwrite INTO TABLE mfc.bill_invoice_increment;

INSERT overwrite table mfc.bill_invoice
SELECT *
FROM mfc.bill_invoice_increment
UNION all
SELECT total.*
FROM mfc.bill_invoice total
LEFT JOIN mfc.bill_invoice_increment inc ON total.iid = inc.iid
WHERE inc.iid IS NULL;

LOAD DATA inpath '/mfc/bill/bill_invoice_fix/*-parameter1' overwrite INTO TABLE mfc.bill_invoice_increment;

INSERT overwrite table mfc.bill_invoice
SELECT *
FROM mfc.bill_invoice_increment
UNION all
SELECT total.*
FROM mfc.bill_invoice total
LEFT JOIN mfc.bill_invoice_increment inc ON total.iid = inc.iid
WHERE inc.iid IS NULL;