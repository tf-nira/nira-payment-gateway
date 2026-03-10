package io.mosip.gateway.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.gateway.payment.entity.PmsServiceEntity;

@Repository
public interface PmsServiceRepository extends JpaRepository<PmsServiceEntity, String> {

	@Query("SELECT COUNT(p) > 0 FROM PmsServiceEntity p WHERE p.prnTaxHeadCode.taxHeadCode = :taxHeadCode AND p.isActive = true")
	boolean existsActiveServiceForTaxHead(String taxHeadCode);
	
	@Query("SELECT p FROM PmsServiceEntity p WHERE p.partnerType = :partnerType AND p.partnerGroup = :partnerGroup AND p.isActive = true")
	Optional<PmsServiceEntity> findActiveService(String partnerType,String partnerGroup);
}