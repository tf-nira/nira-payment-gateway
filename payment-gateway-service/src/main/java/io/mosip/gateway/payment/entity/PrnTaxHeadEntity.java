package io.mosip.gateway.payment.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "prn_tax_head", schema="pgateway")
public class PrnTaxHeadEntity implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "prn_tax_head_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prnTaxHeadId;
	
	@Column(name = "tax_head_code")
	private String taxHeadCode;
	
	@Column(name = "tax_head_desc")
	private String taxHeadDesc;
	
	@Column(name = "tax_head_amount")
	private String taxHeadAmount;
	
	@Column(name = "is_tax_head_valid")
    private boolean isTaxHeadValid;
	
	@Column(name = "currency")
	private String currency;
	
	@Column(name = "mosip_process")
	private String mosipProcess;
    
	@Column(name = "cr_by")
	private String crBy;

	@Column(name = "cr_dtimes")
	private LocalDateTime crDatetime;

	@Column(name = "upd_by")
	private String upBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDatetime;
}
