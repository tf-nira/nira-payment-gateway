package io.mosip.gateway.payment.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

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
public class PrnTaxHeadEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", nullable = false, length = 36)
    private String id;
	
	@Column(name = "tax_head_code", nullable = false, unique = true)
	private String taxHeadCode;
	
	@Column(name = "tax_head_desc", nullable = false)
	private String taxHeadDesc;
	
	@Column(name = "tax_head_amount", nullable = false)
	private String taxHeadAmount;
	
	@Column(name = "is_active", nullable = false)
    private boolean isActive;
	
	@Column(name = "currency", nullable = false)
	private String currency;
	
	@Column(name = "cr_by", nullable = false, updatable = false)
	private String crBy;

	@Column(name = "cr_dtimes", nullable = false, updatable = false)
	private LocalDateTime crDatetime;

	@Column(name = "upd_by")
	private String upBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDatetime;
}
