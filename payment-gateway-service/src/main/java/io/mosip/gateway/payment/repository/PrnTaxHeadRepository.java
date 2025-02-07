package io.mosip.gateway.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;

public interface PrnTaxHeadRepository extends JpaRepository<PrnTaxHeadEntity, String>{

	List <PrnTaxHeadEntity> findAll();

	PrnTaxHeadEntity findByTaxHeadCode(String taxHeadCode);
	
	PrnTaxHeadEntity findByCurrency(String currency);
	
	/*PrnTaxHeadEntity findByMosipProcess(String mosipProcess);
	
	@Query("SELECT p.mosipProcess FROM PrnTaxHeadEntity p WHERE p.taxHeadCode = :taxHeadCode")
    String findMosipProcessByTaxHeadCode(@Param("taxHeadCode") String taxHeadCode);
	
	PrnTaxHeadEntity findByServiceType(String serviceType);
	
	@Query("SELECT DISTINCT p.serviceType FROM PrnTaxHeadEntity p WHERE p.taxHeadCode = :taxHeadCode")
    List<String> findDistinctServiceTypeByTaxHeadCode(@Param("taxHeadCode") String taxHeadCode);*/
	
}
