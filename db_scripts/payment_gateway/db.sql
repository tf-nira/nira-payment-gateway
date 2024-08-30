CREATE DATABASE payment_gateway
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE payment_gateway IS 'Payment gateway database to store the data that is captured as part of payment validation process';

\c payment_gateway 

DROP SCHEMA IF EXISTS pgateway CASCADE;
CREATE SCHEMA pgateway;
ALTER SCHEMA pgateway OWNER TO postgres;
ALTER DATABASE payment_gateway SET search_path TO pgateway,pg_catalog,public;