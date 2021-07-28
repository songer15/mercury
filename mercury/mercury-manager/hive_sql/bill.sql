CREATE TABLE
IF NOT EXISTS mfc.bill_order (
	bind_date BIGINT,
	create_time BIGINT,
	did string,
	email string,
	final_price FLOAT,
	id BIGINT,
	last_modify_time BIGINT,
	order_date BIGINT,
	order_id string,
	payment_action INT,
	payment_amount FLOAT,
	payment_currency string,
	payment_gateway string,
	payment_id BIGINT,
	product_name string,
	STATUS INT,
	uid BIGINT,
	vendor_id BIGINT,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

CREATE TABLE
IF NOT EXISTS mfc.bill_order_increment (
	bind_date BIGINT,
	create_time BIGINT,
	did string,
	email string,
	final_price FLOAT,
	id BIGINT,
	last_modify_time BIGINT,
	order_date BIGINT,
	order_id string,
	payment_action INT,
	payment_amount FLOAT,
	payment_currency string,
	payment_gateway string,
	payment_id BIGINT,
	product_name string,
	STATUS INT,
	uid BIGINT,
	vendor_id BIGINT,
	LocalCreateTimestamps BIGINT,
	LocalCreateTime BIGINT
) ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;

 LOAD DATA inpath '/mfc/bill/bill_order_time/*-parameter1' overwrite INTO TABLE mfc.bill_order_increment;

INSERT overwrite table mfc.bill_order
SELECT *
FROM mfc.bill_order_increment
UNION all
SELECT total.*
FROM mfc.bill_order total
LEFT JOIN mfc.bill_order_increment inc ON total.id = inc.id
WHERE inc.id IS NULL;

 LOAD DATA inpath '/mfc/bill/bill_order_id/*-parameter1' overwrite INTO TABLE mfc.bill_order_increment;
 
INSERT overwrite table mfc.bill_order
SELECT *
FROM mfc.bill_order_increment
UNION all
SELECT total.*
FROM mfc.bill_order total
LEFT JOIN mfc.bill_order_increment inc ON total.id = inc.id
WHERE inc.id IS NULL;

-- CREATE TABLE IF NOT EXISTS report_forms.mfc_bill (cid string,actionType string, actionTime string, hiveTime BIGINT, totalCount INT, paymentAmount string, vendorId string);

-- INSERT INTO report_forms.mfc_recharge_card
-- SELECT 'bill' AS actionType,
--        CURRENT_TIMESTAMP AS hiveTime,
--                             COUNT(1) AS totalCount,
--                             a.payment_amount AS paymentAmount,
--                             a.vendor_id AS vendorId,
--                             a.times AS actionTime,
--                             concat(concat(a.times,a.vendor_id),a.payment_amount) AS cid
-- FROM
--   (SELECT payment_amount,
--           vendor_id,
--           from_unixtime(floor(order_date/1000),'yyyy-MM-dd') AS times
--    FROM mfc.bill_order
--    WHERE order_date BETWEEN parameter2 AND parameter3) a
-- GROUP BY a.times,
--          a.payment_amount,
--          a.vendor_id;


-- SELECT *
-- FROM report_forms.mfc_bill
-- WHERE hiveTime > parameter4;

