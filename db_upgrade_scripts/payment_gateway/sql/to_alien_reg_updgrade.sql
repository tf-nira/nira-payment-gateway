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
(5,'IAID01','Issuance of an Alien ID','100','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(6,'RID004','Renewal of ID','100','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(7,'LID001','Replacement of a Lost ID','100','USD',true,'moonsglw',CURRENT_TIMESTAMP),
(8,'RDID003','Replacement of Defaced or Damaged ID','100','USD',true,'moonsglw',CURRENT_TIMESTAMP)

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
(5,'NEWAID','New Registration of Aliens','NEW',true,'IAID01','moonsglw',CURRENT_TIMESTAMP),
(6,'LOSTAID','Replacement of AIN Card','LOST',true,'LID001','moonsglw',CURRENT_TIMESTAMP),
(7,'DMGAID','Replacement of defaced or damaged identification card','LOST',true,'RDID003','moonsglw',CURRENT_TIMESTAMP),
(8,'RENAID','Renewal or Reactivation of AIN','RENEWAL',true,'RID004','moonsglw',CURRENT_TIMESTAMP)

ON CONFLICT (code) DO NOTHING;