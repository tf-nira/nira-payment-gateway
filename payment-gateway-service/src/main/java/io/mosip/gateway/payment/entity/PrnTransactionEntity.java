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
@Table(name = "prn_transaction", schema="pgateway")
public class PrnTransactionEntity implements  Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", nullable = false, length = 36)
    private String id;
	
	@Column(name = "prn", nullable = false)
    private String prn;
	
	@Column(name = "reg_id", nullable = false)
	private String regId;
	
	@Column(name = "cr_by", nullable = false, updatable = false)
	private String crBy;

	@Column(name = "cr_dtimes", nullable = false, updatable = false)
	private LocalDateTime crDatetime;

}
