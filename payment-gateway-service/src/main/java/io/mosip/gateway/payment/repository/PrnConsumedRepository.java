package io.mosip.gateway.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.gateway.payment.entity.PrnConsumedEntity;


public interface PrnConsumedRepository extends JpaRepository<PrnConsumedEntity, String>{
		
	List <PrnConsumedEntity> findAll();

	PrnConsumedEntity findByPrn(String prn);
	
}