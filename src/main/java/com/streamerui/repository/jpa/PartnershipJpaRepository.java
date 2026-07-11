package com.streamerui.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PartnershipJpaRepository extends JpaRepository<PartnershipEntity, Long> {

    @Query("select p from PartnershipEntity p where p.requesterStreamerId = :sid or p.addresseeStreamerId = :sid")
    List<PartnershipEntity> findAllInvolving(@Param("sid") Long streamerId);

    @Query("select p from PartnershipEntity p where p.status = 'accepted' " +
           "and (p.requesterStreamerId = :sid or p.addresseeStreamerId = :sid)")
    List<PartnershipEntity> findAcceptedInvolving(@Param("sid") Long streamerId);

    @Query("select p from PartnershipEntity p where p.status in ('pending','accepted') " +
           "and ((p.requesterStreamerId = :a and p.addresseeStreamerId = :b) " +
           "  or (p.requesterStreamerId = :b and p.addresseeStreamerId = :a))")
    Optional<PartnershipEntity> findActiveBetween(@Param("a") Long a, @Param("b") Long b);
}
