\c payment_gateway 
TRUNCATE TABLE pgateway.prn_tax_head cascade ;
\COPY pgateway.prn_tax_head (tax_head_code,tax_head_desc,currency,is_tax_head_valid,mosip_process,tax_head_amount,cr_by,cr_dtimes) FROM './dml/prn_tax_head.csv' delimiter ',' HEADER  csv;