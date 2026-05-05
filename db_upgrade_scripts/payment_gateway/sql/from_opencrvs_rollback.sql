\c payment_gateway;

-- Remove payable_service_type entries
DELETE FROM pgateway.payable_service_type
WHERE code IN (
'BCNB6Y','BCN7Y18','BCNA18',
'CBDNAT','CCNAT','SRNAT','CNNAT','CDN1Y',
'BCFOR','CBDFOR','CCFOR','SRFOR',
'CNFCH','CNFAD','LRFBR','DCFOR','LRFDR'
);

-- Remove tax heads
DELETE FROM pgateway.prn_tax_head
WHERE tax_head_code IN (
'MON6001','7YR18YRS','ABOVE18YRS',
'CBD002','CPY01','SR01','CHNC01','DTHN01',
'BRTF','CBDC01','CCE001','SR001',
'CNC01','CNA01','LTBRT1','CD001','LRD01'
);