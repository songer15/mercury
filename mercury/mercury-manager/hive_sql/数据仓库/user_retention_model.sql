CREATE TABLE
IF NOT EXISTS mfc_dw.retention_email_module (
	uid string,
	loginId string, 
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string,
	type string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;


CREATE TABLE
IF NOT EXISTS mfc_dw.retention_mp_module (
	uid string,
	loginId string, 
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string,
	type string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_dw.retention_account_card_module (
	uid string,
	loginId string,
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string,
	type string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_dw.retention_device_module (
	uid string,
	loginId string, 
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string,
	type string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_dw.retention_module_inc (
	uid string,
	loginId string, 
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	invoice_date bigint,
	pid int,
	vendor string,
	row_number_last int,
	row_number_first int
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_dw.retention_device_inc (
	custom_id string,
	did string,
	device_type string,
	vendor string,
	device_create_time bigint,
	bind_uid string,
	bind_date bigint,
	bind_type string,
	loginId string, 
	reg_date bigint,
	create_time bigint,
	expire_ts bigint,
	reg_client_ip string,
	reg_countrycode string,
	trial int,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc_app.retention_device (
	did string,
	device_type string,
	vendor string,
	expire_ts bigint,
	reg_countrycode string,
	first_payment_type string,
	first_payment_time bigint,
	first_payment_product_id int,
	first_payment_vendor string,
	last_payment_type string,
	last_payment_time bigint,
	last_payment_product_id int,
	last_payment_vendor string
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS report_forms.mfc_retention_device_analyse (
	custom_id string,
	device_type string,
	vendor string,
	reg_countrycode string,
	first_payment_type string,
	first_payment_product_id int,
	first_payment_vendor string,
	enter_date string,
	metric_date string,
	user_count bigint
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

INSERT overwrite TABLE mfc_dw.retention_module_inc SELECT
	*
FROM
	(
		SELECT
			a.uid,
			a.email AS loginId,
			a.reg_date,
			a.create_time,
			a.expire_ts,
			a.reg_client_ip,
			a.countrycode AS reg_countrycode,
			a.trial,
			b.invoice_date,
			b.pid,
			b.vendor,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date DESC
			) AS row_number_last,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date ASC
			) AS row_number_first
		FROM
			mfc.account a
		JOIN (
			SELECT
				cast(b1.uid as string),
				b1.invoice_date,
				b1.pid,
				'invoice' AS vendor
			FROM
				mfc.bill_invoice b1
			WHERE
				b1. STATUS = 3 and b1.account_type=1
			UNION all
				SELECT
					split(b2.used_id, '-') [ 1 ] AS uid,
					b2.used_ts AS invoice_date,
					b2.biz_value AS pid,
					'recharge_card' AS vendor
				FROM
					mfc.recharge_card_info b2
				WHERE
					b2.used_id IS NOT NULL
				AND split (b2.used_id, '-') [ 0 ] = '1'
		) b ON cast(a.uid as string) = b.uid
		WHERE
			a.type IN (0, 3)
		AND b.uid IS NOT NULL
	) a
WHERE
	a.row_number_last = 1
OR a.row_number_first = 1;

INSERT overwrite TABLE mfc_dw.retention_email_module SELECT
	first.uid,
	first.loginId,
	first.reg_date,
	first.create_time,
	first.expire_ts,
	first.reg_client_ip,
	first.reg_countrycode,
	first.trial,
	first.invoice_date AS first_payment_time,
	first.pid AS first_payment_product_id,
	first.vendor AS first_payment_vendor,
	last.invoice_date as last_payment_time,
	last.pid as last_payment_product_id,
	last.vendor as last_payment_vendor,
	'email' AS type
FROM
	mfc_dw.retention_module_inc first
JOIN mfc_dw.retention_module_inc last ON first.uid = last.uid
WHERE
	first.row_number_first = 1
AND last.row_number_last = 1;

INSERT overwrite TABLE mfc_dw.retention_module_inc SELECT
	*
FROM
	(
		SELECT
			a.uid,
			a.login_id AS loginId,
			a.reg_date,
			a.create_time,
			a.expire_ts,
			a.reg_client_ip,
			a.countrycode AS reg_countrycode,
			a.trial,
			b.invoice_date,
			b.pid,
			b.vendor,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date DESC
			) AS row_number_last,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date ASC
			) AS row_number_first
		FROM
			mfc.account a
		JOIN (
			SELECT
				cast(b1.uid as string),
				b1.invoice_date,
				b1.pid,
				'invoice' AS vendor
			FROM
				mfc.bill_invoice b1
			WHERE
				b1. STATUS = 3 and b1.account_type=1
			UNION all
				SELECT
					split(b2.used_id, '-') [ 1 ] AS uid,
					b2.used_ts AS invoice_date,
					b2.biz_value AS pid,
					'recharge_card' AS vendor
				FROM
					mfc.recharge_card_info b2
				WHERE
					b2.used_id IS NOT NULL
				AND split (b2.used_id, '-') [ 0 ] = '1'
		) b ON cast(a.uid as string) = b.uid
		WHERE
			a.type IN (2, 4)
		AND b.uid IS NOT NULL
	) a
WHERE
	a.row_number_last = 1
OR a.row_number_first = 1;

INSERT overwrite TABLE mfc_dw.retention_mp_module SELECT
	first.uid,
	first.loginId,
	first.reg_date,
	first.create_time,
	first.expire_ts,
	first.reg_client_ip,
	first.reg_countrycode,
	first.trial,
	first.invoice_date AS first_payment_time,
	first.pid AS first_payment_product_id,
	first.vendor AS first_payment_vendor,
	last.invoice_date as last_payment_time,
	last.pid as last_payment_product_id,
	last.vendor as last_payment_vendor,
	'mp' AS type
FROM
	mfc_dw.retention_module_inc first
JOIN mfc_dw.retention_module_inc last ON first.uid = last.uid
WHERE
	first.row_number_first = 1
AND last.row_number_last = 1;

INSERT overwrite TABLE mfc_dw.retention_module_inc SELECT
	*
FROM
	(
		SELECT
			a.uid,
			a.user_name AS loginId,
			a.reg_date,
			a.create_time,
			a.expire_ts,
			a.reg_client_ip,
			a.countrycode AS reg_countrycode,
			a.trial,
			b.invoice_date,
			b.pid,
			b.vendor,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date DESC
			) AS row_number_last,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date ASC
			) AS row_number_first
		FROM
			mfc.account_card a
		JOIN (
			SELECT
				cast(b1.uid as string),
				b1.reg_date as invoice_date,
				b1.product_id as pid,
				'account_card' AS vendor
			FROM
				mfc.account_card b1
			UNION all
				SELECT
					split(b2.used_id, '-') [ 1 ] AS uid,
					b2.used_ts AS invoice_date,
					b2.biz_value AS pid,
					'recharge_card' AS vendor
				FROM
					mfc.recharge_card_info b2
				WHERE
					b2.used_id IS NOT NULL
				AND split (b2.used_id, '-') [ 0 ] = '1'
		) b ON cast(a.uid as string) = b.uid
		WHERE b.uid IS NOT NULL
	) a
WHERE
	a.row_number_last = 1
OR a.row_number_first = 1;

INSERT overwrite TABLE mfc_dw.retention_account_card_module SELECT
	first.uid,
	first.loginId,
	first.reg_date,
	first.create_time,
	first.expire_ts,
	first.reg_client_ip,
	first.reg_countrycode,
	first.trial,
	first.invoice_date AS first_payment_time,
	first.pid AS first_payment_product_id,
	first.vendor AS first_payment_vendor,
	last.invoice_date as last_payment_time,
	last.pid as last_payment_product_id,
	last.vendor as last_payment_vendor,
	'account_card' AS type
FROM
	mfc_dw.retention_module_inc first
JOIN mfc_dw.retention_module_inc last ON first.uid = last.uid
WHERE
	first.row_number_first = 1
AND last.row_number_last = 1;


INSERT overwrite TABLE mfc_dw.retention_module_inc SELECT
	*
FROM
	(
		SELECT
			a.uid,
			a.did AS loginId,
			a.act_date as reg_date,
			a.create_time,
			a.expire_ts,
			split(a.region, '-') [ 1 ] as reg_client_ip,
			split(a.region, '-') [ 0 ] as reg_countrycode,
			null,
			b.invoice_date,
			b.pid,
			b.vendor,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date DESC
			) AS row_number_last,
			row_number () OVER (
				PARTITION BY a.uid
				ORDER BY
					b.invoice_date ASC
			) AS row_number_first
		FROM
			mfc.device_user a
		JOIN (
			SELECT
				cast(b1.uid as string),
				b1.invoice_date,
				b1.pid,
				'invoice' AS vendor
			FROM
				mfc.bill_invoice b1
			WHERE
				b1. STATUS = 3 and b1.account_type=0
			UNION all
				SELECT
					split(b2.used_id, '-') [ 1 ] AS uid,
					b2.used_ts AS invoice_date,
					b2.biz_value AS pid,
					'recharge_card' AS vendor
				FROM
					mfc.recharge_card_info b2
				WHERE
					b2.used_id IS NOT NULL
				AND split (b2.used_id, '-') [ 0 ] = '0'
		) b ON cast(a.uid as string) = b.uid
		WHERE
			a.act_date > 0
		AND b.uid IS NOT NULL
	) a
WHERE
	a.row_number_last = 1
OR a.row_number_first = 1;

INSERT overwrite TABLE mfc_dw.retention_device_module SELECT
	first.uid,
	first.loginId,
	first.reg_date,
	first.create_time,
	first.expire_ts,
	first.reg_client_ip,
	first.reg_countrycode,
	first.trial,
	first.invoice_date AS first_payment_time,
	first.pid AS first_payment_product_id,
	first.vendor AS first_payment_vendor,
	last.invoice_date as last_payment_time,
	last.pid as last_payment_product_id,
	last.vendor as last_payment_vendor,
	'device' AS type
FROM
	mfc_dw.retention_module_inc first
JOIN mfc_dw.retention_module_inc last ON first.uid = last.uid
WHERE
	first.row_number_first = 1
AND last.row_number_last = 1;

INSERT overwrite TABLE mfc_dw.retention_device_inc (
	SELECT
		a.did AS custom_id,
		a.did,
		'box' AS device_type,
		a.vendor_id AS vendor,
		a.create_time AS device_create_time,
		a.uid AS bind_uid,
		a.act_date AS bind_date,
		b.type AS bind_type,
		b.loginId,
		b.reg_date,
		b.create_time,
		b.expire_ts,
		b.reg_client_ip,
		b.reg_countrycode,
		b.trial,
		b.first_payment_time,
		b.first_payment_product_id,
		b.first_payment_vendor,
		b.last_payment_time,
		b.last_payment_product_id,
		b.last_payment_vendor
	FROM
		mfc.device_user a
	JOIN mfc_dw.retention_device_module b ON a.uid = b.uid
	WHERE
		a.uid IS NOT NULL
UNION ALL
	SELECT
		a.custom_id,
		a.did,
		CASE a.client_type
		WHEN 0 THEN
			'unknown'
		WHEN 2 THEN
			'mobile'
		WHEN 3 THEN
			'box'
		ELSE
			'error'
		END AS device_type,
		a.vendor_id AS vendor,
		a.create_time AS device_create_time,
		a.uid AS bind_uid,
		a.binding_date AS bind_date,
		b.type AS bind_type,
		b.loginId,
		b.reg_date,
		b.create_time,
		b.expire_ts,
		b.reg_client_ip,
		b.reg_countrycode,
		b.trial,
		b.first_payment_time,
		b.first_payment_product_id,
		b.first_payment_vendor,
		b.last_payment_time,
		b.last_payment_product_id,
		b.last_payment_vendor
	FROM (
		select *,
			row_number () OVER (
				PARTITION BY uid
				ORDER BY
					binding_date asc
			) AS row_number 
		from mfc.email_device_binding where length(did) > 0) a
	JOIN (
		SELECT * FROM mfc_dw.retention_email_module
			UNION ALL
		SELECT * FROM mfc_dw.retention_mp_module
	    	UNION ALL
		SELECT * FROM mfc_dw.retention_account_card_module
	) b ON a.uid = b.uid
	WHERE
   	    a.row_number = 1
);

INSERT overwrite table mfc_dw.retention_device_inc
SELECT 
	a.custom_id,
	a.did,
	case a.device_type
	WHEN 'unknown' THEN
			b.device
		WHEN 'error' THEN
			b.device
		ELSE
			a.device_type
		END 
	AS device_type,
	a.vendor,
	a.device_create_time,
	a.bind_uid,
	a.bind_date,
	a.bind_type,
	a.loginId,
	a.reg_date,
	a.create_time,
	a.expire_ts,
	a.reg_client_ip,
	a.reg_countrycode,
	a.trial,
	a.first_payment_time,
	a.first_payment_product_id,
	a.first_payment_vendor,
	a.last_payment_time,
	a.last_payment_product_id,
	a.last_payment_vendor
FROM mfc_dw.retention_device_inc a 
left JOIN mfc_dw.current_device_user_info b ON a.did = b.did;

INSERT overwrite TABLE mfc_app.retention_device (
	SELECT
		a.did,
		a.device_type,
		a.vendor,
		a.expire_ts,
		a.reg_countrycode,
		a.bind_type AS first_payment_type,
		a.first_payment_time,
		a.first_payment_product_id,
		a.first_payment_vendor,
		b.bind_type AS last_payment_type,
		b.last_payment_time,
		b.last_payment_product_id,
		b.last_payment_vendor
	FROM
		(
			SELECT
				*, row_number () OVER (
					PARTITION BY did
					ORDER BY
						first_payment_time ASC
				) AS row_number_first
			FROM
				mfc_dw.retention_device_inc
		) a
	JOIN (
		SELECT
			*, row_number () OVER (
				PARTITION BY did
				ORDER BY
					last_payment_time DESC
			) AS row_number_last
		FROM
			mfc_dw.retention_device_inc
	) b ON a.custom_id = b.custom_id
	WHERE
		a.row_number_first = 1
	AND b.row_number_last = 1
);


INSERT overwrite TABLE report_forms.mfc_retention_device_analyse SELECT
	concat(
		from_unixtime(
			floor(
				max(first_payment_time) / 1000
			)+28800,
			'yyyy-MM'
		),
		'_',
		from_unixtime(
			floor(#expireTime / 1000),
			'yyyy-MM-dd'
		),regexp_replace(reflect("java.util.UUID", "randomUUID"), "-", "")
	) AS custom_id,
	device_type,
	vendor,
	reg_countrycode,
	first_payment_type,
	first_payment_product_id,
	first_payment_vendor,
	from_unixtime(
		floor(
			max(first_payment_time) / 1000
		)+28800,
		'yyyy-MM'
	) AS enter_date,
	from_unixtime(
		floor(#expireTime / 1000),
		'yyyy-MM-dd'
	) AS metric_date,
	count(1) AS user_count
FROM
	mfc_app.retention_device
WHERE
	expire_ts > #expireTime
and first_payment_time < #expireTime
GROUP BY
	device_type,
	vendor,
	reg_countrycode,
	first_payment_type,
	first_payment_product_id,
	first_payment_vendor,
	from_unixtime(
		floor(first_payment_time / 1000)+28800,
		'yyyy-MM'
	)
UNION ALL
select * from report_forms.mfc_retention_device_analyse 
where 
	metric_date != from_unixtime(
		floor(#expireTime / 1000),
		'yyyy-MM-dd'
	)

	-- (
	-- 	SELECT
	-- 		b.*
	-- 	FROM
	-- 		(
	-- 			SELECT
	-- 				device_type,
	-- 				vendor,
	-- 				reg_countrycode,
	-- 				first_payment_type,
	-- 				first_payment_product_id,
	-- 				first_payment_vendor,
	-- 				from_unixtime(
	-- 					floor(
	-- 						max(first_payment_time) / 1000
	-- 					),
	-- 					'yyyy-MM'
	-- 				) AS enter_date,
	-- 				from_unixtime(
	-- 					floor(#parameter / 1000),
	-- 					'yyyy-MM'
	-- 				) AS metric_date,
	-- 				count(1) AS user_count
	-- 			FROM
	-- 				mfc_app.retention_device
	-- 			WHERE
	-- 				expire_ts > #parameter
	-- 			GROUP BY
	-- 				device_type,
	-- 				vendor,
	-- 				reg_countrycode,
	-- 				first_payment_type,
	-- 				first_payment_product_id,
	-- 				first_payment_vendor,
	-- 				from_unixtime(
	-- 					floor(first_payment_time / 1000),
	-- 					'yyyy-MM'
	-- 				)
	-- 		) a
	-- 	RIGHT JOIN report_forms.mfc_retention_device_analyse b ON concat(a.device_type,
	-- 		a.vendor,
	-- 		a.reg_countrycode,
	-- 		a.first_payment_type,
	-- 		a.first_payment_product_id,
	-- 		a.first_payment_vendor,
	-- 		a.enter_date,
	-- 		'_',
	-- 		a.metric_date
	-- 	) = b.custom_id
	-- 	WHERE
	-- 		concat(a.device_type,a.vendor,a.reg_countrycode,
	-- 			a.first_payment_type,
	-- 			a.first_payment_product_id,
	-- 			a.first_payment_vendor,
	-- 			a.enter_date,
	-- 			'_',
	-- 			a.metric_date
	-- 		) IS NULL
	-- )

-- SELECT
-- 	*
-- FROM
-- 	mfc_app.retention_device
-- JOIN (
-- 	SELECT
-- 		date_add(start_date, pos) AS metric_date
-- 	FROM
-- 		(
-- 			SELECT
-- 				'2017-12-14' AS start_date,
-- 				from_unixtime(
-- 					unix_timestamp(),
-- 					'yyyy-MM-dd'
-- 				) AS end_date
-- 		) tmp lateral VIEW posexplode (
-- 			split (
-- 				space(
-- 					datediff(end_date, start_date)
-- 				),
-- 				''
-- 			)
-- 		) t AS pos,
-- 		val
-- ) a ON a.metric_date = from_unixtime(
-- 	floor(first_payment_time / 1000),
-- 	'yyyy-MM-dd'
-- )

-- CREATE EXTERNAL TABLE report_forms.hive_mfc_retention_device_analyse (
-- 	custom_id string,
-- 	device_type string,
-- 	vendor string,
-- 	reg_countrycode string,
-- 	first_payment_type string,
-- 	first_payment_product_id int,
-- 	first_payment_vendor string,
-- 	enter_date string,
-- 	metric_date string,
-- 	user_count bigint
-- ) STORED BY 'org.elasticsearch.hadoop.hive.EsStorageHandler' TBLPROPERTIES (  
--     'es.resource' = 'hive_mfc_retention_device_analyse',  
--     'es.index.auto.create' = 'true',  
-- 	'es.index.read.missing.as.empty' = 'true',
-- 	'es.mapping.id' = 'custom_id',
--     'es.nodes' = '172.16.0.201:9200,172.16.0.202:9200,172.16.0.204:9200'
-- );

-- INSERT overwrite TABLE report_forms.hive_mfc_retention_device_analyse SELECT
-- * from report_forms.mfc_retention_device_analyse
