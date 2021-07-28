CREATE TABLE
IF NOT EXISTS mfc.recharge_card_info (
	transaction_id string,
	reason string,
	charge_type INT,
	biz_sys_type INT,
	create_time BIGINT,
	batch_number string,
	last_modify_time BIGINT,
	LocalCreateTime BIGINT,
	biz_type INT,
	used_id string,
	biz_value INT,
	TYPE string,
	cc_alias string,
	PASSWORD BIGINT,
	expire_ts BIGINT,
	biz_sys_group string,
	id BIGINT,
	LocalCreateTimestamps BIGINT,
	used_ts BIGINT,
	remarks string,
	sellers string,
	properties BIGINT,
	STATUS INT
) COMMENT "recharge_card_info" ROW format serde 'org.openx.data.jsonserde.JsonSerDe' stored AS textfile;



 LOAD DATA inpath '/mfc/activation/recharge_card_info/*-parameter1' overwrite INTO TABLE mfc.recharge_card_info;


CREATE TABLE IF NOT EXISTS report_forms.mfc_activation_recharge_card (cid string,actionType string, actionTime string, hiveTime BIGINT, totalCount INT, bizValue string) COMMENT "mfc_recharge_card";


INSERT INTO report_forms.mfc_activation_recharge_card
SELECT concat(a.times,a.biz_value) AS cid,
       'recharge_card' AS actionType,
       a.times AS actionTime,
       unix_timestamp()*1000 AS hiveTime,
                            COUNT(1) AS totalCount,
                            a.biz_value AS bizValue
FROM
  (SELECT biz_value,
          from_unixtime(floor(used_ts/1000),'yyyy-MM-dd') AS times
   FROM mfc.recharge_card_info
   WHERE used_ts BETWEEN parameter2 AND parameter3) a
GROUP BY a.times,
         a.biz_value;


SELECT *
FROM report_forms.mfc_activation_recharge_card
WHERE hiveTime > parameter4;