\c payment_gateway


---CREATE EXTENSION IF NOT EXISTS "pgcrypto";



INSERT INTO pgateway.prn_tax_head (
	id,
    tax_head_code,
    tax_head_desc,
    tax_head_amount,
    currency,
    is_active,
    cr_by,
    cr_dtimes
)
VALUES
(9,'RECO090','Access - Gov - 0 - 9 Records','10000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(10,'RECO010','Access - Gov - Above 10 Records','500','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(11,'TAIL010','Access - Gov - Tailored','350000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),

(12,'RECO0090','Access - Private - 0 - 9 Records','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(13,'RECORD100','Access - Private - Above 10 Records','500','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(14,'TAIL012','Access - Private - Tailored','350000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),

(15,'VRECORD113','Verification - Gov - 0 - 9 Records','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(16,'VRECORD114','Verification - Gov - Above 10 Records','100','UGX',true,'moonsglw',CURRENT_TIMESTAMP),

(17,'VRECORD111','Verification - Private - 0 - 9 Records','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(18,'VRECORD112','Verification - Private - Above 10 Records','100','UGX',true,'moonsglw',CURRENT_TIMESTAMP),

(19,'AUI09','Foreigners - Access - 0 - 9 Records','10','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(20,'AUI5M','Foreigners - Access - 1 - 5M','12','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(21,'AUI10M','Foreigners - Access - 5M - 10M','10','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(22,'AUI15M','Foreigners - Access - 10M - 15M','8','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(23,'AOI20M','Foreigners - Access - 15M - 20M','6','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(24,'AUI25M','Foreigners - Access - 20M - 25M','5','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(25,'AUI30M','Foreigners - Access - 25M - 30M','4','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(26,'AUI35M','Foreigners - Access - 30M - 35M','3','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(27,'AUIE35M','Foreigners - Access - Exceed 35M','2','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(28,'STAT011','Foreigners - Access - Tailored','200','USD',true,'moonsglw',CURRENT_TIMESTAMP)

ON CONFLICT (tax_head_code) DO NOTHING;

CREATE TABLE IF NOT EXISTS pgateway.pms_service (
    id character varying(36) NOT NULL,
    service_desc character varying(256) NOT NULL,
    partner_type character varying(256) NOT NULL,
    partner_group character varying(256) NOT NULL,
    prn_tax_head_code character varying(256) NOT NULL,
    is_active boolean NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,

    CONSTRAINT pk_pms_service_id PRIMARY KEY (id),

    CONSTRAINT fk_pms_service_prn_tax_head_code 
        FOREIGN KEY (prn_tax_head_code) 
        REFERENCES pgateway.prn_tax_head(tax_head_code)
);

CREATE INDEX IF NOT EXISTS idx_pms_service_tax_head
    ON pgateway.pms_service (prn_tax_head_code);

CREATE INDEX IF NOT EXISTS idx_pms_service_partner
    ON pgateway.pms_service (partner_type, partner_group);


INSERT INTO pgateway.pms_service (
    id,
    service_desc,
    partner_type,
    partner_group,
    prn_tax_head_code,
    is_active,
    cr_by,
    cr_dtimes
)
VALUES
(1,'Access and Use - Gov','ACCESS','GOV','RECO010',true,'moonsglw',CURRENT_TIMESTAMP),
(2,'Access and Use - Private','ACCESS','PRIVATE','RECORD100',true,'moonsglw',CURRENT_TIMESTAMP),
(3,'Access and Use - Foreign','ACCESS','FOREIGN','AUI5M',true,'moonsglw',CURRENT_TIMESTAMP),
(4,'Verify - Gov','VERIFY','GOV','VRECORD114',true,'moonsglw',CURRENT_TIMESTAMP),
(5,'Verify - Private','VERIFY','PRIVATE','VRECORD112',true,'moonsglw',CURRENT_TIMESTAMP)

ON CONFLICT DO NOTHING;