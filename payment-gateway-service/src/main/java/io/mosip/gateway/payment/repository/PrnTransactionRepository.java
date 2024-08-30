package io.mosip.gateway.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.gateway.payment.entity.PrnTransactionEntity;


public interface PrnTransactionRepository extends JpaRepository<PrnTransactionEntity, Long>{
	
	List <PrnTransactionEntity> findAll();

	PrnTransactionEntity findByPrn(String prn);
	
	PrnTransactionEntity findByRegId(String regId);
	
	PrnTransactionEntity findByPrnAndRegId(String prn, String regId);
	
}
