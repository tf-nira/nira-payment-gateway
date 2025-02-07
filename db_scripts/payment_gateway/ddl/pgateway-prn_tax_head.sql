 -- This table saves all the data related to a prn tax heads.

CREATE TABLE pgateway.prn_tax_head(
	id character varying(36) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	currency character varying(256) NOT NULL,
	is_active boolean NOT NULL,
	tax_head_amount character varying(256) NOT NULL,
	tax_head_code character varying(256) NOT NULL,
	tax_head_desc character varying(256) NOT NULL,
	upd_by character varying(256),
    upd_dtimes timestamp,

	CONSTRAINT pk_prn_tax_head_id PRIMARY KEY (id),
	CONSTRAINT uq_prn_tax_head_code UNIQUE (tax_head_code)
);

-- Comments on the table and its columns for clarity
COMMENT ON TABLE pgateway.prn_tax_head IS 'PRN Tax Head: Stores details of a PRN tax heads for NIRA.';
COMMENT ON COLUMN pgateway.prn_tax_head.id IS 'Unique identifier for the record, stored as a UUID string.';
COMMENT ON COLUMN pgateway.prn_tax_head.cr_by IS 'Created By: ID or name of the user who created the record.';
COMMENT ON COLUMN pgateway.prn_tax_head.cr_dtimes IS 'Created DateTimestamp: Date and time when the record was created.';
COMMENT ON COLUMN pgateway.prn_tax_head.currency IS 'Currency: The currency code (e.g., USD, UGX) associated with the tax head.';
COMMENT ON COLUMN pgateway.prn_tax_head.is_active IS 'Is Active: Flag to indicate whether the tax head is currently active.';
COMMENT ON COLUMN pgateway.prn_tax_head.tax_head_amount IS 'Tax Head Amount: Amount associated with the tax head (stored as a string for flexibility).';
COMMENT ON COLUMN pgateway.prn_tax_head.tax_head_code IS 'Tax Head Code: Unique code identifying the tax head.';
COMMENT ON COLUMN pgateway.prn_tax_head.tax_head_desc IS 'Tax Head Description: A brief description of the tax head.';
COMMENT ON COLUMN pgateway.prn_tax_head.upd_by IS 'Updated By: ID or name of the user who last updated the record.';
COMMENT ON COLUMN pgateway.prn_tax_head.upd_dtimes IS 'Updated DateTimestamp: Date and time when the record was last updated.';
