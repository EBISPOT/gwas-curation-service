package uk.ac.ebi.spot.gwas.curation.oracle.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import uk.ac.ebi.spot.gwas.curation.oracle.model.EfoTrait;

import java.util.Optional;

public interface EFOTraitRepository extends JpaRepository<EfoTrait, Long> {

    Optional<EfoTrait> findByShortForm(String shortForm);
}
