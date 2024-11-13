 -- This table saves all the data related to a prn tax heads.

CREATE TABLE pgateway.prn_tax_head(
	prn_tax_head_id bigint NOT NULL generated always as identity,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	currency character varying(255) NOT NULL,
	is_tax_head_valid boolean NOT NULL,
	mosip_process character varying(255) NOT NULL,
	tax_head_amount character varying(255) NOT NULL,
	tax_head_code character varying(255) NOT NULL,
	tax_head_desc character varying(255) NOT NULL,
	upd_by character varying(255),
    upd_dtimes timestamp,

	CONSTRAINT pk_prn_tax_head_id PRIMARY KEY (prn_tax_head_id)
);

COMMENT ON TABLE pgateway.prn_tax_head IS 'PRN Tax Head: Stores details of a PRN tax heads for NIRA.';
