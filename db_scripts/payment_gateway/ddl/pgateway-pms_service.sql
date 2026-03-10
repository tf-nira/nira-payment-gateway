-- This table stores all data related to PMS partner services

CREATE TABLE pgateway.pms_service (
    id character varying(36) NOT NULL,
    service_desc character varying(256) NOT NULL,
    partner_type character varying(256) NOT NULL,
    partner_group character varying(256) NOT NULL,
    prn_tax_head_code character varying(256) NOT NULL,
    is_active boolean NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,

    CONSTRAINT pk_pms_service_id PRIMARY KEY (id),

    CONSTRAINT fk_pms_service_prn_tax_head_code 
        FOREIGN KEY (prn_tax_head_code) 
        REFERENCES pgateway.prn_tax_head(tax_head_code)
);

-- Comments on the table and its columns for clarity

COMMENT ON TABLE pgateway.pms_service IS 
'PMS Service: Stores partner-specific services allowed under PMS.';

COMMENT ON COLUMN pgateway.pms_service.id IS 
'Unique identifier for the record, stored as a UUID string.';

COMMENT ON COLUMN pgateway.pms_service.service_desc IS 
'Service Description: Human-readable description of the PMS service.';

COMMENT ON COLUMN pgateway.pms_service.partner_type IS 
'Partner Type: High-level classification of the partner.';

COMMENT ON COLUMN pgateway.pms_service.partner_group IS 
'Partner Group: Logical grouping of partners under the same category.';

COMMENT ON COLUMN pgateway.pms_service.prn_tax_head_code IS 
'PRN Tax Head Code: Foreign key referencing the tax head code associated with this PMS service.';

COMMENT ON COLUMN pgateway.pms_service.is_active IS 
'Is Active: Flag indicating whether the PMS service is currently active.';

COMMENT ON COLUMN pgateway.pms_service.cr_by IS 
'Created By: ID or name of the user who created the record.';

COMMENT ON COLUMN pgateway.pms_service.cr_dtimes IS 
'Created DateTimestamp: Date and time when the record was created.';

COMMENT ON COLUMN pgateway.pms_service.upd_by IS 
'Updated By: ID or name of the user who last updated the record.';

COMMENT ON COLUMN pgateway.pms_service.upd_dtimes IS 
'Updated DateTimestamp: Date and time when the record was last updated.';