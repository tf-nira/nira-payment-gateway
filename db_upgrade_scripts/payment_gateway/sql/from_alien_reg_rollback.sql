\c payment_gateway;

-- Remove payable_service_type entries
DELETE FROM pgateway.payable_service_type
WHERE code IN (
    'NEWAID','LOSTAID','DMGAID','RENAID'
);

-- Remove tax heads
DELETE FROM pgateway.prn_tax_head
WHERE tax_head_code IN (
    'IAID01','RID004','LID001','RDID003'
);