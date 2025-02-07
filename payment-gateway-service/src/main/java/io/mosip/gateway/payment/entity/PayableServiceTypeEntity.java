package io.mosip.gateway.payment.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Table(name = "payable_service_type", schema="pgateway")
public class PayableServiceTypeEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", nullable = false, length = 36)
    private String id;
	
	@Column(name = "service_type_desc", nullable = false)
    private String serviceTypeDesc;
	
	@Column(name = "code", nullable = false, unique = true)
    private String serviceTypeCode;
	
	@Column(name = "is_active", nullable = false)
    private boolean isActive;
	
	@Column(name = "mosip_process", nullable = false)
	private String mosipProcess;
	
	@OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prn_tax_head_code", referencedColumnName = "tax_head_code")
    private PrnTaxHeadEntity prnTaxHeadCode;
	
	@Column(name = "cr_by", nullable = false, updatable = false)
    private String crBy;
    
    @Column(name = "cr_dtimes", nullable = false, updatable = false)
    private LocalDateTime crDatetime;

    @Column(name = "upd_by")
    private String updBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updDateTime;

}
