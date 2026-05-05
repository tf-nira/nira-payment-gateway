\c payment_gateway;

-- Insert into prn_tax_head
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
(30,'MON6001','Birth Certificate - National - 7 months to 6 years','10000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(31,'7YR18YRS','Birth Certificate - National - 7 years to 18 years','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(32,'ABOVE18YRS','Birth Certificate - National - Above 18 years','50000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(33,'CBD002','Certification of Birth or Death Certificate - National','1000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(34,'CPY01','Certified Copy of Entry Birth or Death - National','1000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(35,'SR01','Search in the Register for Birth or Death - National','1000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(36,'CHNC01','Change of Name of a child of 6years and above - National','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(37,'DTHN01','Certificate of Death After 1year of Occurrence - National','20000','UGX',true,'moonsglw',CURRENT_TIMESTAMP),
(38,'BRTF','Certificate of Birth - Foreigner','40','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(39,'CBDC01','Certification of Birth or Death Certificate - Foriegner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(40,'CCE001','Certified Copy of Entry Birth or Death - Foreigner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(41,'SR001','Search in the Register for Birth or Death - Foreigner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(42,'CNC01','Change of Name of a Child - Foreigner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(43,'CNA01','Change of Name of an Adult - Foreigner','70','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(44,'LTBRT1','Late Registration of Birth - Foreigner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(45,'CD001','Certificate of Death - Foreigner','40','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(46,'LRD01','Late Registration of Death - Foreigner','20','USD',true,'moonsglw',CURRENT_TIMESTAMP)

ON CONFLICT (tax_head_code) DO NOTHING;

-- Insert into payable_service_type
INSERT INTO pgateway.payable_service_type (
    id,
    code,
    service_type_desc,
    mosip_process,
    is_active,
    prn_tax_head_code,
    cr_by,
    cr_dtimes
)
VALUES
(9,'BCNB6Y','Birth Certificate - National - 7 months to 6 years','OPENCRVS',true,'MON6001','moonsglw',CURRENT_TIMESTAMP),
(10,'BCN7Y18','Birth Certificate - National - 7 years to 18 years','OPENCRVS',true,'7YR18YRS','moonsglw',CURRENT_TIMESTAMP),
(11,'BCNA18','Birth Certificate - National - Above 18 years','OPENCRVS',true,'ABOVE18YRS','moonsglw',CURRENT_TIMESTAMP),
(12,'CBDNAT','Certification of Birth or Death Certificate - National','OPENCRVS',true,'CBD002','moonsglw',CURRENT_TIMESTAMP),
(13,'CCNAT','Certified Copy of Entry Birth or Death - National','OPENCRVS',true,'CPY01','moonsglw',CURRENT_TIMESTAMP),
(14,'SRNAT','Search in the Register for Birth or Death - National','OPENCRVS',true,'SR01','moonsglw',CURRENT_TIMESTAMP),
(15,'CNNAT','Change of Name of a child of 6years and above - National','OPENCRVS',true,'CHNC01','moonsglw',CURRENT_TIMESTAMP),
(16,'CDN1Y','Certificate of Death After 1year of Occurrence - National','OPENCRVS',true,'DTHN01','moonsglw',CURRENT_TIMESTAMP),
(17,'BCFOR','Certificate of Birth - Foreigner','OPENCRVS',true,'BRTF','moonsglw',CURRENT_TIMESTAMP),
(18,'CBDFOR','Certification of Birth or Death Certificate - Foriegner','OPENCRVS',true,'CBDC01','moonsglw',CURRENT_TIMESTAMP),
(19,'CCFOR','Certified Copy of Entry Birth or Death - Foreigner','OPENCRVS',true,'CCE001','moonsglw',CURRENT_TIMESTAMP),
(20,'SRFOR','Search in the Register for Birth or Death - Foreigner','OPENCRVS',true,'SR001','moonsglw',CURRENT_TIMESTAMP),
(21,'CNFCH','Change of Name of a Child - Foreigner','OPENCRVS',true,'CNC01','moonsglw',CURRENT_TIMESTAMP),
(22,'CNFAD','Change of Name of an Adult - Foreigner','OPENCRVS',true,'CNA01','moonsglw',CURRENT_TIMESTAMP),
(23,'LRFBR','Late Registration of Birth - Foreigner','OPENCRVS',true,'LTBRT1','moonsglw',CURRENT_TIMESTAMP),
(24,'DCFOR','Certificate of Death - Foreigner','OPENCRVS',true,'CD001','moonsglw',CURRENT_TIMESTAMP),
(25,'LRFDR','Late Registration of Death - Foreigner','OPENCRVS',true,'LRD01','moonsglw',CURRENT_TIMESTAMP)

ON CONFLICT (code) DO NOTHING;