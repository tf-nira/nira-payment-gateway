package io.mosip.gateway.payment.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
@Table(name = "prn_consumed", schema="pgateway")
public class PrnConsumedEntity implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "prn", nullable = false, unique = true)
    private String prn;
	
	@Column(name = "prn_data", nullable = false)
	private String prnData;

    @Column(name = "is_prn_valid", nullable = false)
    private boolean isPrnValid;
    
    @Column(name = "cr_by", nullable = false, updatable = false)
    private String crBy;
    
    @Column(name = "cr_dtimes", nullable = false, updatable = false)
    private LocalDateTime crDatetime;

	@Column(name = "upd_by")
	private String upBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDatetime;
}