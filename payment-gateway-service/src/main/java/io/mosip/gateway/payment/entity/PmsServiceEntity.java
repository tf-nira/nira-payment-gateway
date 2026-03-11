package io.mosip.gateway.payment.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pms_service", schema = "pgateway")
public class PmsServiceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false, length = 36)
	private String id;

	@Column(name = "service_desc", nullable = false)
	private String serviceDesc;

	@Column(name = "partner_type", nullable = false)
	private String partnerType;

	@Column(name = "partner_group", nullable = false)
	private String partnerGroup;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "prn_tax_head_code", referencedColumnName = "tax_head_code", nullable = false)
	private PrnTaxHeadEntity prnTaxHeadCode;

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Column(name = "cr_by", nullable = false, updatable = false)
	private String crBy;

	@Column(name = "cr_dtimes", nullable = false, updatable = false)
	private LocalDateTime crDatetime;

	@Column(name = "upd_by")
	private String updBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDatetime;
}