package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;

import java.util.Optional;

public interface DiseaseTraitRepository extends MongoRepository<DiseaseTrait, String> {

    Page<DiseaseTrait> findByTrait(String trait, Pageable page);

    Page<DiseaseTrait> findByTraitContainingIgnoreCase(String trait, Pageable page);

    Optional<DiseaseTrait> findByTraitIgnoreCase(String trait);

}
