\c payment_gateway

TRUNCATE TABLE pgateway.prn_tax_head cascade ;
\COPY pgateway.prn_tax_head (tax_head_code,tax_head_desc,tax_head_amount,currency,is_active,cr_by,cr_dtimes) FROM './dml/prn_tax_head.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pgateway.payable_service_type cascade ;
\COPY pgateway.payable_service_type (code,service_type_desc,mosip_process,is_active,prn_tax_head_code,cr_by,cr_dtimes) FROM './dml/payable_service_type.csv' delimiter ',' HEADER  csv;