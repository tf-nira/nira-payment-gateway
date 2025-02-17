-- This table stores all data related to payable service types for reference

CREATE TABLE pgateway.payable_service_type (
    id character varying(36) NOT NULL,
    service_type_desc character varying(256) NOT NULL,
    code character varying(256) NOT NULL,
    is_active boolean NOT NULL,
    mosip_process character varying(256) NOT NULL,
    prn_tax_head_code character varying(256),
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,

    CONSTRAINT pk_payable_service_type_id PRIMARY KEY (id),
    CONSTRAINT uq_payable_service_type_code UNIQUE (code),
    CONSTRAINT fk_prn_tax_head_code FOREIGN KEY (prn_tax_head_code) REFERENCES pgateway.prn_tax_head(tax_head_code)
);

-- Comments on the table and its columns for clarity
COMMENT ON TABLE pgateway.payable_service_type IS 'Payable Service Type: Stores details of payable service types.';
COMMENT ON COLUMN pgateway.payable_service_type.id IS 'Unique identifier for the record, stored as a UUID string.';
COMMENT ON COLUMN pgateway.payable_service_type.service_type_desc IS 'Service Type Description: Describes the type of service.';
COMMENT ON COLUMN pgateway.payable_service_type.code IS 'Service Type Code: Unique code identifying the service type.';
COMMENT ON COLUMN pgateway.payable_service_type.is_active IS 'Is Active: Flag indicating whether the service type is currently active.';
COMMENT ON COLUMN pgateway.payable_service_type.mosip_process IS 'MOSIP Process: Identifies the associated MOSIP process.';
COMMENT ON COLUMN pgateway.payable_service_type.prn_tax_head_code IS 'PRN Tax Head Code: Foreign key referencing the tax head code.';
COMMENT ON COLUMN pgateway.payable_service_type.cr_by IS 'Created By: ID or name of the user who created the record.';
COMMENT ON COLUMN pgateway.payable_service_type.cr_dtimes IS 'Created DateTimestamp: Date and time when the record was created.';
COMMENT ON COLUMN pgateway.payable_service_type.upd_by IS 'Updated By: ID or name of the user who last updated the record.';
COMMENT ON COLUMN pgateway.payable_service_type.upd_dtimes IS 'Updated DateTimestamp: Date and time when the record was last updated.';
