package io.mosip.gateway.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;

public interface PrnTaxHeadRepository extends JpaRepository<PrnTaxHeadEntity, Long>{

	List <PrnTaxHeadEntity> findAll();

	PrnTaxHeadEntity findByTaxHeadCode(String taxHeadCode);
	
	PrnTaxHeadEntity findByMosipProcess(String mosipProcess);
	
	@Query("SELECT p.mosipProcess FROM PrnTaxHeadEntity p WHERE p.taxHeadCode = :taxHeadCode")
    String findMosipProcessByTaxHeadCode(@Param("taxHeadCode") String taxHeadCode);
}
