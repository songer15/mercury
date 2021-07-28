CREATE TABLE
IF NOT EXISTS mfc.discount_code (
	account_id bigint,
	account_type int,
	amount float,
	batch_number string,
	bill_invoice_id string,
	bill_status string,
	business_type int,
	create_time bigint,
	discount_code string,
	discount_code_id bigint,
	discount_type int,
	expire_end bigint,
	expire_start bigint,
	id bigint,
	last_modify_time bigint,
	product_name string,
	remark string,
	repeated_use int,
	status int,
	template_id bigint,
	type int,
	use_serial_number bigint,
	user_serial_number bigint,
	LocalCreateTime BIGINT,
	LocalCreateTimestamps BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc.discount_code_increment (
	account_id bigint,
	account_type int,
	amount float,
	batch_number string,
	bill_invoice_id string,
	bill_status string,
	business_type int,
	create_time bigint,
	discount_code string,
	discount_code_id bigint,
	discount_type int,
	expire_end bigint,
	expire_start bigint,
	id bigint,
	last_modify_time bigint,
	product_name string,
	remark string,
	repeated_use int,
	status int,
	template_id bigint,
	type int,
	use_serial_number bigint,
	user_serial_number bigint,
	LocalCreateTime BIGINT,
	LocalCreateTimestamps BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

LOAD DATA inpath '/mfc/bill/discount_code/*-parameter1' overwrite INTO TABLE mfc.discount_code_increment;

INSERT overwrite table mfc.discount_code
SELECT *
FROM mfc.discount_code_increment
UNION all
SELECT total.*
FROM mfc.discount_code total
LEFT JOIN mfc.discount_code_increment inc ON total.id = inc.id
WHERE inc.id IS NULL;
