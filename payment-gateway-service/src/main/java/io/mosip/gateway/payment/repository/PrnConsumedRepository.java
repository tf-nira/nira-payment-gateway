package io.mosip.gateway.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.gateway.payment.entity.PrnConsumedEntity;


public interface PrnConsumedRepository extends JpaRepository<PrnConsumedEntity, Long>{
		
	List <PrnConsumedEntity> findAll();

	PrnConsumedEntity findByPrn(String prn);
	
}