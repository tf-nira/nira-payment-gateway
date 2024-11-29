\c payment_gateway

TRUNCATE TABLE pgateway.prn_tax_head cascade ;
\COPY pgateway.prn_tax_head (tax_head_code,tax_head_desc,tax_head_amount,currency,is_tax_head_valid,mosip_process,cr_by,cr_dtimes) FROM './dml/prn_tax_head_test.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE pgateway.prn_consumed cascade ;
\COPY pgateway.prn_consumed (prn,prn_data,is_prn_valid,cr_by,cr_dtimes) FROM './dml/prn_consumed_test.csv' delimiter ',' HEADER  csv;