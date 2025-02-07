package io.mosip.gateway.payment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.gateway.payment.entity.PayableServiceTypeEntity;
import io.mosip.gateway.payment.entity.PrnTaxHeadEntity;

public interface PayableServiceTypeRepository extends JpaRepository<PayableServiceTypeEntity, String>{
	
	PayableServiceTypeEntity findByServiceTypeCode(String serviceTypeCode);
	
	/*@Query("SELECT p.mosipProcess FROM PrnTaxHeadEntity p WHERE p.taxHeadCode = :taxHeadCode")
    String findMosipProcessByTaxHeadCode(@Param("taxHeadCode") String taxHeadCode);*/
	
	@Query("SELECT DISTINCT pst.serviceTypeCode FROM PayableServiceTypeEntity pst WHERE pst.prnTaxHeadCode.taxHeadCode = :taxHeadCode")
    List<String> findDistinctServiceTypeByTaxHeadCode(@Param("taxHeadCode") String taxHeadCode);

}
